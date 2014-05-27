/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;

import static org.openvpms.component.system.common.query.Constraints.sort;


/**
 * Abstract implementation of the {@link Query} interface that queries objects
 * on short name, some search criteria, and active/inactive status.
 *
 * @author Tim Anderson
 */
public abstract class AbstractArchetypeQuery<T> extends AbstractQuery<T> {


    /**
     * Default sort constraint on name and id nodes.
     */
    protected static final SortConstraint[] NAME_SORT_CONSTRAINT = new SortConstraint[]{sort("name"), sort("id")};

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * The search field. If the text is {@code null} or empty, indicates
     * to query all instances.
     */
    private TextField searchField;

    /**
     * The active/inactive selector.
     */
    private SelectField activeSelector;

    /**
     * The selected archetype short name. May be {@code null}
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
    private static final String TYPE_ID = "query.type";

    /**
     * Search label id.
     */
    private static final String SEARCH_ID = "query.search";

    /**
     * Active label id.
     */
    private static final String ACTIVE_ID = "query.active";

    /**
     * The active states to query.
     */
    private static final String[] ACTIVE_CHOICES = {"query.active.yes", "query.active.no", "query.active.both"};

    /**
     * Index of the Active - Yes selection.
     */
    private static final int ACTIVE_YES = 0;

    /**
     * Index of the Active - No selection.
     */
    private static final int ACTIVE_NO = 1;

