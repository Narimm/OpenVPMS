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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.layout.TableLayoutStrategyFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.NumericPropertyFormatter;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.util.StyleSheetHelper;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.math.BigDecimal;
import java.util.Date;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components for display in a table.
 *
 * @author Tim Anderson
 */
public class TableComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Determines if long text should be truncated.
     */
    private boolean truncateLongText;

    /**
     * The maximum length, when {@code truncateLongText = true}.
     */
    private int maxLength = MAX_LENGTH;

    /**
     * The default maximum length, when {@code truncateLongText = true}.
     */
    private static final int MAX_LENGTH = 30;

    /**
     * The minimum supported length, when {@code truncateLongText = true}.
     */
    private static final int MIN_LENGTH = 5;

    /**
     * Constructs a {@link TableComponentFactory}.
     *
     * @param context the layout context.
     */
    public TableComponentFactory(LayoutContext context) {
        super(context, new TableLayoutStrategyFactory(), Styles.DEFAULT);
    }

    /**
     * Determines if long text should be truncated.
     *
     * @param truncate if {@code true}, truncate long text
     */
    public void setTruncateLongText(boolean truncate) {
        this.truncateLongText = truncate;
        if (truncateLongText) {
            maxLength = StyleSheetHelper.getProperty("table.text.maxlength", MAX_LENGTH);
            if (maxLength < MIN_LENGTH) {
                maxLength = MIN_LENGTH;
            }
        }
    }

    /**
     * Returns a component to display a lookup property.
     *
     * @param property the lookup property
     * @param context  the context object
     * @return a component to display the lookup property
     */
    @Override
    protected Component createLookup(Property property, IMObject context) {
        String name = LookupNameHelper.getName(context, property.getName());
        return createLabel(name, false);
    }

    /**
     * Returns a component to display a string property.
     *
     * @param property the boolean property
     * @return a component to display the property
     */
    @Override
    protected Component createString(Property property) {
        return createLabel(property);
    }

    /**
     * Returns a component to display a numeric property.
     *
     * @param property the numeric property
     * @return a component to display the property
     */
    @Override
    protected Component createNumeric(Property property) {
        String value = getNumericValue(property);
        return TableHelper.rightAlign(value);
    }

    /**
     * Returns a component to display a date property.
     *
     * @param property the date property
     * @return a component to display the property
     */
    @Override
    protected Component createDate(Property property) {
        String value = getDateValue(property);
        Label label = LabelFactory.create();
        label.setText(value);
        return label;
    }

    /**
     * Helper to convert a numeric property to string.
     *
     * @param property the numeric property
     * @return the string value of the property associated with {@code property}
     */
    protected String getNumericValue(Property property) {
        Object tmp = property.getValue();
        Number value;
        if (tmp instanceof String) {
            value = new BigDecimal((String) tmp);
        } else {
            value = (Number) tmp;
        }
        if (value != null) {
            return NumericPropertyFormatter.format(value, property, false);
        }
        return null;
    }

    /**
     * Helper to convert a date value to a string.
     *
     * @param property the date property
     * @return the string value of the property
     */
    protected String getDateValue(Property property) {
        Date value = (Date) property.getValue();
        return (value != null) ? DateFormatter.formatDate(value, false) : null;
    }

    /**
     * Returns a component bound to a boolean property.
     *
     * @param property the property to bind
     * @return a new component
     */
    @Override
    protected Component createBoolean(Property property) {
        // force the check-box to render to minimum width, so that it doesn't fill the cell. This allows row selection
        // events
        return RowFactory.create(RowFactory.create(super.createBoolean(property)));
    }

    /**
     * Returns a component for an empty collection.
     *
     * @return the component
     */
    @Override
    protected Component getEmptyCollectionViewer() {
        return LabelFactory.create();
    }

    /**
     * Returns a label to display a property.
     *
     * @param text      the text to display. May be {@code null}
     * @param multiline if {@code true}, interprets new lines in the text
     * @return a new label
     */
    @Override
    protected Label createLabel(String text, boolean multiline) {
        Label label = LabelFactory.create(multiline);
        if (truncateLongText && text != null && text.length() > maxLength) {
            label.setText(StringUtils.abbreviateMiddle(text, "...", maxLength));
            label.setToolTipText(text);
        } else {
            label.setText(text);
        }
        return label;
    }

    /**
     * Creates a component to view a document.
     *
     * @param reference the reference to view. May be {@code null}
     * @param listener  the listener to notify. May be {@code null}
     * @param layout    the layout context
     * @return a component to view the document
     */
    @Override
    protected Component getObjectViewer(Reference reference, ContextSwitchListener listener, LayoutContext layout) {
        String name = null;
        String tooltip = null;
        if (truncateLongText) {
            IMObjectCache cache = layout.getCache();
            if (cache.exists(reference)) {
                IMObject object = cache.get(reference);
                if (object != null) {
                    name = object.getName();
                }
            } else {
                name = IMObjectHelper.getName(reference);
            }
            if (name != null && name.length() > maxLength) {
                tooltip = name;
                name = StringUtils.abbreviateMiddle(name, "...", maxLength);
            }
        }
        return new IMObjectReferenceViewer(reference, name, tooltip, listener, layout.getContext()).getComponent();
    }
}
