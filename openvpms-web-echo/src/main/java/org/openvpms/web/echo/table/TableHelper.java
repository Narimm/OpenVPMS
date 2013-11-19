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

package org.openvpms.web.echo.table;

import echopointng.layout.TableLayoutDataEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.layout.TableLayoutData;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.echo.colour.ColourHelper;

import java.util.Iterator;


/**
 * Table helper methods.
 *
 * @author Tim Anderson
 */
public class TableHelper {

    /**
     * Helper to ensure that empty cells render with non-zero height.
     */
    public static final String SPACER = "<div>&#160;</div>";

    /**
     * Helper to return an {@code XhtmlFragment} for text.
     * <p/>
     * Any XML characters are escaped.
     *
     * @param text the text. May be {@code null}
     * @return a new fragment
     */
    public static XhtmlFragment createFragment(String text) {
        if (StringUtils.isEmpty(text)) {
            return new XhtmlFragment(SPACER);
        }

        text = StringEscapeUtils.escapeXml(text);
        return new XhtmlFragment("<p>" + text + "</p>");
    }

    /**
     * Helper to return an {@code XhtmlFragment} for an object, using its {@code toString()} method.
     * <p/>
     * Any XML characters are escaped.
     *
     * @param object the object. May be {@code null}
     * @return a new fragment
     */
    public static XhtmlFragment createFragment(Object object) {
        return createFragment(object != null ? object.toString() : null);
    }

    /**
     * Returns the table layout data associated with a style.
     *
     * @param styleName the style name
     * @return the table layout data, or {@code null} if none is found
     */
    public static TableLayoutDataEx getTableLayoutDataEx(String styleName) {
        TableLayoutDataEx result = null;
        ApplicationInstance app = ApplicationInstance.getActive();
        Style style = app.getStyle(Component.class, styleName);
        if (style != null) {
            LayoutData layout = (LayoutData) style.getProperty("layoutData");
            if (layout instanceof TableLayoutDataEx) {
                result = (TableLayoutDataEx) layout;
            } else if (layout instanceof TableLayoutData) {
                result = new TableLayoutDataEx();
                mergeLayoutData(result, (TableLayoutData) layout, true);
            }
        }
        return result;
    }

    /**
     * Merges the style of a component with that defined by the stylesheet.
     *
     * @param component the component
     * @param styleName the stylesheet style name
     */
    public static void mergeStyle(Component component, String styleName) {
        mergeStyle(component, styleName, false);
    }

    /**
     * Merges the style of a component with that defined by the stylesheet.
     *
     * @param component the component
     * @param styleName the stylesheet style name
     * @param overwrite if {@code true} overwrite existing component properties if the specified style has non-null
     *                  corresponding properties
     */
    public static void mergeStyle(Component component, String styleName, boolean overwrite) {
        if (component.getLayoutData() == null && component.getStyleName() == null) {
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
     * Merge the style of a component with that of a {@code TableLayoutData}.
     *
     * @param component  the component to merge with
     * @param layoutData the layout data to merge from
     * @param overwrite  if {@code true} overwrite existing component properties if the specified style has non-null
     *                   corresponding properties
     */
    public static void mergeStyle(Component component, TableLayoutData layoutData, boolean overwrite) {
        if (component.getLayoutData() == null) {
            component.setLayoutData(new TableLayoutData());
        }
        mergeLayoutData((TableLayoutData) component.getLayoutData(), layoutData, overwrite);
    }

    /**
     * Merges table layout data with a style defined by the stylesheet.
     *
     * @param layout    the layout data
     * @param styleName the style name
     */
    public static void mergeStyle(TableLayoutData layout, String styleName) {
        ApplicationInstance app = ApplicationInstance.getActive();
        Style style = app.getStyle(Component.class, styleName);
        if (style != null) {
            LayoutData from = (LayoutData) style.getProperty("layoutData");
            if (from instanceof TableLayoutData) {
                mergeLayoutData(layout, (TableLayoutData) from, false);
            }
        }
    }

    /**
     * Merge style from a stylesheet with a component's properties
     *
     * @param style     the style
     * @param component the component
     * @param overwrite if {@code true} overwrite existing component properties if the specified style has non-null
     *                  corresponding properties
     */
    private static void mergeStyle(Style style, Component component, boolean overwrite) {
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
                    mergeLayoutData(to, from, overwrite);
                }
            } else if (overwrite || component.getProperty(name) == null) {
                Object value = style.getProperty(name);
                if (value != null) {
                    try {
                        BeanUtils.setProperty(component, name, value);

                        if (value instanceof TableLayoutData) {
                            Color background = ((TableLayoutData) value).getBackground();
                            if (background != null) {
                                // layout sets a background, so adjust foreground colours so text doesn't become
                                // unreadable
                                setForeground(component, ColourHelper.getTextColour(background));
                            }
                        }
                    } catch (Throwable ignore) {
                        // no-op
                    }
                }
            }
        }
    }

    /**
     * Recursively sets the foreground colour of a component and its children.
     *
     * @param component the component
     * @param colour    the foreground colour
     */
    private static void setForeground(Component component, Color colour) {
        component.setForeground(colour);
        for (Component child : component.getComponents()) {
            setForeground(child, colour);
        }
    }

    /**
     * Merge layout data.
     *
     * @param to        the layout data to merge to
     * @param from      the layout data to merge from
     * @param overwrite if {@code true} overwrite existing component properties if the specified style has non-null
     *                  corresponding
     */
    private static void mergeLayoutData(TableLayoutData to, TableLayoutData from, boolean overwrite) {
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
