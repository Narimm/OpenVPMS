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

package org.openvpms.web.echo.message;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * A message that can be displayed inline and removed by clicking on a close button.
 *
 * @author Tim Anderson
 */
public class InformationMessage extends Row {

    /**
     * Constructs a new {@link InformationMessage}.
     *
     * @param message the message to display
     */
    public InformationMessage(String message) {
        setStyleName(Styles.INSET);
        Label label = LabelFactory.create(null, true);
        label.setStyleName("InformationMessage");
        label.setText(message);
        Button button = ButtonFactory.create(null, "Message.close");
        button.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                if (getParent() != null) {
                    getParent().remove(InformationMessage.this);
                }
            }
        });
        Row row = RowFactory.create("InformationMessage.row",
                                    RowFactory.create(Styles.WIDE_CELL_SPACING, label, button));
        add(row);
    }

}
