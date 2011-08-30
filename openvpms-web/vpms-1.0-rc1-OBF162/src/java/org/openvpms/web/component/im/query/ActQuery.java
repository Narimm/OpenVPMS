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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.CollectionHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

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
     * The entity participation short name. May be <tt>null</tt>
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
     * The act status lookups. May be <tt>null</tt>
     */
    private final List<Lookup> statusLookups;

    /**
     * Status to exclude. May be <tt>null</tt>
     */
    private final String excludeStatus;

    /**
     * The status dropdown.
     */
    private SelectField statusSelector;


    /**
     * Constructs a new <tt>ActQuery</tt>.
     *
     * @param shortNames the act short names to query
     * @param statuses   the act statuses to search on. May be
     *                   <tt>empty</tt>
     */
    public ActQuery(String[] shortNames, String[] statuses) {
        this(null, null, null, shortNames, statuses);
    }

    /**
     * Constructs a new <tt>ActQuery</tt>.
     *
     * @param entity        the entity to search for. May be <tt>null</tt>
     * @param participant   the partcipant node name. May be <tt>null</tt>
     * @param participation the entity participation short name. May be <tt>null</tt>
     * @param shortNames    the act short names
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <tt>null</tt>
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
     * Construct a new  <tt>ActQuery</tt>.
     *
     * @param entity        the entity to search for. May be <tt>null</tt>
     * @param participant   the partcipant node name. May be <tt>null</tt>
     * @param participation the entity participation short name. May be
     *                      <tt>null</tt>
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
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
     * @param entity the entity to search for. May be <tt>null</tt>
     */
    public void setEntity(Entity entity) {
        entityId = (entity != null) ? entity.getObjectReference() : null;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automatically;
     *         otherwise <tt>false</tt>
     */
    public boolean isAuto() {
        return false;
    }

    /**
     * Sets the short names which are required to be queried.
     * These are short names that are always queried independent of the
     * short name selector.
     *
     * @param shortNames the short names. May be <tt>null</tt>
     */
    public void setRequiredShortNames(String[] shortNames) {
        requiredShortNames = shortNames;
    }

    /**
     * Sets the status to query on.
     *
     * @param status the status to query on. May be <tt>null</tt>
     */
    public void setStatus(String status) {
        if (status == null) {
            statuses = new String[0];
        } else {
            statuses = new String[]{status};
        }
        updateStatusSelector(status);
    }

    /**
     * Sets the statuses to query on.
     *
     * @param statuses the statuses to query on. May be <tt>null</tt>
     */
    public void setStatuses(String[] statuses) {
        if (statuses == null) {
            this.statuses = new String[0];
        } else {
            this.statuses = statuses;
        }
    }

    /**
     * Performs the query.
     * If constraining acts to a particular entity, the entity must be non-null
     * or a <tt>null</tt> will be returned.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<T> query(SortConstraint[] sort) {
        ResultSet<T> result = null;

        if (participant == null || entityId != null) {
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
    public ShortNameConstraint getArchetypes() {
        ShortNameConstraint result;
        ShortNameConstraint constraint = super.getArchetypes();
        if (requiredShortNames == null) {
            result = constraint;
        } else {
            // need to add the required short names
            String[] shortNames = constraint.getShortNames();
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
    @Override
    protected ShortNameConstraint getArchetypeConstraint() {
        ShortNameConstraint archetypes;
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
     * @return the participant constraint, or <tt>null</tt> if not restricting
     *         to a particular entity
     */
    protected ParticipantConstraint getParticipantConstraint() {
        ParticipantConstraint result = null;
        IMObjectReference entityId = getEntityId();
        if (entityId != null) {
            result = new ParticipantConstraint(participant, participation,
                                               entityId);
        }
        return result;
    }

    /**
     * Returns the archetype constraint, containing a subset of the available
     * short names. This includes the required short names plus a user specified
     * short name.
     *
     * @param shortName the short name
     */
    protected ShortNameConstraint getArchetypeConstraint(String shortName) {
        ShortNameConstraint result;
        if (requiredShortNames == null) {
            result = new ShortNameConstraint(shortName, true, true);
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
     * @return the entity reference. May be <tt>null</tt>
     */
    protected IMObjectReference getEntityId() {
        return entityId;
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or <tt>null</tt> to query all dates
     */
    protected Date getFrom() {
        return null;
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or <tt>null</tt> to query all dates
     */
    protected Date getTo() {
        return null;
    }

    /**
     * Returns the act status lookups.
     *
     * @return the act status lookups. May be <tt>null</tt>
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
     * @return <tt>true</tt> to exclude acts with status in
     *         {@link #getStatuses()} ; otherwise include them.
     */
    protected boolean excludeStatuses() {
        return excludeStatus != null;
    }

    /**
     * Invoked when a status is selected.
     */
    protected void onStatusChanged() {
        String value = (String) statusSelector.getSelectedItem();
        setStatus(value);
    }

    /**
     * Adds a status selector to the container, if status lookups have been
     * specified.
     *
     * @param container the container
     */
    protected void addStatusSelector(Component container) {
        List<Lookup> lookups = getStatusLookups();
        if (lookups != null) {
            LookupListModel model = new LookupListModel(lookups, true);
            statusSelector = SelectFieldFactory.create(model);
            statusSelector.setCellRenderer(new LookupListCellRenderer());
            statusSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onStatusChanged();
                }
            });
            String[] statuses = getStatuses();
            String defaultStatus = null;
            if (statuses.length != 0 && !excludeStatuses()) {
                defaultStatus = statuses[0];
            } else {
                for (Lookup lookup : lookups) {
                    if (lookup.isDefaultLookup()) {
                        defaultStatus = lookup.getCode();
                        break;
                    }
                }
            }
            if (defaultStatus != null) {
                updateStatusSelector(defaultStatus);
                setStatus(defaultStatus);
            }
        }

        if (statusSelector != null) {
            container.add(LabelFactory.create("actquery.status"));
            container.add(statusSelector);
            getFocusGroup().add(statusSelector);
        }
    }

    /**
     * Sets the selected status in the status selector, if it exists.
     *
     * @param status the status to selecte
     */
    private void updateStatusSelector(String status) {
        if (statusSelector != null) {
            LookupListModel model
                    = (LookupListModel) statusSelector.getModel();
            int index = model.indexOf(status);
            if (index != -1) {
                statusSelector.setSelectedIndex(index);
            }
        }
    }

    /**
     * Helper to return a list of status lookups with, with the lookup with
     * the specified code removed.
     *
     * @param lookups the lookups
     * @param code    the code of the lookup to be removed.
     *                May be <tt>null</tt>
     * @return a copy of the source list with the code removed, or the source
     *         list if <tt>code>code> is null
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
