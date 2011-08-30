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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Query} interface that queries objects
 * on short name, instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractQuery<T> implements Query<T> {

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * Archetype short names to matches on.
     */
    private final String[] shortNames;

    /**
     * Archetype reference model name. May be <code>null</code>
     */
    private final String refModelName;

    /**
     * Additional constraints to associate with the query. May be
     * <code>null</code>
     */
    private IConstraint constraints;

    /**
     * Determines if the query should be run automatically.
     */
    private boolean auto;

    /**
     * Determines if duplicate rows should be filtered.
     */
    private boolean distinct;

    /**
     * The instance name field. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField instanceName;

    /**
     * The minimum length of the name field, before queries can be performed.
     */
    private int nameMinLength;

    /**
     * The inactive check box. If selected, deactived instances will be returned
     * along with the active ones.
     */
    private CheckBox inactive;

    /**
     * The selected archetype short name. If <code>null</code>, or {@link
     * ArchetypeShortNameListModel#ALL}, indicates to query using all matching
     * short names.
     */
    private String shortName;

    /**
     * The component representing the query.
     */
    private Component component;

    /**
     * The event listener list.
     */
    private List<QueryListener> listeners = new ArrayList<QueryListener>();

    /**
     * The maxmimum no. of results to return per page.
     */
    private int maxResults = 20;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());

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
     * Construct a new <code>AbstractQuery</code> that queries objects with
     * the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames);
        archetypes = new ShortNameConstraint(shortNames, true, true);
        refModelName = null;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = RowFactory.create(ROW_STYLE);
            doLayout(component);
        }
        return component;
    }

    /**
     * Sets the maximum no. of results to return per page.
     *
     * @param maxResults the maxiomum no. of rows per page
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Returns the maximum no. of results to return per page.
     *
     * @return the maximum no. of results to return per page
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        return createResultSet(sort);
    }

    /**
     * The archetype short names being queried.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <code>null</code>
     */
    public void setName(String name) {
        getInstanceName().setText(name);
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <code>null</code>
     */
    public String getName() {
        final String wildcard = "*";
        String name = getInstanceName().getText();
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
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length
     */
    public void setNameMinLength(int length) {
        nameMinLength = length;
    }

    /**
     * Returns the minimum length of a name before queries can be performed
     *
     * @return the minimum length
     */
    public int getNameMinLength() {
        return nameMinLength;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <code>true</code> the query should be run automatically
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwise <code>false</code>
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return <code>true</code> if duplicate rows should be removed;
     *         otherwise <code>false</code>
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    public void setConstraints(IConstraint constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns query contraints.
     *
     * @return the constraints
     */
    public IConstraint getConstraints() {
        return constraints;
    }

    /**
     * Returns the archetypes to select from.
     *
     * @return the archetypes to select from
     */
    public ShortNameConstraint getArchetypes() {
        return archetypes;
    }

    /**
     * Returns the archetype reference model name.
     *
     * @return the archetype reference model name. May be <code>null</code>
     */
    public String getRefModelName() {
        return refModelName;
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <code>null</code>
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet(SortConstraint[] sort);

    /**
     * Returns the archetypes to query, based on whether a short name has been
     * selected or not.
     *
     * @return the archetypes to query
     */
    protected ShortNameConstraint getArchetypeConstraint() {
        String type = getShortName();
        boolean activeOnly = !includeInactive();
        ShortNameConstraint result;
        if (type == null || type.equals(ShortNameListModel.ALL)) {
            result = getArchetypes();
            result.setActiveOnly(activeOnly);
        } else {
            result = new ShortNameConstraint(type, true, activeOnly);
        }
        return result;
    }

    /**
     * Determines if inactive instances should be returned.
     *
     * @return <code>true</code> if inactive instances should be retured;
     *         <code>false</code>
     */
    protected boolean includeInactive() {
        return (inactive != null && inactive.isSelected());
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    protected String getShortName() {
        return shortName;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        shortName = name;
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
        if (shortNames.length > 1) {
            final ShortNameListModel model
                    = new ShortNameListModel(shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(
                    model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = shortNameSelector.getSelectedIndex();
                    String shortName = model.getShortName(index);
                    setShortName(shortName);
                }
            });
            shortNameSelector.setCellRenderer(new ShortNameListCellRenderer());

            Label typeLabel = LabelFactory.create(TYPE_ID);
            container.add(typeLabel);
            container.add(shortNameSelector);
            focusGroup.add(shortNameSelector);
        }
    }

    /**
     * Returns the instance name field.
     *
     * @return the instance name field
     */
    protected TextField getInstanceName() {
        if (instanceName == null) {
            instanceName = TextComponentFactory.create();
            instanceName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onInstanceNameChanged();
                }
            });
        }
        return instanceName;
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
        focusGroup.add(instanceName);
    }

    /**
     * Returns the inactive field.
     *
     * @return the inactive field
     */
    protected CheckBox getInactive() {
        if (inactive == null) {
            inactive = new CheckBox();
            inactive.setSelected(false);
        }
        return inactive;
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addInactive(Component container) {
        Label deactivedLabel = LabelFactory.create(DEACTIVATED_ID);
        container.add(deactivedLabel);
        container.add(getInactive());
        focusGroup.add(inactive);
    }

    /**
     * Invoked when the instance name changes. Invokes {@link #onQuery}.
     */
    protected void onInstanceNameChanged() {
        onQuery();
    }

    /**
     * Notify listnerss to perform a query.
     */
    protected void onQuery() {
        QueryListener[] listeners = this.listeners.toArray(
                new QueryListener[0]);
        for (QueryListener listener : listeners) {
            listener.query();
        }
    }

    /**
     * Determines if a query may be performed on name.
     * A query can be performed on name if the length of the name
     * (minus wildcards) &gt;= {@link #getNameMinLength()}
     *
     * @return <code>true</code> if a query may be performed on name;
     *         otherwise <code>false</code>
     */
    protected boolean canQueryOnName() {
        String name = getName();
        int length = CharSetUtils.delete(name, "*").length();
        return (length >= getNameMinLength());
    }

}
