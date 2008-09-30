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

package org.openvpms.web.component.table;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.TableCellRenderer;
import org.apache.commons.beanutils.BeanUtils;
import org.openvpms.web.component.util.LabelFactory;

import java.util.Iterator;


/**
 * Abstract implementation of the <code>TableCellRenderer</code> interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractTableCellRenderer implements TableCellRenderer {

    /**
     * Helper to ensure that empty cells render with non-zero height.
     */
    private static final XhtmlFragment SPACE
            = new XhtmlFragment("<p>&#160;</p>");


    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value.
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        Component component = getComponent(table, value, column, row);
        String style = getStyle(table, value, column, row);
        if (style != null) {
            mergeStyle(component, style);
        }
        return component;
    }

    /**
     * Returns the style name for a column and row.
     * <p/>This implementation returns <tt>null</tt>
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row. May be <tt>null</tt>
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        return null;
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value
     */
    protected Component getComponent(Table table, Object value, int column,
                                     int row) {
        Component component;
        if (value instanceof Component) {
            component = (Component) value;
        } else {
            if (value != null) {
                Label label = LabelFactory.create();
                label.setText(value.toString());
                component = label;
            } else {
                component = new LabelEx(SPACE);
            }
        }
        return component;
    }

    /**
     * Merges the style of a component with that defined by the stylesheet.
     *
     * @param component the component
     * @param styleName the stylesheet style name
     */
    protected void mergeStyle(Component component, String styleName) {
        mergeStyle(component, styleName, false);
    }

    /**
     * Merges the style of a component with that defined by the stylesheet.
     *
     * @param component the component
     * @param styleName the stylesheet style name
     * @param overwrite if <tt>true</tt> overwrite existing component properties
     *                  if the specified style has non-null corresponding
     *                  properties
     */
    protected void mergeStyle(Component component, String styleName,
                              boolean overwrite) {
        if (component.getLayoutData() == null
                && component.getStyleName() == null) {
            component.setStyleName(styleName);
        } else {
            ApplicationInstance app = ApplicationInstance.getActive();
            Style style = app.getStyle(component.getClass(), styleName);
            if (style != null) {
                mergeStyle(style, component, overwrite);
            }
        }
    }

    /**
     * Merge style from a stylessheet with a component's properties
     *
     * @param style     the style
     * @param component the component
     * @param overwrite if <tt>true</tt> overwrite existing component properties
     *                  if the specified style has non-null corresponding
     *                  properties
     */
    private void mergeStyle(Style style, Component component,
                            boolean overwrite) {
        Iterator names = style.getPropertyNames();
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name.equals("layoutData")
                    && component.getLayoutData() != null) {
                TableLayoutData from;
                TableLayoutData to;

                from = (TableLayoutData) style.getProperty(name);
                to = (TableLayoutData) component.getLayoutData();
                if (to != null) {
                    mergeLayoutData(from, to, overwrite);
                }
            } else if (overwrite || component.getProperty(name) == null) {
                Object value = style.getProperty(name);
                if (value != null) {
                    try {
                        BeanUtils.setProperty(component, name, value);
                    } catch (Throwable ignore) {
                        // no-op
                    }
                }
            }
        }
    }

    /**
     * Merge layout data.
     *
     * @param from      the layout data to merge from
     * @param to        the layout data to merge to
     * @param overwrite if <tt>true</tt> overwrite existing component properties
     *                  if the specified style has non-null corresponding
     *                  properties
     */
    private void mergeLayoutData(TableLayoutData from, TableLayoutData to,
                                 boolean overwrite) {
        if (from.getAlignment() != null
                && (overwrite || to.getAlignment() == null)) {
            to.setAlignment(from.getAlignment());
        }
        if (from.getBackground() != null
                && (overwrite || to.getBackground() == null)) {
            to.setBackground(from.getBackground());
        }
        if (from.getBackgroundImage() != null
                && (overwrite || to.getBackgroundImage() == null)) {
            to.setBackgroundImage(from.getBackgroundImage());
        }
        if (from.getInsets() != null
                && (overwrite || to.getInsets() == null)) {
            to.setInsets(from.getInsets());
        }
    }
}
