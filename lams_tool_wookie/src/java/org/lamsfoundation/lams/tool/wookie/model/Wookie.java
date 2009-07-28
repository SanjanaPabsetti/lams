/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */
/* $$Id$$ */

package org.lamsfoundation.lams.tool.wookie.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.contentrepository.client.IToolContentHandler;
import org.lamsfoundation.lams.tool.wookie.service.WookieService;

/**
 * @hibernate.class table="tl_lawook10_wookie"
 */

public class Wookie implements java.io.Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 579733009969321015L;

    static Logger log = Logger.getLogger(WookieService.class.getName());

    // Fields
    /**
     * 
     */
    private Long uid;

    private Date createDate;

    private Date updateDate;

    private Long createBy;

    private String title;

    private String instructions;

    private boolean runOffline;

    private boolean lockOnFinished;

    private boolean reflectOnActivity;
    
    private boolean allowViewOthersImages;

    private String reflectInstructions;

    private String onlineInstructions;

    private String offlineInstructions;

    private boolean contentInUse;

    private boolean defineLater;

    private Long toolContentId;

    private String imageFileName; // Image uploaded for wookie
    
    private Long imageWidth;
    
    private Long imageHeight;

    private Set<WookieAttachment> wookieAttachments;

    private Set<WookieSession> wookieSessions;

    // *********** NON Persisit fields
    private IToolContentHandler toolContentHandler;

    // Constructors

    /** default constructor */
    public Wookie() {
    }

    /** full constructor */
    public Wookie(Date createDate, Date updateDate, Long createBy, String title, String instructions,
	    boolean runOffline, boolean lockOnFinished, boolean filteringEnabled, String filterKeywords,
	    String onlineInstructions, String offlineInstructions, boolean contentInUse, boolean defineLater,
	    Long toolContentId, String reflectInstructions, Set<WookieAttachment> wookieAttachments,
	    Set<WookieSession> wookieSessions, String imageFileName, Long imageWidth, Long imageHeight, boolean allowViewOthersImages) {
	this.createDate = createDate;
	this.updateDate = updateDate;
	this.createBy = createBy;
	this.title = title;
	this.instructions = instructions;
	this.runOffline = runOffline;
	this.lockOnFinished = lockOnFinished;
	this.onlineInstructions = onlineInstructions;
	this.offlineInstructions = offlineInstructions;
	this.contentInUse = contentInUse;
	this.defineLater = defineLater;
	this.toolContentId = toolContentId;
	this.wookieAttachments = wookieAttachments;
	this.wookieSessions = wookieSessions;
	this.imageFileName = imageFileName;
	this.reflectInstructions = reflectInstructions;
	this.imageHeight = imageHeight;
	this.imageWidth = imageWidth;
	this.allowViewOthersImages = allowViewOthersImages;
    }

    // Property accessors
    /**
     * @hibernate.id generator-class="native" type="java.lang.Long" column="uid"
     * 
     */

    public Long getUid() {
	return uid;
    }

    public void setUid(Long uid) {
	this.uid = uid;
    }

    /**
     * @hibernate.property column="create_date"
     * 
     */

    public Date getCreateDate() {
	return createDate;
    }

    public void setCreateDate(Date createDate) {
	this.createDate = createDate;
    }

    /**
     * @hibernate.property column="update_date"
     * 
     */

    public Date getUpdateDate() {
	return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
	this.updateDate = updateDate;
    }

    /**
     * @hibernate.property column="create_by" length="20"
     * 
     */

    public Long getCreateBy() {
	return createBy;
    }

    public void setCreateBy(Long createBy) {
	this.createBy = createBy;
    }

    /**
     * @hibernate.property column="title" length="255"
     * 
     */

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @hibernate.property column="instructions" length="65535"
     * 
     */

    public String getInstructions() {
	return instructions;
    }

    public void setInstructions(String instructions) {
	this.instructions = instructions;
    }

    /**
     * @hibernate.property column="run_offline" length="1"
     * 
     */

    public boolean isRunOffline() {
	return runOffline;
    }

    public void setRunOffline(boolean runOffline) {
	this.runOffline = runOffline;
    }

    /**
     * @hibernate.property column="lock_on_finished" length="1"
     * 
     */

    public boolean isLockOnFinished() {
	return lockOnFinished;
    }

    public void setLockOnFinished(boolean lockOnFinished) {
	this.lockOnFinished = lockOnFinished;
    }

    /**
     * @hibernate.property column="reflect_on_activity" length="1"
     */
    public boolean isReflectOnActivity() {
	return reflectOnActivity;
    }

    public void setReflectOnActivity(boolean reflectOnActivity) {
	this.reflectOnActivity = reflectOnActivity;
    }
    
    /**
     * @hibernate.property column="allow_view_others_images" length="1"
     */
    public boolean isAllowViewOthersImages() {
        return allowViewOthersImages;
    }

    public void setAllowViewOthersImages(boolean allowViewOthersImages) {
        this.allowViewOthersImages = allowViewOthersImages;
    }

    /**
     * @hibernate.property column="online_instructions" length="65535"
     * 
     */
    public String getOnlineInstructions() {
	return onlineInstructions;
    }

    public void setOnlineInstructions(String onlineInstructions) {
	this.onlineInstructions = onlineInstructions;
    }

    /**
     * @hibernate.property column="offline_instructions" length="65535"
     * 
     */

    public String getOfflineInstructions() {
	return offlineInstructions;
    }

    public void setOfflineInstructions(String offlineInstructions) {
	this.offlineInstructions = offlineInstructions;
    }

    /**
     * @hibernate.property column="content_in_use" length="1"
     * 
     */

    public boolean isContentInUse() {
	return contentInUse;
    }

    public void setContentInUse(boolean contentInUse) {
	this.contentInUse = contentInUse;
    }

    /**
     * @hibernate.property column="define_later" length="1"
     * 
     */

    public boolean isDefineLater() {
	return defineLater;
    }

    public void setDefineLater(boolean defineLater) {
	this.defineLater = defineLater;
    }

    /**
     * @hibernate.property column="tool_content_id" length="20"
     * 
     */

    public Long getToolContentId() {
	return toolContentId;
    }

    public void setToolContentId(Long toolContentId) {
	this.toolContentId = toolContentId;
    }
    
    

    /**
     * @hibernate.set lazy="true" inverse="false" cascade="all-delete-orphan"
     * @hibernate.collection-key column="wookie_uid"
     * @hibernate.collection-one-to-many class="org.lamsfoundation.lams.tool.wookie.model.WookieAttachment"
     * 
     */

    public Set<WookieAttachment> getWookieAttachments() {
	return wookieAttachments;
    }

    public void setWookieAttachments(Set<WookieAttachment> wookieAttachments) {
	this.wookieAttachments = wookieAttachments;
    }

    /**
     * @hibernate.set lazy="true" inverse="true" cascade="none"
     * @hibernate.collection-key column="wookie_uid"
     * @hibernate.collection-one-to-many class="org.lamsfoundation.lams.tool.wookie.model.WookieSession"
     * 
     */

    public Set<WookieSession> getWookieSessions() {
	return wookieSessions;
    }

    public void setWookieSessions(Set<WookieSession> wookieSessions) {
	this.wookieSessions = wookieSessions;
    }

    /**
     * toString
     * 
     * @return String
     */
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();

	buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
	buffer.append("title").append("='").append(getTitle()).append("' ");
	buffer.append("instructions").append("='").append(getInstructions()).append("' ");
	buffer.append("toolContentId").append("='").append(getToolContentId()).append("' ");
	buffer.append("]");

	return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
	if (this == other) {
	    return true;
	}
	if (other == null) {
	    return false;
	}
	if (!(other instanceof Wookie)) {
	    return false;
	}
	Wookie castOther = (Wookie) other;

	return this.getUid() == castOther.getUid() || this.getUid() != null && castOther.getUid() != null
		&& this.getUid().equals(castOther.getUid());
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 37 * result + (getUid() == null ? 0 : this.getUid().hashCode());
	return result;
    }

    public static Wookie newInstance(Wookie fromContent, Long toContentId, IToolContentHandler wookieToolContentHandler) {
	Wookie toContent = new Wookie();
	fromContent.toolContentHandler = wookieToolContentHandler;
	toContent = (Wookie) fromContent.clone();
	toContent.setToolContentId(toContentId);
	toContent.setCreateDate(new Date());
	return toContent;
    }

    @Override
    protected Object clone() {

	Wookie wookie = null;
	try {
	    wookie = (Wookie) super.clone();
	    wookie.setUid(null);

	    if (wookieAttachments != null) {
		// create a copy of the attachments
		Iterator<WookieAttachment> iter = wookieAttachments.iterator();
		Set<WookieAttachment> set = new HashSet<WookieAttachment>();
		while (iter.hasNext()) {
		    WookieAttachment originalFile = (WookieAttachment) iter.next();
		    WookieAttachment newFile = (WookieAttachment) originalFile.clone();
		    set.add(newFile);
		}
		wookie.wookieAttachments = set;
	    }
	    // create an empty set for the wookieSession
	    wookie.wookieSessions = new HashSet<WookieSession>();

	} catch (CloneNotSupportedException cnse) {
	    Wookie.log.error("Error cloning " + Wookie.class);
	}
	return wookie;
    }

    public IToolContentHandler getToolContentHandler() {
	return toolContentHandler;
    }

    public void setToolContentHandler(IToolContentHandler toolContentHandler) {
	this.toolContentHandler = toolContentHandler;
    }

    /**
     * @hibernate.property column="image_file_name" length="511"
     * 
     */

    public String getImageFileName() {
	return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
	this.imageFileName = imageFileName;
    }
    
    /**
     * @hibernate.property column="image_width" 
     * 
     */

    public Long getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Long imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     * @hibernate.property column="image_height" 
     * 
     */
    
    public Long getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Long imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * @hibernate.property column="reflect_instructions" length="65535"
     */
    public String getReflectInstructions() {
	return reflectInstructions;
    }

    public void setReflectInstructions(String reflectInstructions) {
	this.reflectInstructions = reflectInstructions;
    }

}
