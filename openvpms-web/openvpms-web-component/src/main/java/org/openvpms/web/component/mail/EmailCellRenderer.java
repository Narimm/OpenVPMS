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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;

/**
 * Renders an email address using an {@link AddressFormatter}..
 *
 * @author Tim Anderson
 */
class EmailCellRenderer extends AbstractListCellRenderer<Contact> {

    /**
     * The address formatter.
     */
    private final AddressFormatter formatter;

    /**
     * Constructs an {@code EmailCellRenderer}.
     *
     * @param formatter the address formatter
     */
    public EmailCellRenderer(AddressFormatter formatter) {
        super(Contact.class);
        this.formatter = formatter;
    }

    /**
     * Renders an object.
     *
     * @param list   the list component
     * @param object the object to render. May be {@code null}
     * @param index  the object index
     * @return the rendered object
     */
    protected Object getComponent(Component list, Contact object, int index) {
        return (object != null) ? formatter.format(object) : null;
    }

    /**
     * Determines if an object represents 'All'.
     *
     * @param list   the list component
     * @param object the object. May be {@code null}
     * @param index  the object index
     * @return {@code false}
     */
    protected boolean isAll(Component list, Contact object, int index) {
        return false;
    }

    /**
     * Determines if an object represents 'None'.
     *
     * @param list   the list component
     * @param object the object. May be {@code null}
     * @param index  the object index
     * @return {@code false}
     */
    protected boolean isNone(Component list, Contact object, int index) {
        return false;
    }
}
