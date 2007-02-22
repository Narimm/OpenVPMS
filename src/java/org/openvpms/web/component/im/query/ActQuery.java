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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.CollectionHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ActQuery<T> extends AbstractQuery<T> {

    /**
     * The participant node name.
     */
    private final String participant;

    /**
     * The entity participation short name;
     */
    private final String participation;

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference entityId;

    /**
     * Short names which are always queried on.
     */
    private String[] requiredShortNames;

    /**
     * The statuses to query on.
     */
    private String[] statuses;

    /**
     * The act status lookups.
     */
    private final List<Lookup> statusLookups;

    /**
     * Status to exclude. May be <code>null</code>
     */
    private final String excludeStatus;


    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for. May be <code>null</code>
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     */
    public ActQuery(Entity entity, String participant, String participation,
                    String entityName, String conceptName,
                    List<Lookup> statusLookups, String excludeStatus) {
        super(null, entityName, conceptName);
        setEntity(entity);
        this.participant = participant;
        this.participation = participation;
        this.excludeStatus = excludeStatus;
        this.statusLookups = getStatusLookups(statusLookups, excludeStatus);
        statuses = new String[0];
    }

    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for. May be <code>null</code>
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     */
    public ActQuery(Entity entity, String participant, String participation,
                    String[] shortNames, List<Lookup> statusLookups,
                    String excludeStatus) {
        super(shortNames);
        setEntity(entity);
        this.participant = participant;
        this.participation = participation;
        this.excludeStatus = excludeStatus;
        this.statusLookups = getStatusLookups(statusLookups, excludeStatus);
        statuses = new String[0];
    }

    /**
     * Construct a new <code>ActQuery</code> to query acts for a
     * specific status.
     *
     * @param entity        the entity to search for. May be <code>null</code>
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param status        the act status
     */
    public ActQuery(Entity entity, String participant, String participation,
                    String entityName, String conceptName, String status) {
        super(null, entityName, conceptName);
        setEntity(entity);
        this.participant = participant;
        this.participation = participation;
        statuses = new String[]{status};
        statusLookups = null;
        excludeStatus = null;
    }

    /**
     * Construct a new  <code>ActQuery</code>.
     *
     * @param entity        the entity to search for. May be <code>null</code>
     * @param participant   the participant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <code>empty</code>
     */
    public ActQuery(Entity entity, String participant, String participation,
                    String[] shortNames, String[] statuses) {
        super(shortNames);
        setEntity(entity);
        this.participant = participant;
        this.participation = participation;
        this.statuses = statuses;
        statusLookups = null;
        excludeStatus = null;
    }

    /**
     * Sets the entity to search for.
     *
     * @param entity the entity to search for. May be <code>null</code>
     */
    public void setEntity(Entity entity) {
        entityId = (entity != null) ? entity.getObjectReference() : null;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    public boolean isAuto() {
        return false;
    }

    /**
     * Sets the short names which are required to be queried.
     * These are short names that are always queried independent of the
     * short name selector.
     *
     * @param shortNames the short names. May be <code>null</code>
     */
    public void setRequiredShortNames(String[] shortNames) {
        requiredShortNames = shortNames;
    }

    /**
     * Sets the initial status to query on.
     *
     * @param status the status to query on. May be <code>null</code>
     */
    public void setStatus(String status) {
        if (status == null) {
            statuses = new String[0];
        } else {
            statuses = new String[]{status};
        }
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set. May be <code>null</code>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<T> query(SortConstraint[] sort) {
        ResultSet<T> result = null;

        if (entityId != null) {
            result = createResultSet(sort);
        }
        return result;
    }

    /**
     * Returns the archetypes to select from.
     *
     * @return the archetypes to select from
     */
    @Override
    public BaseArchetypeConstraint getArchetypes() {
        BaseArchetypeConstraint result;
        BaseArchetypeConstraint archetype = super.getArchetypes();
        if (requiredShortNames == null) {
            result = archetype;
        } else {
            // need to add the required short names
            String[] shortNames;
            if (archetype instanceof LongNameConstraint) {
                LongNameConstraint lnc = (LongNameConstraint) archetype;
                shortNames = DescriptorHelper.getShortNames(
                        lnc.getRmName(), lnc.getEntityName(),
                        lnc.getConceptName());
            } else {
                ShortNameConstraint snc = (ShortNameConstraint) archetype;
                shortNames = snc.getShortNames();
            }
            shortNames = CollectionHelper.concat(shortNames,
                                                 requiredShortNames);
            result = new ShortNameConstraint(shortNames, true, true);
        }
        return result;
    }

    /**
     * Returns the archetypes to query, based on whether a short name has been
     * selected or not. The constraint alias is set to 'act'.
     *
     * @return the archetypes to query
     */
    protected BaseArchetypeConstraint getArchetypeConstraint() {
        BaseArchetypeConstraint archetypes;
        String type = getShortName();

        if (type == null || type.equals(ShortNameListModel.ALL)) {
            archetypes = getArchetypes();
        } else {
            archetypes = getArchetypeConstraint(type);
        }
        archetypes.setAlias("act");
        return archetypes;
    }

    /**
     * Returns the participant constraint.
     *
     * @return the participant constraint
     */
    protected ParticipantConstraint getParticipantConstraint() {
        return new ParticipantConstraint(participant, participation,
                                         getEntityId());
    }

    /**
     * Returns the archetype constraint, containing a subset of the available
     * short names. This includes the required short names plus a user specified
     * short name.
     *
     * @param shortName the short name
     */
    protected BaseArchetypeConstraint getArchetypeConstraint(String shortName) {
        BaseArchetypeConstraint result;
        if (requiredShortNames == null) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortName);
            ArchetypeId id = new ArchetypeId(archetype.getName());
            result = new LongNameConstraint(
                    null, id.getEntityName(), id.getConcept(), true, true);
        } else {
            String[] shortNames = CollectionHelper.concat(requiredShortNames,
                                                          shortName);
            result = new ShortNameConstraint(shortNames, true, true);
        }
        return result;
    }

    /**
     * Returns the entity reference.
     *
     * @return the entity reference
     */
    protected IMObjectReference getEntityId() {
        return entityId;
    }

    /**
     * Returns the start-from date.
     *
     * @return the start-from date, or <code>null</code> to query all dates
     */
    protected Date getStartFrom() {
        return null;
    }

    /**
     * Returns the start-to date.
     *
     * @return the start-to date, or <code>null</code> to query all dates
     */
    protected Date getStartTo() {
        return null;
    }

    /**
     * Returns the act status lookups.
     *
     * @return the act status lookups. May be <code>null</code>
     */
    protected List<Lookup> getStatusLookups() {
        return statusLookups;
    }

    /**
     * Returns the act statuses to query.
     *
     * @return the act statuses to query
     */
    protected String[] getStatuses() {
        String[] statuses;
        if (excludeStatus != null) {
            statuses = new String[]{excludeStatus};
        } else {
            statuses = this.statuses;
        }
        return statuses;
    }

    /**
     * Determines if act statuses are being excluded.
     *
     * @return <code>true</code> to exclude acts with status in
     *         {@link #getStatuses()} ; otherwise include them.
     */
    protected boolean excludeStatuses() {
        return excludeStatus != null;
    }

    /**
     * Helper to return a list of status lookups with, with the lookup with
     * the specified code removed.
     *
     * @param lookups the lookups
     * @param code    the code of the lookup to be removed.
     *                May be <code>null</code>
     * @return a copy of the source list with the code removed, or the source
     *         list if <code>code>code> is null
     */
    private List<Lookup> getStatusLookups(List<Lookup> lookups,
                                          String code) {
        List<Lookup> result;
        if (code != null) {
            result = new ArrayList<Lookup>(lookups);
            for (Iterator<Lookup> i = result.listIterator(); i.hasNext();) {
                Lookup lookup = i.next();
                if (lookup.getCode().equals(code)) {
                    i.remove();
                    break;
                }
            }
        } else {
            result = lookups;
        }
        return result;
    }
}
