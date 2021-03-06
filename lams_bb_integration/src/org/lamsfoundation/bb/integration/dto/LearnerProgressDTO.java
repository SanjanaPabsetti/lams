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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 * USA 
 * 
 * http://www.gnu.org/licenses/gpl.txt 
 * **************************************************************** 
 */


package org.lamsfoundation.bb.integration.dto;

public class LearnerProgressDTO {

    private Long lessonId;
    private String lessonName;
    private String userName;
    private String lastName;
    private String firstName;
    private Integer learnerId;
    private int activityCount;
    private int attemptedActivities;
    private int activitiesCompleted;
    private boolean lessonComplete;

    public LearnerProgressDTO(int activityCount, int attemptedActivities, int activitiesCompleted,
	    boolean lessonComplete) {
	this.lessonId = lessonId;
	this.lessonName = lessonName;
	this.userName = userName;
	this.lastName = lastName;
	this.firstName = firstName;
	this.learnerId = learnerId;
	this.activityCount = activityCount;
	this.attemptedActivities = attemptedActivities;
	this.activitiesCompleted = activitiesCompleted;
	this.lessonComplete = lessonComplete;
    }

    /**
     * @return Returns the learnerId.
     */
    public Integer getLearnerId() {
	return learnerId;
    }

    /**
     * @return Returns the lessonId.
     */
    public Long getLessonId() {
	return lessonId;
    }

    /**
     * @return Returns the lessonName.
     */
    public String getLessonName() {
	return lessonName;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
	return userName;
    }

    /**
     * @return Returns the attemptedActivities.
     */
    public int getAttemptedActivities() {
	return attemptedActivities;
    }

    /**
     * @return Returns the activitiesCompleted.
     */
    public int getActivitiesCompleted() {
	return activitiesCompleted;
    }

    /**
     * @return Returns the activityCount.
     */
    public int getActivityCount() {
	return activityCount;
    }

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public boolean getLessonComplete() {
	return lessonComplete;
    }

}
