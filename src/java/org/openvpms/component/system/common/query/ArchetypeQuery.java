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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Create a query against an archetype.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeQuery extends AbstractArchetypeQuery
        implements IConstraintContainer {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * indicates whether to select distinct results only.
     */
    private boolean distinct;

    /**
     * Indicates whether to search only for active entities.
     */
    private boolean activeOnly = false;

    /**
     * Define the {@link BaseArchetypeConstraint}. Mandatory.
     */
    private BaseArchetypeConstraint archetypeConstraint;


    /**
     * Construct a query with the specified constraint.
     *
     * @param constraint the constraint to use
     */
    public ArchetypeQuery(BaseArchetypeConstraint constraint) {
        try {
            archetypeConstraint = (BaseArchetypeConstraint) constraint.clone();
        } catch (CloneNotSupportedException exception) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.CloneNotSupported,
                    exception, constraint.getClass()
            );
        }
    }

    /**
     * Create a query for the specified archetype id.
     *
     * @param archetypeId a valid archetype identity
     */
    public ArchetypeQuery(ArchetypeId archetypeId) {
        this(archetypeId, false);
    }

    /**
     * Create a query for the specified archetype id.
     *
     * @param archetypeId a valid archetype identity
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    public ArchetypeQuery(ArchetypeId archetypeId, boolean activeOnly) {
        archetypeConstraint = new ArchetypeIdConstraint(archetypeId,
                                                        activeOnly);
    }

    /**
     * Create an instance of this query specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card character
     *
     * @param rmName      the reference model name (optional)
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly only deal with the primary archetypes
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    @Deprecated
    public ArchetypeQuery(String rmName, String entityName,
                          String conceptName, boolean primaryOnly,
                          boolean activeOnly) {
        this(entityName, conceptName, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this query specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card
     * character.
     *
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly only deal with the primary archetypes
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    public ArchetypeQuery(String entityName, String conceptName,
                          boolean primaryOnly, boolean activeOnly) {
        StringBuffer shortName = new StringBuffer();
        if (entityName != null) {
            shortName.append(entityName);
        } else {
            shortName.append("*");
        }
        shortName.append(".");
        if (conceptName != null) {
            shortName.append(conceptName);
        } else {
            shortName.append("*");
        }
        archetypeConstraint = new ShortNameConstraint(shortName.toString(),
                primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this constraint with the specified short name,
     * for primary/non-primary archetypes and active/inactive objects.
     *
     * @param shortName   the short name
     */
    public ArchetypeQuery(String shortName) {
        this(shortName, false);
    }

    /**
     * Create an instance of this constraint with the specified short name,
     * for primary/non-primary archetypes and active/inactive objects.
     *
     * @param shortName   the short name
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    public ArchetypeQuery(String shortName, boolean activeOnly) {
        this(shortName, false, activeOnly);
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param shortName   the short name
     * @param primaryOnly only deal with the primary archetypes
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    public ArchetypeQuery(String shortName, boolean primaryOnly, boolean activeOnly) {
        archetypeConstraint = new ShortNameConstraint(shortName, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this class with the specified archetype short names.
     *
     * @param shortNames  an array of archetype short names
     * @param primaryOnly only deal with the primary archetypes
     * @param activeOnly  if <tt>true</tt> only return active objects
     */
    public ArchetypeQuery(String[] shortNames, boolean primaryOnly, boolean activeOnly) {
        archetypeConstraint = new ShortNameConstraint(shortNames, primaryOnly, activeOnly);
    }

    /**
     * Create a query based on the specified {@link IMObjectReference}.
     *
     * @param reference the object reference
     */
    public ArchetypeQuery(IMObjectReference reference) {
        archetypeConstraint = new ObjectRefConstraint(reference);
    }

    /**
     * Sets the first result.
     *
     * @param firstResult the first result
     * @return this query
     */
    @Override
    public ArchetypeQuery setFirstResult(int firstResult) {
        super.setFirstResult(firstResult);
        return this;
    }

    /**
     * Sets the maximum number of results to retrieve.  If not set,
     * there is no limit to the number of results retrieved.
     *
     * @param maxResults the maximum no. of results to retrieve
     * @return this query
     */
    @Override
    public ArchetypeQuery setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }

    /**
     * Determines if the total no. of results should be counted and returned
     * in the resulting {@link IPage}.
     *
     * @param count if <code>true</code> count the no. of results
     * @return this query
     */
    @Override
    public ArchetypeQuery setCountResults(boolean count) {
        super.setCountResults(count);
        return this;
    }

    /**
     * Determines if only active objects should be returned.
     *
     * @return <code>true</code> if only active objects will be returned;
     *         otherwise both active and inactive objects will be returned
     */
    public boolean isActiveOnly() {
        return activeOnly;
    }

    /**
     * Determines if only active objects should be returned.
     *
     * @param activeOnly if <code>true</code> only active objects will be
     *                   returned; otherwise both active and inactive objects
     *                   will be returned
     * @return this query
     */
    public ArchetypeQuery setActiveOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
        return this;
    }

    /**
     * Determines if duplicate results should be excluded.
     *
     * @return <code>true</code> if duplicate results should be excluded.
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Determines if duplicate results should be excluded. If not set,
     * any duplicates will be returned.
     *
     * @param distinct if <code>true</code> exclude duplicate results
     * @return this query
     */
    public ArchetypeQuery setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetypeConstraint.
     */
    public BaseArchetypeConstraint getArchetypeConstraint() {
        return archetypeConstraint;
    }

    /**
     * Adds the specified constraint to the query.
     *
     * @param constraint the constraint to add
     * @return this query
     */
    public ArchetypeQuery add(IConstraint constraint) {
        archetypeConstraint.add(constraint);
        return this;
    }

    /**
     * Remove the specified constraint from query.
     *
     * @param constraint the constraint to remove
     * @return this query
     */
    public ArchetypeQuery remove(IConstraint constraint) {
        archetypeConstraint.remove(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("constraints", archetypeConstraint)
                .toString();
    }

}
