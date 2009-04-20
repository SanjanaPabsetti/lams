/****************************************************************
 * Copyright (C) 2008 LAMS Foundation (http://lamsfoundation.org)
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
 
/* $Id$ */ 
package org.lamsfoundation.lams.gradebook.dto; 

import java.util.ArrayList;

import org.lamsfoundation.lams.gradebook.util.GBGridView;
 
public abstract class GradebookGridRowDTO{
    
    public abstract ArrayList<String> toStringArray(GBGridView view);
    
    public static final String CELL_EMPTY = "-";
    
    // The id for a row, might be activityId, userId, lessonID etc
    public String id;
    
    // The name for the row, every gradebook row has some sort of name
    // Be it the user's name, the activity name, the lesson name etc
    public String rowName;
 
    // A unit of time in milliseconds that  determines the time taken for the corressponding task
    public Long timeTaken;
    
    // Another unit of time that represents average time taken for a corresponding task
    public Long averageTimeTaken;
    
    // The mark for the corresponding gradebook grid row task
    public Double mark;
    
    // Average mark for the corresponding task
    public Double averageMark;

    /**
     * A shared function to convert milliseconds into a readable string
     * 
     * @param timeInMillis
     * @return
     */
    protected String convertTimeToString(Long timeInMillis) {
	StringBuilder sb = new StringBuilder();
	if (timeInMillis != null && timeInMillis > 1000) {
	    long totalTimeInSeconds = timeInMillis / 1000;

	    long seconds = (totalTimeInSeconds >= 60 ? totalTimeInSeconds % 60 : totalTimeInSeconds); 
	    long minutes = (totalTimeInSeconds = (totalTimeInSeconds / 60)) >= 60 ? totalTimeInSeconds % 60 : totalTimeInSeconds; 
	    long hours = (totalTimeInSeconds = (totalTimeInSeconds / 60)) >= 24 ? totalTimeInSeconds % 24 : totalTimeInSeconds; 
	    long days = (totalTimeInSeconds = (totalTimeInSeconds / 24)); 

	    if (days != 0 ) { sb.append("" + days + "d, "); }
	    if (hours != 0 ) { sb.append("" + hours + "h, "); }
	    if (minutes != 0 ) { sb.append("" + minutes + "m, "); }
	    if (seconds != 0 ) { sb.append("" + seconds + "s"); }
	}
	
	if (sb.length() > 0) {
	    return sb.toString();
	} else {
	    return null;
	}
    }
    
    protected String toItalic(String string) {
	return "<i>" + string + "</i>";
    }

    public Long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Double getMark() {
        return mark;
    }

    public void setMark(Double mark) {
        this.mark = mark;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public Long getAverageTimeTaken() {
        return averageTimeTaken;
    }

    public void setAverageTimeTaken(Long averageTimeTaken) {
        this.averageTimeTaken = averageTimeTaken;
    }

    public Double getAverageMark() {
        return averageMark;
    }

    public void setAverageMark(Double averageMark) {
        this.averageMark = averageMark;
    }
    
    
}
 
