/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * =============================================================
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */
/* $$Id$$ */
package org.lamsfoundation.lams.learningdesign.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import org.lamsfoundation.lams.learningdesign.Activity;
import org.lamsfoundation.lams.learningdesign.ComplexActivity;
import org.lamsfoundation.lams.learningdesign.LearningLibrary;
import org.lamsfoundation.lams.util.wddx.WDDXTAGS;

/**
 * @author Manpreet Minhas
 * 
 * This class acts as a data transfer object for transfering 
 * information between FLASH and the core module. It passes 
 * information about all available Learning Libraries.
 * 
 */
public class LearningLibraryDTO extends BaseDTO {
	
	private Long learningLibraryID;
	private String description;
	private String title;
	private Boolean validFlag;
	private Vector templateActivities;
	
	
	public LearningLibraryDTO(){
		
	}
	public LearningLibraryDTO(Long learningLibraryID, String description,
			String title, Boolean validFlag,
			Vector templateActivities) {		
		this.learningLibraryID = learningLibraryID;
		this.description = description;
		this.title = title;
		this.validFlag = validFlag;		
		this.templateActivities = templateActivities;
	} 
	
	
	public LearningLibraryDTO(LearningLibrary learningLibrary){
		this.learningLibraryID = learningLibrary.getLearningLibraryId();
		this.description = learningLibrary.getDescription();
		this.title = learningLibrary.getTitle();
		this.validFlag = learningLibrary.getValidLibrary();		
		this.templateActivities = populateActivities(learningLibrary.getActivities().iterator());
	}
	
	public LearningLibraryDTO(LearningLibrary learningLibrary, List templateActivity)
	{
		this.learningLibraryID = learningLibrary.getLearningLibraryId();
		this.description = learningLibrary.getDescription();
		this.title = learningLibrary.getTitle();
		this.validFlag = learningLibrary.getValidLibrary();	
		this.templateActivities = populateActivities(templateActivity.iterator());
	}
	
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return Returns the learningLibraryID.
	 */
	public Long getLearningLibraryID() {
		return learningLibraryID;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @return Returns the validFlag.
	 */
	public Boolean getValidFlag() {
		return validFlag;
	}
	
	/**
	 * @return Returns the activities.
	 */
	public Vector getTemplateActivities() {
		return templateActivities;
	} 
	
  /* public Vector populateActivities(Iterator iterator){		
		Vector activities = new Vector();
		while(iterator.hasNext()){
			Activity activity = (Activity)iterator.next();
			activities.add(activity.getLibraryActivityDTO());
		}		
		return activities;		
	}
	*/
	
	public Vector populateActivities(Iterator iterator)
	{
	    Vector activities = new Vector();
	    Vector childActivities = null;
		while(iterator.hasNext()){
			Activity object = (Activity) iterator.next();
			
			if(object.isComplexActivity()){ //parallel, sequence or options activity
				ComplexActivity complexActivity = (ComplexActivity)object;
				Iterator childIterator = complexActivity.getActivities().iterator();
				childActivities = new Vector();
				while(childIterator.hasNext()){
					Activity activity =(Activity)childIterator.next();
					childActivities.add(activity.getLibraryActivityDTO());					
				}				
				activities.add(complexActivity.getLibraryActivityDTO());
				activities.addAll(childActivities);
			}else{
				activities.add(object.getLibraryActivityDTO());
			}			
		}
		return activities;
	}
	
	
}
