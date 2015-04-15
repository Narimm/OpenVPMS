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

package org.openvpms.web.component.im.contact;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.sms.SMSDialog;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;

import java.util.List;


/**
 * A {@link IMObjectLayoutStrategy} for <em>contact.phoneNumber</em> that enables SMS messages to be sent to mobiles.
 *
 * @author Tim Anderson
 */
public class PhoneContactViewLayout extends AbstractLayoutStrategy {

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be <tt>null</tt>
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(final IMObject object, IMObject parent, List<Property> properties,
                                  Component container, final LayoutContext context) {
        IMObjectBean bean = new IMObjectBean(object);
        final String phone = bean.getString("telephoneNumber");
        Party practice = context.getContext().getPractice();
        if (bean.getBoolean("sms") && !StringUtils.isEmpty(phone) && SMSHelper.isSMSEnabled(practice)) {
            Button send = ButtonFactory.create("button.sms.send");
            send.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onSend((Contact) object, context);
                }
            });
            RowLayoutData rowLayout = new RowLayoutData();
            Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
            rowLayout.setAlignment(topRight);
            send.setLayoutData(rowLayout);
            ComponentGrid grid = createGrid(object, properties, context);
            Row row = RowFactory.create("WideCellSpacing", createGrid(grid));
            ButtonSet set = new ButtonSet(row);
            set.add(send);
            container.add(ColumnFactory.create("Inset.Small", row));
        } else {
            super.doSimpleLayout(object, parent, properties, container, context);
        }
    }

    /**
     * Displays an SMS dialog to send a message to the specified phone.
     *
     * @param contact the phone contact
     * @param context the layout context
     */
    private void onSend(Contact contact, LayoutContext context) {
        HelpContext help = context.getHelpContext().subtopic("sms");
        SMSDialog dialog = new SMSDialog(contact, context.getContext(), help);
        dialog.show();
    }
}
