/****************************************************************
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
 * ****************************************************************
 */

/* $$Id$$ */
package org.lams.lams.tool.wiki.web;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author mtruong
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Creation Date: 12-07-05
 *  
 * ----------------XDoclet Tags--------------------
 * 
 * @struts:form name="WikiMonitoringForm" type="org.lams.lams.tool.wiki.web.WikiMonitoringForm"
 *
 * ----------------XDoclet Tags--------------------
 */ 
public class WikiMonitoringForm extends ActionForm {

	private static final long serialVersionUID = 6958826482877304278L;


	static Logger logger = Logger.getLogger(WikiMonitoringForm.class.getName());
    
       
    private String toolContentID;
    
    private String method;
    
    private Map parametersToAppend;
    
	private String currentTab;

	/* Only valid when form just set up by Java. Values are not returned from jsp page */
	private String title;
	private String content;
	private String onlineInstructions;
	private String offlineInstructions;
	private String contentEditable;
	private List attachmentsList;
	private Integer totalLearners;
	private Map groupStatsMap;

	/**
     * @return Returns the parametersToAppend.
     */
    public Map getParametersToAppend() {
        return parametersToAppend;
    }
    /**
     * @param parametersToAppend The parametersToAppend to set.
     */
    public void setParametersToAppend(Map parametersToAppend) {
        this.parametersToAppend = parametersToAppend;
    }
    /**
     * @return Returns the method.
     */
    public String getMethod() {
        return method;
    }
    /**
     * @param method The method to set.
     */
    public void setMethod(String method) {
        this.method = method;
    }
    /**
     * @return Returns the toolContentId.
     */
    public String getToolContentID() {
        return toolContentID;
    }
    /**
     * @param toolContentId The toolContentId to set.
     */
    public void setToolContentID(String toolContentId) {
        this.toolContentID = toolContentId;
    }
    public void reset(ActionMapping mapping, HttpServletRequest request)
	{
		this.method = null;
		this.parametersToAppend = null;		
		
	}
	public String getCurrentTab() {
		return currentTab;
	}
	public void setCurrentTab(String currentTab) {
		this.currentTab = currentTab;
	}
 
	public String getContent() {
		return content;
	}

	public String getOfflineInstructions() {
		return offlineInstructions;
	}

	public String getOnlineInstructions() {
		return onlineInstructions;
	}

	public String getTitle() {
		return title;
	}

	public String getContentEditable() {
		return contentEditable;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setContentEditable(String contentEditable) {
		this.contentEditable = contentEditable;
	}
	public void setOfflineInstructions(String offlineInstructions) {
		this.offlineInstructions = offlineInstructions;
	}
	public void setOnlineInstructions(String onlineInstructions) {
		this.onlineInstructions = onlineInstructions;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List getAttachmentsList() {
		return attachmentsList;
	}
	public void setAttachmentsList(List attachmentsList) {
		this.attachmentsList = attachmentsList;
	}
	public Map getGroupStatsMap() {
		return groupStatsMap;
	}
	public void setGroupStatsMap(Map groupStatsMap) {
		this.groupStatsMap = groupStatsMap;
	}
	public Integer getTotalLearners() {
		return totalLearners;
	}
	public void setTotalLearners(Integer totalLearners) {
		this.totalLearners = totalLearners;
	}
}
