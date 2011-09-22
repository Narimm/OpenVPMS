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
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.contact;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.List;


/**
 * A {@link IMObjectLayoutStrategy} for <em>contact.phoneNumber</em> that enables SMS messages to be sent to mobiles.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PhoneContactViewLayout extends AbstractLayoutStrategy {

    /**
     * Lays out child components in a grid.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be <tt>null</tt>
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container, LayoutContext context) {
        IMObjectBean bean = new IMObjectBean(object);
        final String phone = bean.getString("telephoneNumber");
        if (bean.getBoolean("sms") && !StringUtils.isEmpty(phone)) {
            Button send = ButtonFactory.create("button.sms.send");
            send.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onSend(phone);
                }
            });
            RowLayoutData rowLayout = new RowLayoutData();
            Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
            rowLayout.setAlignment(topRight);
            send.setLayoutData(rowLayout);
            Grid grid = createGrid(descriptors);
            doGridLayout(object, descriptors, properties, grid, context);
            Row row = RowFactory.create("WideCellSpacing", grid);
            ButtonSet set = new ButtonSet(row);
            set.add(send);
            container.add(ColumnFactory.create("Inset.Small", row));
        } else {
            super.doSimpleLayout(object, parent, descriptors, properties, container, context);
        }
    }

    /**
     * Displays an SMS dialog to send a message to the specified phone.
     *
     * @param phoneNumber the phone number
     */
    private void onSend(String phoneNumber) {
        SMSDialog dialog = new SMSDialog(phoneNumber);
        dialog.show();
    }
}
