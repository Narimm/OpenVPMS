package org.openvpms.web.component.table;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.TableCellRenderer;


/**
 * Abstract implementation of the <code>TableCellRenderer</code> interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractTableCellRenderer implements TableCellRenderer {

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
        Component component = getComponent(value);
        String style = getStyle(column, row);
        mergeLayout(component, style);
        return component;
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected abstract String getStyle(int column, int row);

    /**
     * Returns a component for a value.
     *
     * @param value the value retrieved from the <code>TableModel</code>
     * @return a component representation of the value
     */
    protected Component getComponent(Object value) {
        Component component;
        if (value instanceof Component) {
            component = (Component) value;
        } else {
            Label label;
            if (value != null) {
                label = new Label(value.toString());
            } else {
                label = new Label();
            }
            component = label;
        }
        return component;
    }

    /**
     * Merges the layout of a component with that defined by the stylesheet.
     *
     * @param component the component
     * @param styleName the stylesheet style name
     */
    protected void mergeLayout(Component component, String styleName) {
        if (component.getLayoutData() == null) {
            component.setStyleName(styleName);
        } else {
            Style style = ApplicationInstance.getActive().getStyle(
                    Component.class, styleName);
            if (style != null) {
                TableLayoutData layout =
                        (TableLayoutData) component.getLayoutData();
                TableLayoutData styleLayout
                        = (TableLayoutData) style.getProperty("layoutData");
                if (styleLayout != null) {
                    if (layout.getAlignment() == null) {
                        layout.setAlignment(styleLayout.getAlignment());
                    }
                    if (layout.getBackground() == null) {
                        layout.setBackground(styleLayout.getBackground());
                    }
                    if (layout.getBackgroundImage() == null) {
                        layout.setBackgroundImage(
                                styleLayout.getBackgroundImage());
                    }
                    if (layout.getInsets() == null) {
                        layout.setInsets(styleLayout.getInsets());
                    }
                }
            }
        }
    }
}
