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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.bound;

import echopointng.RichTextArea;
import nextapp.echo2.app.Extent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.TextDocument;


/**
 * Bound rich text area.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundRichTextArea extends RichTextArea {

    /**
     * The binder.
     */
    public Binder binder;


    /**
     * Constructs a <tt>BoundRichTextArea</tt>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     */
    public BoundRichTextArea(Property property, int columns) {
        super(new TextDocument());
        setWidth(new Extent(columns, Extent.EM));
        binder = new TextComponentBinder(this, property);
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
