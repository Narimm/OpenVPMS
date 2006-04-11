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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActQuery extends AbstractQuery {

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference _entityId;

    /**
     * Determines if acts should be filtered on type.
     */
    private final boolean _selectType;

    /**
     * The status dropdown.
     */
    private SelectField _statusSelector;

    /**
     * The selected status.
     */
    private String _status;

    /**
     * The act status lookups.
     */
    private final List<Lookup> _statusLookups;

    /**
     * Status to exclude. May be <code>null</code>
     */
    private final String _excludeStatus;

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
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     */
    public ActQuery(Entity entity, String entityName, String conceptName,
                    List<Lookup> statusLookups) {
        this(entity, entityName, conceptName, statusLookups, null);
    }

    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     */
    public ActQuery(Entity entity, String entityName, String conceptName,
                    List<Lookup> statusLookups,
                    String excludeStatus) {
        super(null, entityName, conceptName);
        setEntity(entity);
        _excludeStatus = excludeStatus;
        if (_excludeStatus != null) {
            _statusLookups = new ArrayList<Lookup>(statusLookups);
            for (ListIterator<Lookup> iterator = _statusLookups.listIterator();
                 iterator.hasNext();) {
                Lookup lookup = iterator.next();
                if (lookup.getValue().equals(_excludeStatus)) {
                    iterator.remove();
                }
            }
        } else {
            _statusLookups = statusLookups;
        }
        _selectType = false;
    }

    /**
     * Construct a new <code>ActQuery</code> to query acts for a specific
     * status.
     *
     * @param entityName  the act entity name
     * @param conceptName the act concept name
     * @param status      the act status
     */
    public ActQuery(Entity entity, String entityName, String conceptName,
                    String status) {
        super(null, entityName, conceptName);
        setEntity(entity);
        _status = status;
        _statusLookups = null;
        _excludeStatus = null;
        _selectType = true;
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
     * Performs the query.
     *
     * @param rows      the maxiomum no. of rows per page
     * @param node      the node to sort on. May be <code>null</code>
     * @param ascending if <code>true</code> sort the rows in ascending order;
     *                  otherwise sort them in <code>descebding</code> order
     * @return the query result set
     */
    @Override
    public ResultSet query(int rows, String node, boolean ascending) {
        ResultSet<Act> result = null;

        String type = getShortName();
        String entityName;
        String conceptName;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            entityName = getEntityName();
            conceptName = getConceptName();
        } else {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(type);
            ArchetypeId id = new ArchetypeId(archetype.getName());
            entityName = id.getEntityName();
            conceptName = id.getConcept();
        }

        if (_entityId != null) {
            Date startFrom = getStartFrom();
            Date startTo = getStartTo();
            SortOrder order = null;
            if (node != null) {
                order = new SortOrder(node, ascending);
            }
            String status;
            boolean exclude = false;
            if (_excludeStatus != null && _status == null) {
                status = _excludeStatus;
                exclude = true;
            } else {
                status = _status;
            }
            result = new ActResultSet(_entityId, entityName, conceptName,
                                      startFrom, startTo, status, exclude, rows,
                                      order);
        }
        return result;
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

        if (_statusLookups != null) {
            LookupListModel model = new LookupListModel(_statusLookups, true);
            _statusSelector = SelectFieldFactory.create(model);
            _statusSelector.setCellRenderer(new LookupListCellRenderer());
            _statusSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onStatusChanged();
                }
            });
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
        _status = (String) _statusSelector.getSelectedItem();
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
     * Returns the start-from date.
     *
     * @return the start-from date
     */
    private Date getStartFrom() {
        return _startAll.isSelected() ? null : getDate(_startFrom);
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
     * Returns the start-to date.
     *
     * @return the start-to date
     */
    private Date getStartTo() {
        return _startAll.isSelected() ? null : getDate(_startTo);
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

}
