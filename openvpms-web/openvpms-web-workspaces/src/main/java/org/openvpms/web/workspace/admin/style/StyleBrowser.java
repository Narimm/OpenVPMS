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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.style;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.UserStyleSheets;
import org.openvpms.web.echo.table.DefaultTableHeaderRenderer;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;
import org.openvpms.web.resource.i18n.Messages;

import java.awt.Dimension;
import java.util.Map;


/**
 * A browser for style sheet properties.
 *
 * @author Tim Anderson
 */
public class StyleBrowser {

    /**
     * Screen resolution selector.
     */
    private SelectField resolutionSelector;

    /**
     * Screen width property.
     */
    private final SimpleProperty width;

    /**
     * Screen height property.
     */
    private final SimpleProperty height;

    /**
     * The table that displays the properties of the selected screen resolution.
     */
    private Table propertyTable;

    /**
     * The user style sheets.
     */
    private final UserStyleSheets styles;

    /**
     * The rendered component.
     */
    private Component component;

    /**
     * Listener for modifications to the width and height properties.
     */
    private final ModifiableListener resolutionListener;


    /**
     * Constructs a <tt>StyleBrowser</tt>.
     *
     * @param stylesheets the user style sheets
     * @param size        the inital resolution to evaluate properties against
     */
    public StyleBrowser(UserStyleSheets stylesheets, Dimension size) {
        this.styles = stylesheets;
        resolutionListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                displayResolution();
            }
        };
        width = new SimpleProperty("width", size.width, Integer.class);
        width.setDisplayName(Messages.get("stylesheet.width"));
        width.addModifiableListener(this.resolutionListener);
        height = new SimpleProperty("height", size.height, Integer.class);
        height.setDisplayName(Messages.get("stylesheet.height"));
        height.addModifiableListener(resolutionListener);

        resolutionSelector = createResolutionSelector();
