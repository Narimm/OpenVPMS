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
import nextapp.echo2.app.PasswordField;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.text.TextDocument;


/**
 * Binds a {@link Property} to a <code>PasswordField</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundPasswordField extends PasswordField {

    /**
     * The binder.
     */
    public Binder binder;


    /**
     * Constructs a <tt>BoundPasswordField</tt>.
     *
     * @param property the property to bind
     */
    public BoundPasswordField(Property property) {
        super();
        setDocument(new TextDocument());
        int columns = property.getMaxLength();
        if (columns > 20) {
            columns = 20;
        }
        if (columns <= 10) {
            setWidth(new Extent(columns, Extent.EM));
        } else {
            setWidth(new Extent(columns, Extent.EX));
        }

        binder = new TextComponentBinder(this, property);
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
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