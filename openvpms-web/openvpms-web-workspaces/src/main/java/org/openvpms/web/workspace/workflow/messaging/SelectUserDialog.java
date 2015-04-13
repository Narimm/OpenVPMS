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
 */

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Dialog to select a user.
 *
 * @author Tim Anderson
 */
public class SelectUserDialog extends PopupDialog {

    /**
     * The selector.
     */
    private final IMObjectSelector<User> selector;


    /**
     * Constructs a {@code SelectUserDialog}.
     *
     * @param title   the window title
     * @param context the context
     * @param help    the help context
     */
    public SelectUserDialog(String title, Context context, HelpContext help) {
        super(title, "SelectUserDialog", CANCEL);

        String shortName = UserArchetypes.USER;
        String type = DescriptorHelper.getDisplayName(shortName);
        selector = new IMObjectSelector<User>(type, new DefaultLayoutContext(context, help), shortName);
        selector.setListener(new AbstractIMObjectSelectorListener<User>() {
            public void selected(User object) {
                onSelected(object);
            }
        });

        Label label = LabelFactory.create("username");
        Row row = RowFactory.create("CellSpacing", label,
                                    selector.getComponent());
        getLayout().add(row);
    }

    /**
     * Returns the selected user.
     *
     * @return the selected user
     */
    public User getUser() {
        return selector.getObject();
    }

    /**
     * Invoked when a user is selected. If non-null, closes the dialog.
     *
     * @param object the selected user. May be {@code null}
     */
    private void onSelected(IMObject object) {
        if (object != null) {
            close(OK_ID);
        }
    }


}