//        resolutionSelector.getSelectionModel().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//            }
//        });
        resolutionSelector.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                updateWidthAndHeight(getSelectedResolution());
                displayResolution();
            }
        });
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (component == null) {
            Row row1 = RowFactory.create("CellSpacing", LabelFactory.create("stylesheet.resolution"),
                                         resolutionSelector);
            Row row2 = RowFactory.create("CellSpacing", createLabel(width), createField(width), createLabel(height),
                                         createField(height));
            Row wrapper = RowFactory.create("WideCellSpacing", row1, row2);
            component = ColumnFactory.create("Inset", ColumnFactory.create("WideCellSpacing", wrapper, getTable()));
        }
        return component;
    }

    /**
     * Returns the selected resolution.
     *
     * @return the selected resolution
     */
    public Dimension getSelectedResolution() {
        Dimension result = (Dimension) resolutionSelector.getSelectedItem();
        return result != null ? result : StyleHelper.ANY_RESOLUTION;
    }

    /**
     * Sets the selected resolution.
     *
     * @param resolution the resolution
     */
    public void setSelectedResolution(Dimension resolution) {
        resolutionSelector.setSelectedItem(resolution);
        updateWidthAndHeight(resolution);
    }

    /**
     * Refreshes the browser.
     */
    public void refresh() {
        Dimension size = getSelectedResolution();
        DefaultListModel model = getResolutions();
        resolutionSelector.setModel(model);
        if (model.indexOf(size) != -1) {
            resolutionSelector.setSelectedItem(size);
        } else {
            resolutionSelector.setSelectedIndex(0);
        }
        updateWidthAndHeight(size);
        displayResolution();
    }

    /**
     * Updates the widhe and height fields for the supplied screen resolution.
     *
     * @param size the screen resolution
     */
    private void updateWidthAndHeight(Dimension size) {
        if (!StyleHelper.ANY_RESOLUTION.equals(size)) {
            // update the width and height based on the selected resolution
            try {
                width.removeModifiableListener(resolutionListener);
                height.removeModifiableListener(resolutionListener);
                width.setValue(size.width);
                height.setValue(size.height);
            } finally {
                width.addModifiableListener(resolutionListener);
                height.addModifiableListener(resolutionListener);
            }
        }
    }

    /**
     * Displays the selected resolution.
     */
    private void displayResolution() {
        getTable().setModel(createTableModel());
    }

    /**
     * Returns the resolution to evaluate properties against.
     *
     * @return the resolution to evaluate properties against
     */
    private Dimension getEvalResolution() {
        if (width.isValid() && height.isValid()) {
            return new Dimension((Integer) width.getValue(), (Integer) height.getValue());
        }
        return new Dimension();
    }

    /**
     * Returns the available resolutions.
     * <p/>
     * This always returns a model with at least one resolution.
     *
     * @return the resolutions
     */
    private DefaultListModel getResolutions() {
        Dimension[] sizes = (Dimension[]) ArrayUtils.add(styles.getResolutions(), 0, StyleHelper.ANY_RESOLUTION);
        return new DefaultListModel(sizes);
    }

    /**
     * Returns the table to display properties in.
     *
     * @return the table
     */
    private Table getTable() {
        if (propertyTable == null) {
            PropertyTableModel model = createTableModel();
            propertyTable = TableFactory.create(model);
            propertyTable.setDefaultHeaderRenderer(DefaultTableHeaderRenderer.DEFAULT);
            propertyTable.setDefaultRenderer(Object.class, EvenOddTableCellRenderer.INSTANCE);
        }
        return propertyTable;
    }

    /**
     * Creates a new property table model.
     *
     * @return a new model
     */
    private PropertyTableModel createTableModel() {
        Dimension size = getSelectedResolution();
        Dimension evalSize = getEvalResolution();
        Map<String, String> properties = StyleHelper.getProperties(styles, size, false);
        Map<String, String> evaluated = styles.evaluate(properties, evalSize.width, evalSize.height);
        return new PropertyTableModel(properties, evaluated);
    }

    /**
     * Creates a new screen resolution selector.
     *
     * @return a new screen resolution selector
     */
    private SelectField createResolutionSelector() {
        SelectField field = SelectFieldFactory.create(getResolutions());
        field.addActionListener(new ActionListener() { // add a listener so notification occurs in timely fashion

            public void onAction(ActionEvent event) {
            }
        });
        field.setCellRenderer(new ListCellRenderer() {
            public Object getListCellRendererComponent(Component list, Object value, int index) {
                if (index == 0) {
                    return Messages.get("stylesheet.anyresolution");
                }
                Dimension size = (Dimension) value;
                return Messages.format("stylesheet.size", size.width, size.height);
            }
        });
        return field;
    }

    /**
     * Helper to create a label for a property.
     *
     * @param property the property
     * @return a new label
     */
    private Label createLabel(Property property) {
        Label label = LabelFactory.create();
        label.setText(property.getDisplayName());
        return label;
    }

    /**
     * Helper to create a 4 character wide text field, bound to a property.
     *
     * @param property the property
     * @return a new text field
     */
    private TextComponent createField(Property property) {
        return BoundTextComponentFactory.create(property, 4);
    }

    private static class PropertyTableModel extends AbstractTableModel {

        /**
         * The properties and their expressions.
         */
        private final Map<String, String> properties;

        /**
         * The evaluated properties.
         */
        private final Map<String, String> evaluated;

        /**
         * The column header names.
         */
        private String[] columnNames =
                {Messages.get("stylesheet.propertyName"), Messages.get("stylesheet.propertyExpression"),
                 Messages.get("stylesheet.propertyValue")};

        /**
         * The property name column.
         */
        private static final int NAME_COLUMN = 0;

        /**
         * The property expression column.
         */
        private static final int EXPRESSION_COLUMN = 1;

        /**
         * The property value column.
         */
        private static final int VALUE_COLUMN = 2;

        /**
         * Constructs a <tt>PropertyTableModel</tt>.
         *
         * @param properties the properties
         * @param evaluated  the evaluated properties
         */
        public PropertyTableModel(Map<String, String> properties, Map<String, String> evaluated) {
            this.properties = properties;
            this.evaluated = evaluated;
        }

        /**
         * Returns the name of the specified column.
         */
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /**
         * Returns the number of columns in the table.
         *
         * @return the column count
         */
        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns the number of rows in the table.
         *
         * @return the row count
         */
        public int getRowCount() {
            return properties.size();
        }

        /**
         * Returns the value found at the given coordinate within the table.
         * Column and row values are 0-based.
         *
         * @param column the column index (0-based)
         * @param row    the row index (0-based)
         */
        public Object getValueAt(int column, int row) {
            Object result = null;
            String[] keys = properties.keySet().toArray(new String[properties.keySet().size()]);
            String key = keys[row];
            switch (column) {
                case NAME_COLUMN:
                    result = key;
                    break;
                case EXPRESSION_COLUMN:
                    result = properties.get(key);
                    break;
                case VALUE_COLUMN:
                    result = evaluated.get(key);
                    break;
            }
            return result;
        }

    }

}
