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

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.system.common.query.ArchetypeConstraint;
import org.openvpms.component.system.common.query.ArchetypeLongNameConstraint;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;


/**
 * Abstract implementation of the {@link Query} interface that queries {@link
 * IMObject} instances on short name, instance name, and active/inactive
 * status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractQuery implements Query {

    /**
     * The archetypes to query.
     */
    private final ArchetypeConstraint _archetypes;

    /**
     * Archetype short names to matches on.
     */
    private final String[] _shortNames;

    /**
     * Archetype reference model name. May be <code>null</code>
     */
    private final String _refModelName;

    /**
     * Archetype entity name. May be <code>null</code>
     */
    private final String _entityName;

    /**
     * Archetype concept name. May be <code>null</code>
     */
    private final String _conceptName;

    /**
     * Additional constraints to associate with the query. May be
     * <code>null</code>
     */
    private IConstraint _constraints;

    /**
     * The instance name. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField _instanceName;

    /**
     * The inactive check box. If selected, deactived instances will be returned
     * along with the active ones.
     */
    private CheckBox _inactive;

    /**
     * The selected archetype short name. If <code>null</code>, or {@link
     * ArchetypeShortNameListModel#ALL}, indicates to query using all matching
     * short names.
     */
    private String _shortName;

    /**
     * The component representing the query.
     */
    private Component _component;

    /**
     * The event listener list.
     */
    private List<QueryListener> _listeners = new ArrayList<QueryListener>();

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "type";

    /**
     * Name label id.
     */
    private static final String NAME_ID = "name";

    /**
     * Deactivated label id.
     */
    private static final String DEACTIVATED_ID = "deactivated";

    /**
     * Button row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     */
    public AbstractQuery(String[] shortNames) {
        _shortNames = DescriptorHelper.getShortNames(shortNames);
        _archetypes = new ArchetypeShortNameConstraint(shortNames, true, true);
        _refModelName = null;
        _entityName = null;
        _conceptName = null;
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractQuery(String refModelName, String entityName,
                         String conceptName) {
        _shortNames = DescriptorHelper.getShortNames(refModelName, entityName,
                                                     conceptName);
        _archetypes = new ArchetypeLongNameConstraint(
                refModelName, entityName, conceptName, true, true);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            _component = RowFactory.create(ROW_STYLE);
            doLayout(_component);
        }
        return _component;
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
    public ResultSet query(int rows, String node, boolean ascending) {
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();

        ArchetypeConstraint archetypes;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            archetypes = _archetypes;
            archetypes.setActiveOnly(activeOnly);
        } else {
            archetypes = new ArchetypeShortNameConstraint(type, true,
                                                          activeOnly);
        }

        SortOrder sort = null;
        if (node != null) {
            sort = new SortOrder(node, ascending);
        }

        return new DefaultResultSet(archetypes, name, _constraints, sort, rows);
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
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        _listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    public void setConstraints(IConstraint constraints) {
        _constraints = constraints;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetype constraint
     */
    public ArchetypeConstraint getArchetypeConstraint() {
        return _archetypes;
    }
    
    /**
     * Returns the archetype reference model name.
     *
     * @return the archetype reference model name. May be <code>null</code>
     */
    public String getRefModelName() {
        return _refModelName;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the archetype entity name. May be <code>null</code>
     */
    public String getEntityName() {
        return _entityName;
    }

    /**
     * Returns the archetype concept name.
     *
     * @return the archetype concept name. May be <code>null</code>
     */
    public String getConceptName() {
        return _conceptName;
    }

    /**
     * Sets the archetype instance name to query.
     *
     * @param name the archetype instance name. If <code>null</code> indicates
     *             to query all instances
     */
    protected void setName(String name) {
        _instanceName.setText(name);
    }

    /**
     * Returns the archetype instance name, including wildcards.
     *
     * @return the archetype instance name. Nay be <code>null</code>
     */
    protected String getName() {
        final String wildcard = "*";
        String name = _instanceName.getText();
        if (!StringUtils.isEmpty(name)) {
            // if entered name contains a wildcard then leave alone else
            // add one to end
            if (!name.contains(wildcard)) {
                name = name + wildcard;
            }
        }
        return name;
    }

    /**
     * Determines if inactive instances should be returned.
     *
     * @return <code>true</code> if inactive instances should be retured;
     *         <code>false</code>
     */
    protected boolean includeInactive() {
        return _inactive.isSelected();
    }

    /**
     * Returns the archetype short names.
     *
     * @return the archetype short names
     */
    protected String[] getShortNames() {
        return _shortNames;
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    protected String getShortName() {
        return _shortName;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        _shortName = name;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addInstanceName(container);
        addInactive(container);
        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
    }

    /**
     * Adds the short name selector to a container, if there is more than one
     * matching short name
     *
     * @param container the container
     */
    protected void addShortNameSelector(Component container) {
        if (_shortNames.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(_shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = shortNameSelector.getSelectedIndex();
                    String shortName = model.getShortName(index);
                    setShortName(shortName);
                }
            });

            Label typeLabel = LabelFactory.create(TYPE_ID);
            container.add(typeLabel);
            container.add(shortNameSelector);
        }
    }

    /**
     * Returns the instance name field.
     *
     * @return the instance name field
     */
    protected TextField getInstanceName() {
        if (_instanceName == null) {
            _instanceName = TextComponentFactory.create();
            _instanceName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return _instanceName;
    }

    /**
     * Adds the instance name field to a container.
     *
     * @param container the container
     */
    protected void addInstanceName(Component container) {
        Label nameLabel = LabelFactory.create(NAME_ID);
        container.add(nameLabel);
        container.add(getInstanceName());
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addInactive(Component container) {
        _inactive = new CheckBox();
        _inactive.setSelected(false);
        Label deactivedLabel = LabelFactory.create(DEACTIVATED_ID);
        container.add(deactivedLabel);
        container.add(_inactive);
    }

    /**
     * Notify listnerss to perform a query.
     */
    protected void onQuery() {
        QueryListener[] listeners = _listeners.toArray(new QueryListener[0]);
        for (QueryListener listener : listeners) {
            listener.query();
        }
    }

}
