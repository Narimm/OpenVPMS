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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.contact;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ButtonFactory;


/**
 * An {@link IMObjectLayoutStrategy} for <em>contact.emailAddress</em> that enables mail messages to be sent.
 *
 * @author Tim Anderson
 */
public class EmailContactViewLayout extends AbstractLayoutStrategy {

    /**
     * Creates a component for a property.
     * <p/>
     * If there is a pre-existing component, registered via {@link #addComponent}, this will be returned.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display {@code property}
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent, final LayoutContext context) {
        ComponentState result = null;
        if ("emailAddress".equals(property.getName())) {
            final Contact contact = (Contact) parent;
            final String mail = ContactHelper.getEmail(contact);
            if (!StringUtils.isEmpty(mail) && context.getMailContext() != null) {
                Button link = ButtonFactory.create(null, "hyperlink", new ActionListener() {
                    public void onAction(ActionEvent event) {
                        MailDialog dialog = new MailDialog(context.getMailContext(), contact, context.getContext(),
                                                           context.getHelpContext());
                        dialog.show();
                    }
                });
                link.setText(ContactHelper.getEmail(contact));
                result = new ComponentState(link, property);
            }
        }
        if (result == null) {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }
}