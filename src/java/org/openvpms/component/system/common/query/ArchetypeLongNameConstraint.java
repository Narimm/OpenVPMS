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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class allows the user to specify the different parts of an archetype
 * long name (except for version). The client can specify the reference mode
 * name, entity name and concept name, either as complete names or with wild 
 * card character.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeLongNameConstraint extends ArchetypeConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The reference model name
     */
    private String rmName;
    
    /**
     * The entity name
     */
    private String entityName;
    
    /**
     * The concept name
     */
    private String conceptName;

    /**
     * Create an instance of this constraint specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card character
     *  
     * @param rmName 
     *            the reference model name (optional)
     * @param entityName
     *            the entity name (optional)         
     * @param conceptName
     *            the concept name (optional) 
     * @param  primaryOmly
     *            restrict the search to primary archetypes
     * @param activeOnly
     *            restrict to active only entities                                                  
     */
    public ArchetypeLongNameConstraint(String rmName, String entityName, 
            String conceptName, boolean primaryOnly, boolean activeOnly) {
        super(primaryOnly, activeOnly);
        
        if (StringUtils.isEmpty(rmName) &&
            StringUtils.isEmpty(entityName) &&
            StringUtils.isEmpty(conceptName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.InvalidLongNameConstraint,
                    new Object[] { rmName, entityName, conceptName});
        }
        
        this.rmName = rmName;
        this.entityName = entityName;
        this.conceptName = conceptName;
    }

    /**
     * @return Returns the conceptName.
     */
    public String getConceptName() {
        return conceptName;
    }

    /**
     * @param conceptName The conceptName to set.
     */
    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    /**
     * @return Returns the entityName.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName The entityName to set.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return Returns the rmName.
     */
    public String getRmName() {
        return rmName;
    }

    /**
     * @param rmName The rmName to set.
     */
    public void setRmName(String rmName) {
        this.rmName = rmName;
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
        
        ArchetypeLongNameConstraint rhs = (ArchetypeLongNameConstraint) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(rhs))
            .append(rmName, rhs.rmName)
            .append(entityName, rhs.entityName)
            .append(conceptName, rhs.conceptName)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("rmName", rmName)
            .append("entityName", entityName)
            .append("conceptName", conceptName)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeLongNameConstraint copy = (ArchetypeLongNameConstraint)super.clone();
        copy.rmName = this.rmName;
        copy.entityName = this.entityName;
        copy.conceptName = this.conceptName;
        
        return copy;
    }
}
