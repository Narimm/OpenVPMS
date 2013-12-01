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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.DateFormat;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author Tim Anderson
 */
public class ReadOnlyComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Creates a new <tt>ReadOnlyComponentFactory</tt>.
     *
     * @param context the layout context
     */
    public ReadOnlyComponentFactory(LayoutContext context) {
        this(context, Styles.DEFAULT);
    }

    /**
     * Creates a new <tt>ReadOnlyComponentFactory</tt>.
     *
     * @param context the layout context
     * @param style   the style name to use
     */
    public ReadOnlyComponentFactory(LayoutContext context, String style) {
        super(context, new ViewLayoutStrategyFactory(), style);
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param property the property
     * @param context  the context object
     * @return a component to display the lookup
     */
    protected Component createLookup(Property property, IMObject context) {
        TextComponent result;
        final int maxDisplayLength = 50;
        int length = property.getMaxLength();
        int columns = (length < maxDisplayLength) ? length : maxDisplayLength;
        String value = null;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor != null) {
            value = LookupNameHelper.getLookupName(descriptor, context);
            if (value != null && value.length() > 0) {
                if (value.length() < columns) {
                    columns = value.length();
                }
            }
        }
        result = TextComponentFactory.create(columns);
        result.setText(value);
        ComponentFactory.setStyle(result, getStyle());
        return result;
    }

    /**
     * Returns a component to display a date.
     *
     * @param property the property to bind the field to
     * @return a component to display the date
     */
    protected Component createDate(Property property) {
        DateFormat format = DateFormatter.getDateFormat(false);
        int maxColumns = DateFormatter.getLength(format);
        TextField result = BoundTextComponentFactory.create(property, maxColumns, format);
        ComponentFactory.setStyle(result, getStyle());
        return result;
    }

}
