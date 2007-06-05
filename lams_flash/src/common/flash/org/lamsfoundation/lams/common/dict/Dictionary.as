﻿/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ************************************************************************
 */
import org.lamsfoundation.lams.common.*
import org.lamsfoundation.lams.common.dict.*
import org.lamsfoundation.lams.common.util.*
import org.lamsfoundation.lams.common.comms.*
import org.lamsfoundation.lams.authoring.*;

/**
* Dictionary - Holds all of the LAMS text allowing LAMS to be multilingual
* 
* @class Dictionary
* @author DI
*/
dynamic class Dictionary {

	private var items:Hashtable;
    private var _currentLanguage:String;
	private var _currentDate:Object;
	private var _module:String;
	private var comms:Communication;	
	private var app:ApplicationParent;	
    
    //Application instance is stored as a static in the application class
    private static var _instance:Dictionary = null;    
	private static var DICT_PREFIX:String = "dictionary.";
	private static var DICT_DATE:String = "dictionary.date";
	private static var SO_SEPARATOR:String = ".";
	
    //Defined so compiler can 'see' events added at runtime by EventDispatcher
    private var dispatchEvent:Function;     
    public var addEventListener:Function;
    public var removeEventListener:Function;

    /**
    * Constructor - Dictionary is singleton so private 
    */
    private function Dictionary() {
		//Get comms and application references        
        app = ApplicationParent.getInstance();
        comms = app.getComms();		
		mx.events.EventDispatcher.initialize(this);
        items = new Hashtable();
    }

    /**
    * Retrieves an instance of the Application singleton
    */ 
    public static function getInstance():Dictionary{
        if(Dictionary._instance == null){
            Dictionary._instance = new Dictionary();
        }
        return Dictionary._instance;
    }


	/**
	* Load the dictionary for the language specified
	* @param language (string) language parameter.
	*/
	public function load(language:String):Void {
        //TODO DI 19/05/05 Check cookie and if not present load from server
        //Set Language
        this._currentLanguage = language;
		this._currentDate = _root.langDate;
		
		var _oldDate:Object = null;
		var _removeCache:Boolean = Config.getInstance().removeCache;
        // Cookie or server?
		Debugger.log('Config.DOUBLE_CLICK_DELAY:'+Config.DOUBLE_CLICK_DELAY,Debugger.GEN,'load','org.lamsfoundation.lams.dict.Dictionary');
		Debugger.log('Config.USE_CACHE:'+Config.USE_CACHE,Debugger.GEN,'load','org.lamsfoundation.lams.dict.Dictionary');
        if(CookieMonster.cookieExists(DICT_DATE)){
			_oldDateCookie = CookieMonster.open(DICT_DATE, true);
		}
		
		Debugger.log('Existing date:'+String(_oldDateCookie.date) + ' Current date:' + String(_currentDate),Debugger.GEN,'load','org.lamsfoundation.lams.dict.Dictionary');
        
		// determine if dictionary should be loaded from cookie file or from server request
		if(CookieMonster.cookieExists(DICT_PREFIX+language+SO_SEPARATOR+app.module) && Config.USE_CACHE && _oldDateCookie.date != null && !_removeCache) {
			
			// clear all data if dictionaries have been updated
			if(this._currentDate != _oldDateCookie.date){
				Debugger.log('Removing all dictionary cookies (updated date).',Debugger.GEN,'load','org.lamsfoundation.lams.dict.Dictionary');
				clearAll(_oldDateCookie);
				openFromServer();
			} else {
				openFromDisk();
			}
		} else if(_removeCache) {
			Debugger.log('Removing all dictionary cookies (clear cache)',Debugger.GEN,'load','org.lamsfoundation.lams.dict.Dictionary');
			clearAll(_oldDateCookie);
			openFromServer();
		} else {
            openFromServer();
        }        
	}
	
	/**
	* event broadcast when a new language is loaded 
	*/ 
	public function broadcastInit(){
		dispatchEvent({type:'init',target:this});		
	}
 
	/**
	 * Remove all stored dictionary data and old dictionary date cookie
	 * 
	 * @param   obj Array of language/module combination strings
	 *
	 */
	public function clearAll(obj:Object) {
		for(var i=0; i<obj.data.length; i++){
			if(CookieMonster.cookieExists(DICT_PREFIX + obj.data[i])){
				CookieMonster.deleteCookie(DICT_PREFIX + obj.data[i]);
			}
		}
		
		if(CookieMonster.cookieExists(DICT_DATE)){
			CookieMonster.deleteCookie(DICT_DATE);
		}
	}
    /**
    * @returns a string holding the text requested
    */
    public static function getValue(key:String, param:Array):String{
        Debugger.log('requesting :' + key + ' > ' + getInstance().items.get(key).value,Debugger.GEN,'getValue','org.lamsfoundation.lams.dict.Dictionary');
        var v:String = _instance.items.get(key).value;
		if(v!=null){
			if(param!=null){
				trace('using params..');
				for(var i=0; i<param.length; i++){
					trace('param values: ' + param[0]);
					// replace param {i} with value in array
					var str_toReplace =  String.fromCharCode(123) + String(i) + String.fromCharCode(125);
					trace(str_toReplace);
					var newStr:String = StringUtils.replace(v, str_toReplace, String(param[i]));
					trace(String(newStr));
					v = newStr;
				}
			}
			return v;
		}else{
			Debugger.log('Entry not found in '+getInstance()._currentLanguage+' dictionary, key='+key,Debugger.CRITICAL,'getValue','Dictionary');
			return "?";
		}
		
		
    }
	
	/**
    * @returns a string holding the text requested
    */
    public static function getDescription(key:String):String{
        Debugger.log('requesting description for :' + key + ' > ' + getInstance().items.get(key).description,Debugger.GEN,'getValue','org.lamsfoundation.lams.dict.Dictionary');
        var v:String = _instance.items.get(key).description;
		if(v!=null){
			return v;
		}else{
			Debugger.log('Entry not found in '+getInstance()._currentLanguage+' dictionary, key='+key,Debugger.CRITICAL,'createFromData','Dictionary');
			return "?";
		}
		
		
    }
    
    /**
    * Converts the dictionary data into a data object ready for serializing
    * @returns Object containing the dictionary converted to a dataObject 
    */
    public function toData():Object{
        var obj:Object ={};
        var hashKeys:Array = items.keys();
        var hashValues:Array = items.values();
        
        obj = [];
        
        //Go through hash of dictionary items and get data objects for each
        for(var i=0;i<hashKeys.length;i++){
            obj[i] = hashValues[i].toData();
            obj[i].key = hashKeys[i];
        }
        return obj;
    }
    
    /**
    * Creates Dictionary info from data object
    * @param dataObj - a data Object to convert to a dictionary
    */
    public function createFromData(dataObj:Object):Void{
		Debugger.log('creating from data',Debugger.CRITICAL,'createFromData','Dictionary');

        //First empty hash 
        items.clear();
        //_global.breakpoint();
        //Go through data object and copy values to items hash
        for(var i=0;i<dataObj.length;i++){
            //Create hashKey object and then prune hashkey from dataObject before adding dictionary item to hashtable 
            var hashKey = dataObj[i].key;
            //delete dataObj[i].key;
            
			var newDataObj = new Object();
			newDataObj.value = dataObj[i].value;
			newDataObj.description = dataObj[i].description;
			
			//Create the dictionary item from the data object
            var dictItem:DictionaryItem = DictionaryItem.createFromData(newDataObj)
            items.put(hashKey,dictItem);
        }
        //Dispatch load event 
        dispatchEvent({type:'load',target:this});
    }
    
    /**
    * Save the Dictionary to disk 
    * @return  
    */
    public function saveToDisk():Void{
		
        //Convert to data object and then serialize before saving to a cookie
        var dataObj = toData();
		var success = CookieMonster.save(dataObj,DICT_PREFIX + _currentLanguage + SO_SEPARATOR + app.module,false);
	
		if (!success) {
			//TODO DI 30/05/05 raise error if config data can't be saved
			Debugger.log('Config item could not be saved: ' + _root.langDate ,Debugger.CRITICAL,'saveToDisk','Dictionary');
		}
	
		// open date cookie if exists
		var dateCookie:Object = null;
		if(CookieMonster.cookieExists(DICT_DATE)){
			dateCookie = CookieMonster.open(DICT_DATE, true);
		}
	
		// create new date cookie object
		var dateObj = new Object();
		dateObj.date = _root.langDate;
		dateObj.data = (dateCookie == null) ? new Array() : getDateArray(dateCookie.data);
		
		var dataItem:String = _currentLanguage + SO_SEPARATOR + app.module;
		dateObj.data.push(dataItem);
		
		// save to file
		success = CookieMonster.save(dateObj, DICT_DATE, true);
		
		if (!success) {
			//TODO DI 30/05/05 raise error if config data can't be saved
			Debugger.log('Config item could not be saved: ' + _root.langDate ,Debugger.CRITICAL,'saveToDisk','Dictionary');
		}
    }
    
	/**
	 * Get the string array of loaded dictionaries
	 * 
	 * 
	 * @param   data 	dictionary date cookie
	 * @return  		array of loaded dictionaries
	 */
	
	private function getDateArray(data:Array):Array {
		var arr:Array = new Array();
		for(var i=0; i<data.length; i++){
			arr[i] = data[i];
		}
		return arr;
	}
	
    /**
    * Open the Dictionary from disk 
    */
    public function openFromDisk():Void{
        Debugger.log('opening Dictionary from Shared Object',Debugger.CRITICAL,'openFromDisk','Dictionary');		
	   
		var dataObj:Object = CookieMonster.open(DICT_PREFIX + _currentLanguage + SO_SEPARATOR + app.module,false);
		createFromData(dataObj);
		
    }
    
    /**
    * Handler function for dictionary being returned from the server
    */
    public function onDictionaryLoadedFromServer(dictionaryDataObj:Object){
		Debugger.log('onDictionaryLoadedFromServer',Debugger.CRITICAL,'onDictionaryLoadedFromServer','Dictionary');		
	   //Create the dictionary from the data obj
        createFromData(dictionaryDataObj);
		
        //Now that dict has been loaded by server, cache it 
        saveToDisk();		
    }

    /**
    * Opens Dictionary from server
    */
    public function openFromServer() {
		this._module = app.module;
		Debugger.log('URL : '+ 'flashxml/' + _module  + '/' + _currentLanguage + '_dictionary.xml',Debugger.CRITICAL,'openFromServer','Dictionary');
		var callBack = Proxy.create(this,onDictionaryLoadedFromServer);
        //comms.loadXML('http://dolly.uklams.net/lams/lams_authoring/' + _currentLanguage + '_dictionary.xml',callBack,true,true);
        comms.getRequest('flashxml/' + _module + '/' + _currentLanguage + '_dictionary.xml' + "?v=" + _root.build,callBack,false);
    }
    
    /**
    * TODO: THIS IS ONLY USED FOR TESTING WHILST SERVER DOES NOT SUPPORT STRUCTURE
    * REMOVE WHEN NO LONGER REQUIRED
    */
    public function createDictionaryFromCode(lang:String) {
        Debugger.log('SHOULD NOT BE CALLED _ NO LONGER USED!!',Debugger.CRITICAL,'createDictionaryFromCode','Dictionary');
		//_global.breakpoint();
        //Clear the dictionary + set language
        /*
		items.clear();
        _currentLanguage = lang;
        switch (lang) {
            case 'en' : 
                //this will return a structure that will be passed into createFromData()
                items.put(0,new DictionaryItem(0,'Workspace'));
                items.put(1,new DictionaryItem(1,'OK'));
                items.put(2,new DictionaryItem(2,'Cancel'));
                items.put(4,new DictionaryItem(4,'Preferences'));
                items.put(5,new DictionaryItem(5,'OK'));
                items.put(6,new DictionaryItem(6,'Cancel'));
                items.put(7,new DictionaryItem(7,'Language'));
                items.put(8,new DictionaryItem(8,'Theme'));
                
                //--Menu Items
                items.put(9,new DictionaryItem(9,'New'));
                items.put(10,new DictionaryItem(10,'Open'));
                items.put(11,new DictionaryItem(11,'Revert'));
                items.put(12,new DictionaryItem(12,'Close'));
                items.put(13,new DictionaryItem(13,'Save'));
                items.put(14,new DictionaryItem(14,'Draw Transition'));
                items.put(15,new DictionaryItem(15,'Draw Optional'));
                items.put(16,new DictionaryItem(16,'Preferences'));
                items.put(17,new DictionaryItem(17,'File'));
                items.put(18,new DictionaryItem(18,'Tools'));
                items.put(19,new DictionaryItem(19,'Help'));
                items.put(20,new DictionaryItem(20,'About'));
                items.put(21,new DictionaryItem(21,'Preferences'));

                break;
            case 'fr' : 
                //FRENCH TEXT
                items.put(0,new DictionaryItem(0,'zone de travail'));
                items.put(1,new DictionaryItem(1,'OK'));
                items.put(2,new DictionaryItem(2,'Annuler'));
                items.put(4,new DictionaryItem(4,'Préférences'));
                items.put(5,new DictionaryItem(5,'OK'));
                items.put(6,new DictionaryItem(6,'Annuler'));
                items.put(7,new DictionaryItem(7,'Langue'));
                items.put(8,new DictionaryItem(8,'Thème'));

                //--Menu Items
                items.put(9,new DictionaryItem(9,'Nouveau'));
                items.put(10,new DictionaryItem(10,'Ouvert'));
                items.put(11,new DictionaryItem(11,'Retournez'));
                items.put(12,new DictionaryItem(12,'Fin'));
                items.put(13,new DictionaryItem(13,'Save'));
                items.put(14,new DictionaryItem(14,'Aspiration Transitoire'));
                items.put(15,new DictionaryItem(15,'Aspiration Facultative'));
                items.put(16,new DictionaryItem(16,'Préférences'));
                items.put(17,new DictionaryItem(17,'Fichier'));
                items.put(18,new DictionaryItem(18,'Outils'));
                items.put(19,new DictionaryItem(19,'Aide'));
                items.put(20,new DictionaryItem(20,'À propos de...'));
                items.put(21,new DictionaryItem(21,'Préférences'));
                break;
            default :
        }
        //Dispatch load event 
        dispatchEvent({type:'load',target:this});
		*/
   }
    
    /**
    * Returns the currently selected language
    */
    function get currentLanguage():String {
        return _currentLanguage;
    }
}
