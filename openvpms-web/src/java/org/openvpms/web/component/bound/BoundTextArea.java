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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextArea;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.util.TextDocument;


/**
 * Binds a {@link Property} to a <tt>TextArea</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundTextArea extends TextArea {

    /**
     * The binder.
     */
    public Binder binder;


    /**
     * Constructs a new <tt>BoundTextArea</tt>.
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param rows     the no. of rows to display
     */
    public BoundTextArea(Property property, int columns, int rows) {
        super(new TextDocument());
        setWidth(new Extent(columns, Extent.EX));
        setHeight(new Extent(rows, Extent.EM));
        binder = new TextComponentBinder(this, property);
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
        PropertyTransformer transformer = property.getTransformer();
        if (!(transformer instanceof StringPropertyTransformer)) {
            property.setTransformer(new StringPropertyTransformer(property, false));
        } else {
            ((StringPropertyTransformer) transformer).setTrim(false);
        }
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

}
