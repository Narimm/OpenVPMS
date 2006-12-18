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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;


/**
 * Print dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintDialog extends PopupDialog {

    /**
     * The preview button identifier.
     */
    public static final String PREVIEW_ID = "preview";

    /**
     * The printers.
     */
    private SelectField printers;


    /**
     * Construct a new <code>PrintDialog</code>.
     *
     * @param title the window title
     */
    public PrintDialog(String title) {
        super(title, "PrintDialog", OK_CANCEL);
        setModal(true);

        Label label = LabelFactory.create("printdialog.printer");
        DefaultListModel model = new DefaultListModel(getPrinters());
        printers = SelectFieldFactory.create(model);
        Row row = RowFactory.create("ControlRow", label, printers);
        getLayout().add(row);
        addButton(PREVIEW_ID, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onPreview();
            }
        });

        PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
        if (printer != null) {
            setDefaultPrinter(printer.getName());
        }
    }

    /**
     * Sets the default printer.
     *
     * @param name the default printer name. May be <code>null</code>
     */
    public void setDefaultPrinter(String name) {
        DefaultListModel model = (DefaultListModel) printers.getModel();
        int index = model.indexOf(name);
        if (index != -1) {
            printers.setSelectedIndex(index);
        }
    }

    /**
     * Returns the selected printer.
     *
     * @return the selected printer, or <code>null</code> if none is selected
     */
    public String getPrinter() {
        return (String) printers.getSelectedItem();
    }

    /**
     * Invoked when the preview button is pressed. This sets the action and
     * closes the window.
     */
    protected void onPreview() {
        setAction(PREVIEW_ID);
        close();
    }

    /**
     * Returns a list of the available printers.
     *
     * @return a list of the available printers
     */
    private String[] getPrinters() {
        PrintService[]  printers = PrintServiceLookup.lookupPrintServices(
                null, null);
        String[] names = new String[printers.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = printers[i].getName();
        }
        return names;
    }

}
