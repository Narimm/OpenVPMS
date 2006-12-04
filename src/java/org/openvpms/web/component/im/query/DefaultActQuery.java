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

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultActQuery extends ActQuery<Act> {

    /**
     * Determines if acts should be filtered on type.
     */
    private final boolean _selectType;

    /**
     * The status dropdown.
     */
    private SelectField _statusSelector;

    /**
     * Indicates if all start dates should be queried. If so, the startFrom and
     * startTo dates are ignored.
     */
    private CheckBox _startAll;

    /**
     * The start-from label.
     */
    private Label _startFromLabel;

    /**
     * The start-from date.
     */
    private DateField _startFrom;

    /**
     * The start-to label.
     */
    private Label _startToLabel;

    /**
     * The start-to date.
     */
    private DateField _startTo;

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * Row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Construct a new <code>DefaultActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation,
                           String entityName, String conceptName,
                           List<Lookup> statusLookups) {
        this(entity, participant, participation, entityName, conceptName,
             statusLookups, null);
    }

    /**
     * Construct a new <code>DefaultActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String entityName,
                           String conceptName, List<Lookup> statusLookups,
                           String excludeStatus) {
        super(entity, participant, participation, entityName, conceptName,
              statusLookups, excludeStatus);
        _selectType = false;
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new <code>DefaultActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] shortNames,
                           List<Lookup> statusLookups, String excludeStatus) {
        super(entity, participant, participation, shortNames, statusLookups,
              excludeStatus);
        _selectType = true;
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new <code>DefaultActQuery</code> to query acts for a specific
     * status.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param status        the act status
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String entityName,
                           String conceptName, String status) {
        super(entity, participant, participation, entityName, conceptName,
              status);
        _selectType = true;
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new  <code>DefaultActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <code>empty</code>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] shortNames,
                           String[] statuses) {
        super(entity, participant, participation, shortNames, statuses);
        _selectType = true;
        QueryFactory.initialise(this);
    }

    /**
     * Sets the initial status to query on.
     *
     * @param status the status to query on
     */
    @Override
    public void setStatus(String status) {
        super.setStatus(status);
        updateStatusSelector(status);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        if (_selectType) {
            addShortNameSelector(container);
        }

        List<Lookup> lookups = getStatusLookups();
        if (lookups != null) {
            LookupListModel model = new LookupListModel(lookups, true);
            _statusSelector = SelectFieldFactory.create(model);
            _statusSelector.setCellRenderer(new LookupListCellRenderer());
            _statusSelector.addActionListener(new ActionListener() {
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

        _startAll = CheckBoxFactory.create("actquery.all", true);
        _startAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStartAllChanged();
            }
        });

        _startFromLabel = LabelFactory.create("actquery.from");
        _startFrom = DateFieldFactory.create();
        _startFrom.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onStartFromChanged();
                    }
                });

        _startToLabel = LabelFactory.create("actquery.to");
        _startTo = DateFieldFactory.create();
        _startTo.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onStartToChanged();
                    }
                });
        Row startRange = RowFactory.create(
                CELLSPACING_STYLE,
                _startFromLabel, _startFrom,
                _startToLabel, _startTo);
        Row startRow = RowFactory.create(ROW_STYLE, _startAll, startRange);

        onStartAllChanged();

        if (_statusSelector != null) {
            container.add(LabelFactory.create("actquery.status"));
            container.add(_statusSelector);
        }
        container.add(startRow);
    }

    /**
     * Invoked when a status is selected.
     */
    private void onStatusChanged() {
        String value = (String) _statusSelector.getSelectedItem();
        super.setStatus(value);
    }

    /**
     * Invoked when the start-all check box changes.
     */
    private void onStartAllChanged() {
        boolean enabled = !_startAll.isSelected();
        enable(_startFromLabel, enabled);
        enable(_startFrom, enabled);
        enable(_startToLabel, enabled);
        enable(_startTo, enabled);
    }

    /**
     * Returns the start-from date.
     *
     * @return the start-from date
     */
    protected Date getStartFrom() {
        return _startAll.isSelected() ? null : getDate(_startFrom);
    }

    /**
     * Returns the start-to date.
     *
     * @return the start-to date
     */
    protected Date getStartTo() {
        return _startAll.isSelected() ? null : getDate(_startTo);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        return new ActResultSet(getParticipantConstraint(),
                                getArchetypeConstraint(),
                                getStartFrom(), getStartTo(), getStatuses(),
                                excludeStatuses(), getConstraints(),
                                getMaxResults(), sort);
    }


    /**
     * Enable/disable a component.
     *
     * @param component the component to update
     * @param enabled   if <code>true</code> enable the component; otherwise
     *                  disable it
     */
    private void enable(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (enabled) {
            component.setForeground(Color.BLACK);
        } else {
            component.setForeground(Color.LIGHTGRAY);
        }
    }

    /**
     * Enable/disable a date field.
     *
     * @param field   the field to update
     * @param enabled if <code>true</code> enable the field; otherwise disable
     *                it
     */
    private void enable(DateField field, boolean enabled) {
        enable(field.getTextField(), enabled);
        field.getDateChooser().setEnabled(enabled);
        field.setEnabled(enabled);
        if (!enabled) {
            field.setExpanded(false);
        }
    }

    /**
     * Sets the start-from date.
     *
     * @param date the start-from date
     */
    private void setStartFrom(Date date) {
        setDate(_startFrom, date);
    }

    /**
     * Sets the start-to date.
     *
     * @param date the start-to date
     */
    private void setStartTo(Date date) {
        setDate(_startTo, date);
    }

    /**
     * Invoked when the start-from date changes. Sets the start-to date =
     * start-from if start-from is greater.
     */
    private void onStartFromChanged() {
        Date from = getStartFrom();
        Date to = getStartTo();
        if (from != null && from.compareTo(to) > 0) {
            setStartTo(from);
        }
    }

    /**
     * Invoked when the start-to date changes. Sets the start-from date =
     * start-to if start-from is greater.
     */
    private void onStartToChanged() {
        Date from = getStartFrom();
        Date to = getStartTo();
        if (from != null && from.compareTo(to) > 0) {
            setStartFrom(to);
        }
    }

    /**
     * Returns the date of the given field.
     *
     * @param field the date field
     * @return the selected date from <code>field</code>
     */
    private Date getDate(DateField field) {
        return field.getDateChooser().getSelectedDate().getTime();
    }

    /**
     * Sets the date of the given field.
     *
     * @param field the date field
     * @param date  the date to set
     */
    private void setDate(DateField field, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        field.getDateChooser().setSelectedDate(calendar);
    }

    /**
     * Sets the selected status in the status selector, if it exists.
     *
     * @param status the status to selecte
     */
    private void updateStatusSelector(String status) {
        if (_statusSelector != null) {
            LookupListModel model
                    = (LookupListModel) _statusSelector.getModel();
            int index = model.indexOf(status);
            if (index != -1) {
                _statusSelector.setSelectedIndex(index);
            }
        }
    }

}
