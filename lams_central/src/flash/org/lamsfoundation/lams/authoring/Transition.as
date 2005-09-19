﻿import org.lamsfoundation.lams.authoring.*;
import org.lamsfoundation.lams.common.*;
import org.lamsfoundation.lams.common.util.*;/**
* Transition data class. Transitions are used in the design to join up activities. Stored in the DDM
*
* @author      DC
* @version     0.1
* @comments    DesignDataModel stores a complete learning design
*/
class org.lamsfoundation.lams.authoring.Transition {
	
	//Transition properties
	
	private var _transitionID:Number;
	private var _uiID:Number;
	private var _fromActivityID:Number;
	private var _fromUIID:Number;
	private var _toActivityID:Number;
	private var _toUIID:Number;
	
	private var _title:String;
	private var _description:String;
	
	private var _createDateTime:Date;
	private var _learningDesignID:Number;
	
	
	function Transition(uiID,
						fromUIID,
						toUIID,
						learningDesignID){
		_uiID = uiID;
		_fromUIID = fromUIID;
		_toUIID = toUIID;
		_learningDesignID = learningDesignID;
		
	}
	
	public function isToGateActivity():Boolean{
		var ddm = Application.getInstance().getDesignDataModel();
		var a:Activity = ddm.getActivityByUIID(this.toActivityID);
		return a.isGateActivity();
	}
	
	
	public function toData():Object{
		var dto:Object = new Object();
		dto.transitionID = (_transitionID) ?  _transitionID : Config.NUMERIC_NULL_VALUE;
		dto.uiID= (_uiID) ?  _uiID : Config.NUMERIC_NULL_VALUE;
		dto.fromActivityID = (_fromActivityID) ?  _fromActivityID : Config.NUMERIC_NULL_VALUE;
		dto.fromUIID = (_fromUIID) ?  _fromUIID : Config.NUMERIC_NULL_VALUE;
		dto.toActivityID = (_toActivityID) ?  _toActivityID : Config.NUMERIC_NULL_VALUE;
		dto.toUIID = (_toUIID) ?  _toUIID : Config.NUMERIC_NULL_VALUE;
		dto.title = (_title) ?  _title : Config.STRING_NULL_VALUE;
		dto.description = (_description) ?  _description : Config.STRING_NULL_VALUE;
		 Debugger.log('Config.DATE_NULL_VALUE:' + Config.DATE_NULL_VALUE,Debugger.GEN,'toData','Canvas');
		dto.createDateTime = (_createDateTime) ?  _createDateTime : Config.DATE_NULL_VALUE;
		dto.learningDesignID = (_learningDesignID) ?  _learningDesignID : Config.NUMERIC_NULL_VALUE;
		return dto;
	}

	public function set transitionID(a:Number):Void{
		_transitionID = a;
	}
	public function get transitionID():Number{
		return _transitionID;
	}	public function set uiID(a:Number):Void{
		_uiID = a;
	}
	public function get uiID():Number{
		return _uiID;
	}
	
	public function set fromActivityID(a:Number):Void{
		_fromActivityID = a;
	}
	public function get fromActivityID():Number{
		return _fromActivityID;
	}
	
	public function set fromUIID(a:Number):Void{
		_fromUIID = a;
	}
	public function get fromUIID():Number{
		return _fromUIID;
	}
	
	public function set toActivityID(a:Number):Void{
		_toActivityID = a;
	}
	public function get toActivityID():Number{
		return _toActivityID;
	}	public function set toUIID(a:Number):Void{
		_toUIID = a;
	}
	public function get toUIID():Number{
		return _toUIID;
	}
	public function set title(a:String):Void{
		_title = a;
	}
	public function get title():String{
		return _title;
	}
	
	public function set description(a:String):Void{
		_description = a;
	}
	public function get description():String{
		return _description;
	}
	
	public function set createDateTime(a:Date):Void{
		_createDateTime = a;
	}
	public function get createDateTime():Date{
		return _createDateTime;
	}
	
	
	public function set learningDesignID(a):Void{
		_learningDesignID = a;
	}
	public function get learningDesignID():Number{
		return _learningDesignID;
	}
	
}