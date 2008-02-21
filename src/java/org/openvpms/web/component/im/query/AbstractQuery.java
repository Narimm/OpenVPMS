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
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.ArrayList;
import java.util.Iterator;
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
     * The type that this query returns.
     */
    private final Class type;

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * Archetype short names to matches on.
     */
    private final String[] shortNames;

    /**
     * Additional constraints to associate with the query. May be <tt>null</tt>.
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
     * The instance name field. If the text is <tt>null</tt> or empty, indicates
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
     * The selected archetype short name. If <tt>null</tt>, or {@link
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
     * The default sort constraints. May be <tt>null</tt>
     */
    private SortConstraint[] sort;

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
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, Class type) {
        this(shortNames, true, type);
    }

    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if <tt>true</tt> only include primary archetypes
     * @param type        the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @SuppressWarnings("unchecked")
    public AbstractQuery(String[] shortNames, boolean primaryOnly,
                         Class type) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames,
                                                         primaryOnly);
        archetypes = new ShortNameConstraint(shortNames, primaryOnly, true);
        this.type = type;
        if (IMObject.class.isAssignableFrom(type)) {
            // verify that the specified type matches what the query actually
            // returns
            Class actual = IMObjectHelper.getType(this.shortNames);
            if (!type.isAssignableFrom(actual)) {
                throw new QueryException(QueryException.ErrorCode.InvalidType,
                                         type, actual);

            }
        }
    }

    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if <tt>true</tt> only include primary archetypes
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, boolean primaryOnly) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames,
                                                         primaryOnly);
        archetypes = new ShortNameConstraint(shortNames, primaryOnly, true);
        this.type = IMObjectHelper.getType(this.shortNames);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = createContainer();
            doLayout(component);
        }
        return component;
    }

    /**
     * Returns the type that this query returns.
     *
     * @return the type
     */
    public Class getType() {
        return type;
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
     * Sets the default sort constraint.
     *
     * @param sort the default sort cosntraint. May be <tt>null</tt>
     */
    public void setDefaultSortConstraint(SortConstraint[] sort) {
        this.sort = sort;
    }

    /**
     * Returns the default sort constraint
     *
     * @return the default sort constraint. May be <tt>null</tt>
     */
    public SortConstraint[] getDefaultSortConstraint() {
        return sort;
    }

    /**
     * Performs the query using the default sort constraint (if any).
     *
     * @return the query result set
     * @throws ArchetypeServiceException for any error
     */
    public ResultSet<T> query() {
        return query(sort);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        return createResultSet(sort);
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator(SortConstraint[] sort) {
        return new ResultSetIterator<T>(createResultSet(sort));
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator() {
        return iterator(sort);
    }

    /**
     * The archetype short names being queried.
     * Any wildcards are expanded.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <tt>null</tt>
     */
    public void setName(String name) {
        getInstanceName().setText(name);
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <tt>null</tt>
     */
    public String getName() {
        return getWildcardedText(getInstanceName());
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
     * @param auto if <tt>true</tt> the query should be run automatically
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automatically;
     *         otherwise <tt>false</tt>
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
     * @return <tt>true</tt> if duplicate rows should be removed;
     *         otherwise <tt>false</tt>
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
     * @param constraints the constraints. May be <tt>null</tt>
     */
    public void setConstraints(IConstraint constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns query contraints.
     *
     * @return the constraints. May be <tt>null</tt>
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
     * Determines if only primary archetypes will be queried.
     *
     * @return <tt>true</tt> if only primary archetypes will be queried
     */
    public boolean isPrimaryOnly() {
        return archetypes.isPrimaryOnly();
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
     * @param sort the sort criteria. May be <tt>null</tt>
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
            result = new ShortNameConstraint(type, isPrimaryOnly(), activeOnly);
        }
        return result;
    }

    /**
     * Determines if inactive instances should be returned.
     *
     * @return <tt>true</tt> if inactive instances should be returned;
     *         otherwise <tt>false</tt>
     */
    protected boolean includeInactive() {
        return (inactive != null && inactive.isSelected());
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <tt>null</tt>
     */
    protected String getShortName() {
        return shortName;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <tt>null</tt>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        shortName = name;
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new <tt>Row</tt>.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    protected Component createContainer() {
        return RowFactory.create(ROW_STYLE);
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
            inactive = CheckBoxFactory.create(false);
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
     * @return <tt>true</tt> if a query may be performed on name;
     *         otherwise <tt>false</tt>
     */
    protected boolean canQueryOnName() {
        String name = getName();
        int length = 0;
        if (name != null) {
            length = CharSetUtils.delete(name, "*").length();
        }
        return (length >= getNameMinLength());
    }

    /**
     * Helper to return the text of the supplied field with a wildcard
     * (<em>*</em>) appended, if it is not empty and doesn't already contain
     * one.
     *
     * @param field the text field
     * @return the wildcarded field text, or <tt>null</tt> if the field is empty
     */
    protected String getWildcardedText(TextComponent field) {
        return getWildcardedText(field, false);
    }

    /**
     * Helper to return the text of the supplied field with a wildcard
     * (<em>*</em>) appended, if it is not empty and doesn't already contain
     * one.
     *
     * @param field     the text field
     * @param substring if <tt>true</tt>, also prepend a wildcard if one is
     *                  appended, to support substring matches
     * @return the wildcarded field text, or <tt>null</tt> if the field is empty
     */
    protected String getWildcardedText(TextComponent field, boolean substring) {
        String text = field.getText();
        final String wildcard = "*";
        if (!StringUtils.isEmpty(text)) {
            // if entered name contains a wildcard then leave alone else
            // add one to end
            if (!text.contains(wildcard)) {
                text = text + wildcard;
                if (substring) {
                    text = wildcard + text;
                }
            }
        } else {
            text = null;
        }
        return text;
    }

}
