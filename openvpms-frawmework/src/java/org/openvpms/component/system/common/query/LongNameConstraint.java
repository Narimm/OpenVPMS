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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class allows the user to specify the different parts of an archetype
 * long name (except for version). The client can specify the reference mode
 * name, entity name and concept name, either as complete names or with wild
 * card character.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @deprecated
 */
@Deprecated
public class LongNameConstraint extends BaseArchetypeConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The reference model name.
     */
    private String rmName;

    /**
     * The entity name.
     */
    private String entityName;

    /**
     * The concept name.
     */
    private String conceptName;


    /**
     * Create an instance of this constraint specifying one or more elements.
     * Any of the parameters can be null or may include the wild card character
     *
     * @param rmName      the reference model name (optional)
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly restrict the search to primary archetypes
     * @param activeOnly  restrict to active only entities
     */
    public LongNameConstraint(String rmName, String entityName,
                              String conceptName, boolean primaryOnly,
                              boolean activeOnly) {
        this(null, rmName, entityName, conceptName, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this constraint specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card character
     *
     * @param alias       the type name alias. May be <code>null</code>
     * @param rmName      the reference model name (optional)
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly restrict the search to primary archetypes
     * @param activeOnly  restrict to active only entities
     */
    public LongNameConstraint(String alias, String rmName, String entityName,
                              String conceptName, boolean primaryOnly,
                              boolean activeOnly) {
        super(alias, primaryOnly, activeOnly);

        if (StringUtils.isEmpty(rmName) &&
                StringUtils.isEmpty(entityName) &&
                StringUtils.isEmpty(conceptName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.InvalidLongNameConstraint,
                    rmName, entityName, conceptName);
        }

        this.rmName = rmName;
        this.entityName = entityName;
        this.conceptName = conceptName;
    }

    /**
     * Returns the archetype concept name.
     *
     * @return the concept name. May be <code>null</code>
     */
    public String getConceptName() {
        return conceptName;
    }

    /**
     * Sets the archetype concept name.
     *
     * @param conceptName the concept name. May be <code>null</code>
     */
    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the entity name. May be <code>null</code>
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Sets the archetype entity name.
     *
     * @param entityName the entityName. May be <code>null</code>
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the archetype reference model name.
     *
     * @return the reference model name. May be <code>null</code>.
     */
    public String getRmName() {
        return rmName;
    }

    /**
     * Sets the archetype reference model name.
     *
     * @param rmName the reference model name. May be <code>hull</code>
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

        if (!(obj instanceof LongNameConstraint)) {
            return false;
        }

        LongNameConstraint rhs = (LongNameConstraint) obj;
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

}
