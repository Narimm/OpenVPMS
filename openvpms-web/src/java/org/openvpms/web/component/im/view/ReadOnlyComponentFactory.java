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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.layout.DefaultLayoutStrategyFactory;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.TextComponentFactory;

import java.text.DateFormat;
import java.text.Format;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ReadOnlyComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <code>ReadOnlyComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public ReadOnlyComponentFactory(LayoutContext context) {
        super(context, new DefaultLayoutStrategyFactory());
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param property
     * @return a component to display the lookup
     */
    protected Component getLookup(Property property) {
        return getTextComponent(property);
    }

    /**
     * Returns a component to display a number.
     *
     * @param property
     * @return a component to display the datge
     */
    protected Component getNumber(Property property) {
        int maxColumns = 10;
        NodeDescriptor descriptor = property.getDescriptor();
        Format format = NumberFormatter.getFormat(descriptor, false);
        TextField text = TextComponentFactory.create(property, maxColumns,
                                                     format);
        Alignment align = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        text.setAlignment(align);
        return text;
    }

    /**
     * Returns a component to display a date.
     *
     * @param property
     * @return a component to display the datge
     */
    protected Component getDate(Property property) {
        DateFormat format = DateFormatter.getDateFormat(false);
        int maxColumns = DateFormatter.getLength(format);
        return TextComponentFactory.create(property, maxColumns, format);
    }

}
