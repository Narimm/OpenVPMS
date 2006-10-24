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
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeLongNameConstraint;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.util.CollectionHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ActQuery extends AbstractQuery<Act> {

    /**
     * The participant node name.
     */
    private final String _participant;

    /**
     * The entity participation short name;
     */
    private final String _participation;

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference _entityId;

    /**
     * Short names which are always queried on.
     */
    private String[] _requiredShortNames;

    /**
     * The statuses to query on.
     */
    private String[] _statuses;

    /**
     * The act status lookups.
     */
    private final List<Lookup> _statusLookups;

    /**
     * Status to exclude. May be <code>null</code>
     */
    private final String _excludeStatus;


    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
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
        _participant = participant;
        _participation = participation;
        _excludeStatus = excludeStatus;
        if (_excludeStatus != null) {
            _statusLookups = new ArrayList<Lookup>(statusLookups);
            for (ListIterator<Lookup> iterator = _statusLookups.listIterator();
                 iterator.hasNext();) {
                Lookup lookup = iterator.next();
                if (lookup.getCode().equals(_excludeStatus)) {
                    iterator.remove();
                }
            }
        } else {
            _statusLookups = statusLookups;
        }
        _statuses = new String[0];
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
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
        _participant = participant;
        _participation = participation;
        _excludeStatus = excludeStatus;
        if (_excludeStatus != null) {
            _statusLookups = new ArrayList<Lookup>(statusLookups);
            for (ListIterator<Lookup> iterator = _statusLookups.listIterator();
                 iterator.hasNext();) {
                Lookup lookup = iterator.next();
                if (lookup.getCode().equals(_excludeStatus)) {
                    iterator.remove();
                }
            }
        } else {
            _statusLookups = statusLookups;
        }
        _statuses = new String[0];
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new <code>ActQuery</code> to query acts for a
     * specific status.
     *
     * @param entity        the entity to search for
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
        _participant = participant;
        _participation = participation;
        _statuses = new String[]{status};
        _statusLookups = null;
        _excludeStatus = null;
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new  <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <code>empty</code>
     */
    public ActQuery(Entity entity, String participant, String participation,
                    String[] shortNames, String[] statuses) {
        super(shortNames);
        setEntity(entity);
        _participant = participant;
        _participation = participation;
        _statuses = statuses;
        _statusLookups = null;
        _excludeStatus = null;
        QueryFactory.initialise(this);
    }

    /**
     * Sets the entity to search for.
     *
     * @param entity the entity to search for. May be <code>null</code>
     */
    public void setEntity(Entity entity) {
        _entityId = (entity != null) ? entity.getObjectReference() : null;
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
        _requiredShortNames = shortNames;
    }

    /**
     * Sets the initial status to query on.
     *
     * @param status the status to query on. May be <code>null</code>
     */
    public void setStatus(String status) {
        if (status == null) {
            _statuses = new String[0];
        } else {
            _statuses = new String[]{status};
        }
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set. May be <code>null</code>
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        ResultSet<Act> result = null;

        if (_entityId != null) {
            result = createResultSet(sort);
        }
        return result;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetype constraint
     */
    @Override
    public BaseArchetypeConstraint getArchetypeConstraint() {
        BaseArchetypeConstraint result;
        BaseArchetypeConstraint archetype = super.getArchetypeConstraint();
        if (_requiredShortNames == null) {
            result = archetype;
        } else {
            // need to add the required short names
            String[] shortNames;
            if (archetype instanceof ArchetypeLongNameConstraint) {
                ArchetypeLongNameConstraint lnc
                        = (ArchetypeLongNameConstraint) archetype;
                shortNames = DescriptorHelper.getShortNames(
                        lnc.getRmName(), lnc.getEntityName(),
                        lnc.getConceptName());
            } else {
                ArchetypeShortNameConstraint snc
                        = (ArchetypeShortNameConstraint) archetype;
                shortNames = snc.getShortNames();
            }
            shortNames = CollectionHelper.concat(shortNames,
                                                 _requiredShortNames);
            result = new ArchetypeShortNameConstraint(
                    shortNames, true, true);
        }
        return result;
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    protected abstract ResultSet<Act> createResultSet(SortConstraint[] sort);

    /**
     * Returns the archetypes to query, based on whether a short name has been
     * selected or not.
     *
     * @return the archetypes to query
     */
    protected BaseArchetypeConstraint getArchetypes() {
        BaseArchetypeConstraint archetypes;
        String type = getShortName();

        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            archetypes = getArchetypeConstraint();
        } else {
            archetypes = getArchetypeConstraint(type);
        }
        return archetypes;
    }

    /**
     * Returns the participant constraint.
     *
     * @return the participant constraint
     */
    protected ParticipantConstraint getParticipantConstraint() {
        return new ParticipantConstraint(_participant, _participation,
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
        if (_requiredShortNames == null) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortName);
            ArchetypeId id = new ArchetypeId(archetype.getName());
            result = new ArchetypeLongNameConstraint(
                    null, id.getEntityName(), id.getConcept(), true, true);
        } else {
            String[] shortNames = CollectionHelper.concat(_requiredShortNames,
                                                          shortName);
            result = new ArchetypeShortNameConstraint(
                    shortNames, true, true);
        }
        return result;
    }

    /**
     * Returns the entity reference.
     *
     * @return the entity reference
     */
    protected IMObjectReference getEntityId() {
        return _entityId;
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
        return _statusLookups;
    }

    /**
     * Returns the act statuses to query.
     *
     * @return the act statuses to query
     */
    protected String[] getStatuses() {
        String[] statuses;
        if (_excludeStatus != null) {
            statuses = new String[]{_excludeStatus};
        } else {
            statuses = _statuses;
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
        return _excludeStatus != null;
    }

}
