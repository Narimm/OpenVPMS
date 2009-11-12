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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.web.component.im.util.PrintHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Print dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintDialog extends PopupDialog {

    /**
     * The print label.
     */
    private Label label;

    /**
     * The printers.
     */
    private final SelectField printers;

    /**
     * Determines if the preview button should be added.
     */
    private final boolean preview;

    /**
     * The preview button identifier.
     */
    private static final String PREVIEW_ID = "preview";


    /**
     * Constructs a new <tt>PrintDialog</tt>.
     */
    public PrintDialog() {
        this(Messages.get("printdialog.title"));
    }

    /**
     * Constructs a new <tt>PrintDialog</tt>.
     *
     * @param title the window title
     */
    public PrintDialog(String title) {
        this(title, true);
    }

    /**
     * Constructs a new <tt>PrintDialog</tt>.
     *
     * @param title   the window title
     * @param preview if <tt>true</tt> add a 'preview' button
     */
    public PrintDialog(String title, boolean preview) {
        this(title, preview, false);
    }

    /**
     * Constructs a new <tt>PrintDialog</tt>.
     *
     * @param title   the window title
     * @param preview if <tt>true</tt> add a 'preview' button
     * @param skip    if <tt>triue</tt> display a 'skip' button that simply
     *                closes the dialog
     */
    public PrintDialog(String title, boolean preview, boolean skip) {
        super(title, "PrintDialog", (skip) ? OK_SKIP_CANCEL : OK_CANCEL);
        setModal(true);
        DefaultListModel model = new DefaultListModel(
                PrintHelper.getPrinters());
        printers = SelectFieldFactory.create(model);
        this.preview = preview;
    }

    /**
     * Sets the default printer.
     *
     * @param name the default printer name. May be <tt>null</tt>
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
     * @return the selected printer, or <tt>null</tt> if none is selected
     */
    public String getPrinter() {
        return (String) printers.getSelectedItem();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create("WideCellSpacing");
        doLayout(column);
        getLayout().add(ColumnFactory.create("Inset", column));
    }

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        label = LabelFactory.create("printdialog.printer");
        if (preview) {
            addButton(PREVIEW_ID, new ActionListener() {
                public void onAction(ActionEvent e) {
                    onPreview();
                }
            });
        }

        Column column = ColumnFactory.create("CellSpacing", label,
                                             RowFactory.create(printers));
        // wrap printers in a row so it renders min width on FF

        container.add(column);
    }

    /**
     * Returns the printer label.
     *
     * @return the printer label
     */
    protected Label getPrinterLabel() {
        return label;
    }

    /**
     * Returns the printers dropdown.
     *
     * @return the printers dropdown
     */
    protected SelectField getPrinters() {
        return printers;
    }

    /**
     * Invoked when the preview button is pressed.
     * This implementation does nothing.
     */
    protected void onPreview() {
    }

}
