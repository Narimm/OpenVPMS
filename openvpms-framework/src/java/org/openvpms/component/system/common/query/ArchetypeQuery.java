/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.common.query;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Create a query against an archetype
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeQuery implements IConstraintContainer {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L; 

    /**
     * Indicates that all the rows should be returned
     */
    public final static int ALL_ROWS = -1;

    /**
     * Define the first row to be retrieve if paging is being used
     */
    private int firstRow = 0;
    
    /**
     * Define the maximum number of rows to be retrieve
     */
    private int numOfRows = ALL_ROWS;
    
    /**
     * indicates whether to select distinct rows only
     */
    private boolean distinct;
    
    /**
     * Indicates whether to search only for active entities
     */
    private boolean activeOnly = false;
    
    /**
     * Define the {@link BaseArchetypeConstraint}. mandatory.
     */
    private BaseArchetypeConstraint archetypeConstraint;
    
    /**
     * Construct a query with the specified constraint
     * 
     * @param constraint
     *            the constraint to use
     */
    public ArchetypeQuery(BaseArchetypeConstraint constraint) {
        try {
            this.archetypeConstraint = (BaseArchetypeConstraint)constraint.clone();
        } catch (CloneNotSupportedException exception) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.CloneNotSupported,
                            new Object[] {constraint.getClass()},
                            exception);
        }
            
    }

    /**
     * Create a query for the specified archetype id
     * 
     * @param archetypeId 
     *            a valid archetype identity
     */
    public ArchetypeQuery(ArchetypeId archetypeId) {
        this(archetypeId, false);
    }
    
    /**
     * Create a query for the specified archetype id
     * 
     * @param archetypeId 
     *            a valid archetype identity
     * @param activeOnly
     *            constraint to active only objects            
     */
    public ArchetypeQuery(ArchetypeId archetypeId, boolean activeOnly) {
        this.archetypeConstraint = new ArchetypeIdConstraint(archetypeId, activeOnly);
    }
    
    /**
     * Create an instance of this query specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card character
     *  
     * @param rmName  
     *            the reference model name (optional)
     * @param entityName
     *            the entity name (optional)         
     * @param conceptName
     *            the concept name (optional)
     * @param primaryOnly
     *            only deal with the primary archetypes                          
     * @param activeOnly
     *            constraint to active only objects            
     */
    public ArchetypeQuery(String rmName, String entityName, 
            String conceptName, boolean primaryOnly, boolean activeOnly) {
        this.archetypeConstraint = new ArchetypeLongNameConstraint(rmName, 
                entityName, conceptName, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this constraint with the specified short name
     * 
     * @param shortName
     *            the short name
     * @param primaryOnly
     *            only deal with the primary archetypes                          
     * @param activeOnly
     *            constraint to active only objects            
     */
    public ArchetypeQuery(String shortName, boolean primaryOnly, boolean activeOnly) {
        this.archetypeConstraint = new ArchetypeShortNameConstraint(
                shortName, primaryOnly, activeOnly);
    }
    
    /**
     * Create an instance of this class with the specified archetype
     * short names
     * 
     * @param shortNames
     *            an array of archetype short names
     * @param primaryOnly
     *            only deal with the primary archetypes                          
     * @param activeOnly
     *            constraint to active only objects            
     */
    public ArchetypeQuery(String[] shortNames, boolean primaryOnly,
            boolean activeOnly) {
        this.archetypeConstraint = new ArchetypeShortNameConstraint(
                shortNames, primaryOnly, activeOnly);
    }
    
    /**
     * Create a query based on the specified {@link org.openvpms.component.business.domain.im.common.IMObjectReference}.
     * 
     * @param reference
     *            the object reference
     */
    public ArchetypeQuery(IMObjectReference reference) {
        this.archetypeConstraint = new ObjectRefArchetypeConstraint(reference);
    }
    
    /**
     * @return Returns the firstRow.
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * @param firstRow The firstRow to set.
     * @return ArchetypeQuery
     */
    public ArchetypeQuery setFirstRow(int firstRow) {
        this.firstRow = firstRow;
        return this;
    }

    /**
     * @return Returns the numOfRows.
     */
    public int getNumOfRows() {
        return numOfRows;
    }

    /**
     * @param numOfRows The numOfRows to set.
     */
    public ArchetypeQuery setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
        return this;
    }

    /**
     * @return Returns the activeOnly.
     */
    public boolean isActiveOnly() {
        return activeOnly;
    }

    /**
     * @param activeOnly The activeOnly to set.
     */
    public void setActiveOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

    /**
     * @return Returns the distinct.
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * @param distinct The distinct to set.
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * @return Returns the archetypeConstraint.
     */
    public BaseArchetypeConstraint getArchetypeConstraint() {
        return archetypeConstraint;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#add(org.openvpms.component.system.common.query.IConstraint)
     */
    public ArchetypeQuery add(IConstraint constraint) {
        this.archetypeConstraint.add(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#remove(org.openvpms.component.system.common.query.IConstraint)
     */
    public ArchetypeQuery remove(IConstraint constraint) {
        this.archetypeConstraint.remove(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeLongNameConstraint)) {
            return false;
        }
        
        ArchetypeQuery rhs = (ArchetypeQuery) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(rhs))
            .append(firstRow, rhs.firstRow)
            .append(numOfRows, rhs.numOfRows)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("firstRow", firstRow)
            .append("numOfRows", numOfRows)
            .append("constraints", archetypeConstraint)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeQuery copy = (ArchetypeQuery)super.clone();
        copy.firstRow = this.firstRow;
        copy.numOfRows = this.numOfRows;
        
        return copy;
    }
}