    /**
     * Index of the Active - No selection.
     */
    private static final int ACTIVE_BOTH = 2;


    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public AbstractArchetypeQuery(String[] shortNames, Class type) {
        this(shortNames, true, type);
    }

    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if {@code true} only include primary archetypes
     * @param type        the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public AbstractArchetypeQuery(String[] shortNames, boolean primaryOnly, Class type) {
        super(shortNames, primaryOnly, type);
        archetypes = new ShortNameConstraint(getShortNames(), primaryOnly, true);
    }

    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if {@code true} only include primary archetypes
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public AbstractArchetypeQuery(String[] shortNames, boolean primaryOnly) {
        super(shortNames, primaryOnly);
        archetypes = new ShortNameConstraint(getShortNames(), primaryOnly, true);
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
     * Determines if objects must be active.
     *
     * @param active if {@code true} only query active objects, otherwise query both active and inactive objects
     */
    public void setActiveOnly(boolean active) {
        getActiveSelector().setSelectedItem(active ? ACTIVE_CHOICES[ACTIVE_YES] : ACTIVE_CHOICES[ACTIVE_BOTH]);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        return createResultSet(sort);
    }

    /**
     * Determines if the query selects a particular object.
     * <p/>
     * NOTE: This implementation only supports objects of type {@code IMObject}, delegating to
     * {@link #selects(org.openvpms.component.business.domain.im.common.IMObjectReference)}.
     *
     * @param object the object to check
     * @return {@code true} if the object is selected by the query
     */
    @Override
    public boolean selects(T object) {
        return object instanceof IMObject && selects(((IMObject) object).getObjectReference());
    }

    /**
     * Determines if the query selects a particular object reference.
     * <p/>
     * Note that this implementation only supports {@link AbstractArchetypeServiceResultSet}.
     * Derived classes using different {@link ResultSet} implementations must override it.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    public boolean selects(IMObjectReference reference) {
        ResultSet<T> set = query();
        boolean result;
        if (set instanceof AbstractArchetypeServiceResultSet) {
            ((AbstractArchetypeServiceResultSet<T>) set).setReferenceConstraint(reference);
            result = set.hasNext();
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Sets the value to query on.
     *
     * @param value the value. May contain wildcards, or be {@code null}
     */
    public void setValue(String value) {
        getSearchField().setText(value);
    }

    /**
     * Returns the value being queried on.
     *
     * @return the value. May contain wildcards, or be {@code null}
     */
    public String getValue() {
        return getWildcardedText(getSearchField());
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
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be {@code null}
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Determines if active and/or inactive instances should be returned.
     *
     * @return the active state
     */
    public BaseArchetypeConstraint.State getActive() {
        if (activeSelector != null) {
            switch (activeSelector.getSelectedIndex()) {
                case ACTIVE_YES:
                    return BaseArchetypeConstraint.State.ACTIVE;
                case ACTIVE_NO:
                    return BaseArchetypeConstraint.State.INACTIVE;
                default:
                    return BaseArchetypeConstraint.State.BOTH;
            }
        }
        return BaseArchetypeConstraint.State.ACTIVE;
    }

    /**
     * Determines if active and/or inactive instances should be returned.
     *
     * @param state the active state
     */
    public void setActive(BaseArchetypeConstraint.State state) {
        if (activeSelector != null) {
            switch (state) {
                case ACTIVE:
                    activeSelector.setSelectedIndex(ACTIVE_YES);
                    break;
                case INACTIVE:
                    activeSelector.setSelectedIndex(ACTIVE_NO);
                    break;
                default:
                    activeSelector.setSelectedIndex(ACTIVE_BOTH);
            }
        }
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
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet(SortConstraint[] sort);

    /**
     * Determines if only primary archetypes will be queried.
     *
     * @return {@code true} if only primary archetypes will be queried
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
        ShortNameConstraint result;
        if (type == null) {
            result = getArchetypes();
            result.setState(getActive());
        } else {
            result = new ShortNameConstraint(type, isPrimaryOnly(), getActive());
        }
        return result;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If {@code null}, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        shortName = name;
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new {@code Row}.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    protected Component createContainer() {
        return RowFactory.create(Styles.CELL_SPACING);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addSearchField(container);
        addActive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Adds the short name selector to a container, if there is more than one matching short name.
     *
     * @param container the container
     */
    protected void addShortNameSelector(Component container) {
        String[] shortNames = getShortNames();
        if (shortNames.length > 1) {
            final ShortNameListModel model = new ShortNameListModel(shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
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
     * Returns the search field.
     *
     * @return the search field
     */
    protected TextField getSearchField() {
        if (searchField == null) {
            searchField = TextComponentFactory.create();
            searchField.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onSearchFieldChanged();
                }
            });
        }
        return searchField;
    }

    /**
     * Adds the search field to a container.
     *
     * @param container the container
     */
    protected void addSearchField(Component container) {
        Label label = LabelFactory.create(SEARCH_ID);
        container.add(label);
        container.add(getSearchField());
        focusGroup.add(searchField);
    }

    /**
     * Returns the active field.
     *
     * @return the active field
     */
    protected SelectField getActiveSelector() {
        if (activeSelector == null) {
            ListModel model = new DefaultListModel(ACTIVE_CHOICES);
            activeSelector = SelectFieldFactory.create(model);
            activeSelector.setSelectedIndex(0);
            activeSelector.setCellRenderer(new ListCellRenderer() {
                @Override
                public Object getListCellRendererComponent(Component list, Object value, int index) {
                    return Messages.get(value.toString());
                }
            });
        }
        return activeSelector;
    }

    /**
     * Adds the active selector to a container.
     *
     * @param container the container
     */
    protected void addActive(Component container) {
        container.add(LabelFactory.create(ACTIVE_ID));
        container.add(getActiveSelector());
        focusGroup.add(activeSelector);
    }

    /**
     * Invoked when the search field changes. Invokes {@link #onQuery}.
     */
    protected void onSearchFieldChanged() {
        onQuery();
    }

    /**
     * Determines if a query may be performed on name.
     * A query can be performed on name if the length of the name
     * (minus wildcards) &gt;= {@link #getValueMinLength()}
     *
     * @return {@code true} if a query may be performed on name; otherwise {@code false}
     */
    protected boolean canQueryOnName() {
        String name = getValue();
        int length = 0;
        if (name != null) {
            length = CharSetUtils.delete(name, "*").length();
        }
        return (length >= getValueMinLength());
    }

    /**
     * Helper to return the text of the supplied field with a wildcard
     * (<em>*</em>) appended, if it is not empty and doesn't already contain
     * one.
     *
     * @param field the text field
     * @return the wildcarded field text, or {@code null} if the field is empty
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
     * @param substring if {@code true}, also prepend a wildcard if one is appended, to support substring matches
     * @return the wildcarded field text, or {@code null} if the field is empty
     */
    protected String getWildcardedText(TextComponent field, boolean substring) {
        String text = field.getText();
        final String wildcard = "*";
        if (!StringUtils.isEmpty(text)) {
            // if entered name contains a wildcard then leave alone else add one to end
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
