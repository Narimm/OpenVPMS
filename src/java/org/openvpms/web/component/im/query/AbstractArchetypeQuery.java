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
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;


/**
 * Abstract implementation of the {@link Query} interface that queries objects
 * on short name, instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractArchetypeQuery<T> extends AbstractQuery<T> {

    /**
     * The instance name field. If the text is <tt>null</tt> or empty, indicates
     * to query all instances.
     */
    private TextField instanceName;

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
    public AbstractArchetypeQuery(String[] shortNames, Class type) {
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
    public AbstractArchetypeQuery(String[] shortNames, boolean primaryOnly,
                                  Class type) {
        super(shortNames, primaryOnly, type);
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
    public AbstractArchetypeQuery(String[] shortNames, boolean primaryOnly) {
        super(shortNames, primaryOnly);
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
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        return createResultSet(sort);
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
     * Determines if only primary archetypes will be queried.
     *
     * @return <tt>true</tt> if only primary archetypes will be queried
     */
    protected boolean isPrimaryOnly() {
        return getArchetypes().isPrimaryOnly();
    }

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
        FocusHelper.setFocus(getInstanceName());
    }

    /**
     * Adds the short name selector to a container, if there is more than one
     * matching short name
     *
     * @param container the container
     */
    protected void addShortNameSelector(Component container) {
        String[] shortNames = getShortNames();
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
