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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ComponentHelper;


/**
 * Post dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PostDialog extends PrintDialog {

    /**
     * Determines if the act should be printed.
     */
    private CheckBox print;


    /**
     * Constructs a new <code>PostDialog</code>.
     *
     * @param title the window title
     */
    public PostDialog(String title) {
        super(title, false);
    }

    /**
     * Determines if the act should be printed.
     *
     * @return <code>true</code> if the act should be printed,
     *         otherwise <code>false</code>
     */
    public boolean print() {
        return print.isSelected();
    }

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        print = CheckBoxFactory.create("postdialog.print");
        print.setSelected(true);
        print.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onPrintChanged();
            }
        });
        container.add(print);
        super.doLayout(container);
    }

    /**
     * Invoked when the 'print' checkbox is selected.
     */
    private void onPrintChanged() {
        boolean enabled = print();
        ComponentHelper.enable(getPrinterLabel(), enabled);
        ComponentHelper.enable(getPrinters(), enabled);
    }

}
