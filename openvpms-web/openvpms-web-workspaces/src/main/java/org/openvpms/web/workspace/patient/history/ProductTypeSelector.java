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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import echopointng.BorderEx;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.table.DefaultTableCellRenderer;
import org.openvpms.web.echo.table.TableNavigator;
import org.openvpms.web.resource.i18n.Messages;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Product type selector. This supports:
 * <ul>
 * <li>multiple selection</li>
 * <li>text entry, with completion on return</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ProductTypeSelector extends DropDown {

    /**
     * Text field property.
     */
    private final Property text = new SimpleProperty("productType", String.class);

    /**
     * Listener for changes to the text property.
     */
    private final ModifiableListener textListener;

    /**
     * Checkbox to select all product types.
     */
    private final CheckBox all;

    /**
     * Listener for changes to the All checkbox.
     */
    private final PropertyChangeListener allListener;

    /**
     * Listener for changes to product type checkboxes.
     */
    private final PropertyChangeListener typeListener;

    /**
     * The product types.
     */
    private final List<Type> types;

    /**
     * The table.
     */
    private final PagedIMTable<Type> pagedTable;

    /**
     * Listener to notify of selection changes.
     */
    private Runnable listener;

    /**
     * Check box column index.
     */
    private static final int SELECTED_INDEX = 0;

    /**
     * Product type column index.
     */
    private static final int PRODUCT_TYPE_INDEX = 1;

    /**
     * Constructs a {@link {ProductTypeSelector}.
     */
    public ProductTypeSelector() {
        textListener = modifiable -> updateSelection();
        allListener = evt -> onAllChanged();
        typeListener = evt -> onProductTypeChanged();
        Model model = new Model();
        pagedTable = new PagedIMTable<>(model);

        setTarget(BoundTextComponentFactory.create(text, 20));
        setBorder(BorderEx.NONE);
        setRolloverBorder(BorderEx.NONE);
        setPopUpAlwaysOnTop(true);
        setFocusOnExpand(true);

        ArchetypeQuery query = new ArchetypeQuery(ProductArchetypes.PRODUCT_TYPE);
        List<Entity> productTypes = QueryHelper.query(query);
        all = CheckBoxFactory.create(true);

        // listener to force change events to be notified as they occur
        ActionListener actionListener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
            }
        };
        all.addActionListener(actionListener);
        all.addPropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, allListener);

        types = new ArrayList<>();
        types.add(new Type(Messages.get("list.all"), all));

        for (Entity productType : productTypes) {
            CheckBox checkbox = CheckBoxFactory.create(false);
            checkbox.addPropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, typeListener);
            checkbox.addActionListener(actionListener);
            types.add(new Type(productType, checkbox));
        }

        Table table = pagedTable.getTable();
        table.setBorder(new Border(0, Color.WHITE, Border.STYLE_NONE));
        table.setSelectionEnabled(true);
        table.setRolloverEnabled(false);
        table.setHeaderVisible(false);
        table.setDefaultRenderer(Object.class, DefaultTableCellRenderer.INSTANCE);
        table.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onTableSelected();
            }
        });
        pagedTable.setResultSet(new ListResultSet<>(types, 20));
        // magic no. that allows product types to be displayed in an insurance claim history browser without clipping

        setPopUp(pagedTable.getComponent());

        text.addModifiableListener(textListener);
    }

    /**
     * Registers a listener to be notified of selections.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    /**
     * Determines if all product types are selected.
     *
     * @return {@code true} if all product types are selected
     */
    public boolean isAll() {
        return all.isSelected();
    }

    /**
     * Returns the selected product types.
     *
     * @return the selected product types
     */
    public List<Entity> getSelected() {
        return getProductTypes(isAll());
    }

    /**
     * Returns the product types.
     *
     * @param all if {@code true} return all product types, otherwise only return those that are selected
     * @return the product types
     */
    private List<Entity> getProductTypes(boolean all) {
        List<Entity> result = new ArrayList<>();
        for (Type productType : types) {
            if (productType.productType != null) {
                if (all || productType.checkBox.isSelected()) {
                    result.add(productType.productType);
                }
            }
        }
        return result;
    }

    /**
     * Invoked when a product type checkbox changes.
     */
    private void onProductTypeChanged() {
        boolean selectAll = !haveSelections();
        if (all.isSelected() != selectAll) {
            all.removePropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, allListener);
            all.setSelected(selectAll);
            all.addPropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, allListener);
        }
        List<Entity> selected = getProductTypes(false);
        int size = selected.size();
        if (size == 1) {
            setText(selected.get(0).getName());
        } else if (size > 1) {
            setText(Messages.get("patient.record.query.multipleproductype"));
        } else {
            setText(null);
        }
        notifyListener();
    }

    /**
     * Determines if there are any product type selections, excluding 'All'.
     *
     * @return {@code true} if there are any product type
     */
    private boolean haveSelections() {
        for (Type productType : types) {
            if (productType.productType != null && productType.checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invoked when a row is selected.
     */
    private void onTableSelected() {
        setExpanded(false);
        Table table = pagedTable.getTable();
        int index = table.getSelectionModel().getMinSelectedIndex();
        if (index >= 0 && index < types.size()) {
            Type type = types.get(index);
            if (type.productType == null) {
                // i.e. All selected
                onAllChanged();
            } else {
                CheckBox checkBox = type.checkBox;
                if (!checkBox.isSelected()) {
                    checkBox.setSelected(true); // will deselect All
                    notifyListener();
                }
            }
        }
        table.getSelectionModel().clearSelection();
    }

    /**
     * Invoked when the 'All' checkbox changes.
     */
    private void onAllChanged() {
        boolean changed = deselect();
        if (!all.isSelected()) {
            all.setSelected(true);
            changed = true;
        }
        setText(null);
        if (changed) {
            notifyListener();
        }
    }

    /**
     * Notifies the listener of changes, if one is registered.
     */
    private void notifyListener() {
        if (listener != null) {
            try {
                listener.run();
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Updates the text field.
     *
     * @param value the text. May be {@code null}
     */
    private void setText(String value) {
        text.removeModifiableListener(textListener);
        text.setValue(value);
        text.addModifiableListener(textListener);
    }

    /**
     * Updates a product type checkbox, disabling events.
     *
     * @param checkbox the checkbox
     * @param selected if {@code true} select it, otherwise deselect it
     * @return {@code true} if the checkbox was changed
     */
    private boolean setSelected(CheckBox checkbox, boolean selected) {
        boolean changed = false;
        if (checkbox.isSelected() != selected) {
            checkbox.removePropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, typeListener);
            checkbox.setSelected(selected);
            checkbox.addPropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, typeListener);
            changed = true;
        }
        return changed;
    }

    /**
     * Deselect checkboxes other than All.
     *
     * @return {@code true} if changes were made
     */
    private boolean deselect() {
        boolean changed = false;
        List<Type> objects = types;
        for (int i = 1; i < objects.size(); ++i) {
            CheckBox checkBox = objects.get(i).checkBox;
            if (setSelected(checkBox, false)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Updates the selection based on the text field.
     */
    private void updateSelection() {
        String value = text.getString();
        String full = null;
        if (StringUtils.isBlank(value)) {
            onAllChanged();
        } else {
            boolean changed = false;
            int selected = 0;
            value = value.toLowerCase();
            List<Type> objects = types;
            for (int i = 1; i < objects.size(); ++i) {
                Type entry = objects.get(i);
                Entity type = entry.productType;
                boolean select;
                if (type.getName() != null && type.getName().toLowerCase().contains(value)) {
                    select = true;
                    selected++;
                    full = type.getName();
                } else {
                    select = false;
                }
                CheckBox checkBox = entry.checkBox;
                if (setSelected(checkBox, select)) {
                    changed = true;
                }
            }
            boolean haveSelection = (selected != 0);
            if (all.isSelected() == haveSelection) {
                all.removePropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, allListener);
                all.setSelected(!haveSelection);
                all.addPropertyChangeListener(CheckBox.SELECTED_CHANGED_PROPERTY, allListener);
            }
            if (selected == 1) {
                setText(full);
            } else if (selected > 1) {
                setText(Messages.get("patient.record.query.multipleproductype"));
            }
            if (changed) {
                TableNavigator navigator = pagedTable.getNavigator();
                if (navigator != null) {
                    navigator.first();
                }

                notifyListener();
            }
        }
    }

    private static class Type {

        private final Entity productType;

        private final CheckBox checkBox;

        private final String name;

        Type(Entity productType, CheckBox checkBox) {
            this.productType = productType;
            this.checkBox = checkBox;
            this.name = productType.getName();
        }

        Type(String name, CheckBox checkBox) {
            this.productType = null;
            this.checkBox = checkBox;
            this.name = name;
        }
    }

    private static class Model extends AbstractIMTableModel<Type> {

        Model() {
            // need to render the check boxes in 2 columns so the clicking on the product type name can close the
            // popup whereas clicking on the checkboxes leaves the popup open
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            TableColumn selected = new TableColumn(SELECTED_INDEX);
            selected.setWidth(new Extent(20));
            model.addColumn(selected);
            model.addColumn(new TableColumn(PRODUCT_TYPE_INDEX));
            setTableColumnModel(model);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Type object, TableColumn column, int row) {
            if (column.getModelIndex() == SELECTED_INDEX) {
                return object.checkBox;
            } else if (column.getModelIndex() == PRODUCT_TYPE_INDEX) {
                return object.name;
            }
            return null;
        }
    }

}