package org.lamsfoundation.lams.learningdesign;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.lamsfoundation.lams.lesson.LessonClass;


/**
 *        @hibernate.class
 *         table="lams_grouping"
 *
 */
public abstract class Grouping implements Serializable
{
    /** Grouping type id of random grouping */
    public static final Integer RANDOM_GROUPING_TYPE = new Integer(1);
    
    /** Grouping type id of chosen grouping */
    public static final Integer CHOSEN_GROUPING_TYPE = new Integer(2);
    
    /** Grouping type id for lesson class grouping */
    public static final Integer CLASS_GROUPING_TYPE = new Integer(3);
    
    /** identifier field */
    private Long groupingId;
    
    /** nullable persistent field */
    private Integer maxNumberOfGroups;
    
   
    /** persistent field */
    private Set groups;
    
    /** persistent field */
    private Set activities;
    
    /** non-persistent field */
    private Set learners;
    
    /** full constructor */
    public Grouping(Long groupingId, Set groups, Set activities)
    {
        this.groupingId = groupingId;
        this.groups = groups;
        this.activities = activities;
    }
    
    /** default constructor */
    public Grouping()
    {
    }
    
    /** minimal constructor */
    public Grouping(Long groupingId)
    {
        this.groupingId = groupingId;
    }
    
    /**
     *            
     *
     */
    public Long getGroupingId()
    {
        return this.groupingId;
    }
    
    public void setGroupingId(Long groupingId)
    {
        this.groupingId = groupingId;
    }
    
    
    
    /**
     *            
     *
     */
    public Integer getGroupingTypeId()
    {
        if(this instanceof LessonClass)
            return CLASS_GROUPING_TYPE;
        else if(this instanceof ChosenGrouping)
            return CHOSEN_GROUPING_TYPE;
        else
            return RANDOM_GROUPING_TYPE;
    }

    /**
     *            
     *
     */
    public Set getGroups()
    {
        return this.groups;
    }
    
    public void setGroups(Set groups)
    {
        this.groups = groups;
    }
    
    /**
     *           
     *
     */
    public Set getActivities()
    {
        return this.activities;
    }
    
    public void setActivities(Set activities)
    {
        this.activities = activities;
    }
    
    public String toString()
    {
        return new ToStringBuilder(this)
        .append("groupingId", getGroupingId())
        .toString();
    }
    
    public boolean equals(Object other)
    {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Grouping) ) return false;
        Grouping castOther = (Grouping) other;
        return new EqualsBuilder()
        .append(this.getGroupingId(), castOther.getGroupingId())
        .isEquals();
    }
    
    public int hashCode()
    {
        return new HashCodeBuilder()
        .append(getGroupingId())
        .toHashCode();
    }
    /**
     * 
     */
    public Integer getMaxNumberOfGroups()
    {
        return maxNumberOfGroups;
    }
    /**
     * @param maxNumberOfGroups The maxNumberOfGroups to set.
     */
    public void setMaxNumberOfGroups(Integer maxNumberOfGroups)
    {
        this.maxNumberOfGroups = maxNumberOfGroups;
    }
    
    /**
     * Return the next group order id.
     * @return the next order id.
     */
    public synchronized int getNextGroupOrderId()
    {
        int order =0;
        if(this.groups!=null)
        {
            order = groups.size();
            return ++order;
        }
        else return ++order;
    }
    
    /**
     * Return all the learners who participate this grouping.
     * @return the learners set.
     */
    public Set getLearners()
    {
        //verify pre-condition
        if(groups==null)
            throw new IllegalArgumentException("Fail to get learnings from" +
            		"a grouping without groups");
        
        learners = new HashSet();
        for(Iterator i = groups.iterator();i.hasNext();)
        {
            Group group = (Group)i.next();
            learners.addAll(group.getUsers());
        }
        return learners;
    }
}
