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

package org.openvpms.web.component.print;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.SpinBox;
import org.openvpms.web.resource.util.Messages;


/**
 * Print dialog.
 *
 * @author Tim Anderson
 */
public class PrintDialog extends PopupDialog {

    /**
     * The printers.
     */
    private final SelectField printers;

    /**
     * Determines if the preview button should be added.
     */
    private final boolean preview;

    /**
     * Determines if the mail button should be added.
     */
    private final boolean mail;

    /**
     * The preview button identifier.
     */
    private static final String PREVIEW_ID = "preview";

    /**
     * The mail button identifier.
     */
    private static final String MAIL_ID = "mail";

    /**
     * The no. of copies to print.
     */
    private SpinBox copies;


    /**
     * Constructs a {@code PrintDialog}.
     */
    public PrintDialog() {
        this(Messages.get("printdialog.title"));
    }

    /**
     * Constructs a {@code PrintDialog}.
     *
     * @param title the window title
     */
    public PrintDialog(String title) {
        this(title, true);
    }

    /**
     * Constructs a {@code PrintDialog}.
     *
     * @param title   the window title
     * @param preview if {@code true} add a 'preview' button
     */
    public PrintDialog(String title, boolean preview) {
        this(title, preview, true, false);
    }

    /**
     * Constructs a {@code PrintDialog}.
     *
     * @param title   the window title
     * @param preview if {@code true} add a 'preview' button
     * @param mail    if {@code true} add a 'mail' button
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     */
    public PrintDialog(String title, boolean preview, boolean mail, boolean skip) {
        this(title, preview, mail, skip, null);
    }

    /**
     * Constructs a {@code PrintDialog}.
     *
     * @param title   the window title
     * @param preview if {@code true} add a 'preview' button
     * @param mail    if {@code true} add a 'mail' button
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param help    the help context. May be {@code null}
     */
    public PrintDialog(String title, boolean preview, boolean mail, boolean skip, HelpContext help) {
        super(title, "PrintDialog", (skip) ? OK_SKIP_CANCEL : OK_CANCEL, help);
        setModal(true);
        copies = new SpinBox(1, 99);
        DefaultListModel model = new DefaultListModel(
            PrintHelper.getPrinters());
        printers = SelectFieldFactory.create(model);
        this.preview = preview;
        this.mail = mail;
    }

    /**
     * Sets the default printer.
     *
     * @param name the default printer name. May be {@code null}
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
     * @return the selected printer, or {@code null} if none is selected
     */
    public String getPrinter() {
        return (String) printers.getSelectedItem();
    }

    /**
     * Sets the number of copies to print.
     *
     * @param copies the number of copies to print
     */
    public void setCopies(int copies) {
        this.copies.setValue(copies);
    }

    /**
     * Returns the number of copies to print.
     *
     * @return the number of copies to print
     */
    public int getCopies() {
        return copies.getValue();
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
        if (preview) {
            addButton(PREVIEW_ID, new ActionListener() {
                public void onAction(ActionEvent e) {
                    onPreview();
                }
            });
        }
        if (mail) {
            addButton(MAIL_ID, new ActionListener() {
                public void onAction(ActionEvent e) {
                    onMail();
                }
            });
        }

        FocusGroup parent = getFocusGroup();
        FocusGroup child = new FocusGroup("PrintDialog");
        child.add(printers);
        child.add(copies.getFocusGroup());
        parent.add(0, child); // insert before buttons

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("printdialog.printer"));
        grid.add(printers);
        grid.add(LabelFactory.create("printdialog.copies"));
        grid.add(copies);

        setFocus(copies);

        container.add(grid);
    }

    /**
     * Invoked when the preview button is pressed.
     * This implementation does nothing.
     */
    protected void onPreview() {
    }

    /**
     * Invoked when the mail button is pressed.
     * This implementation does nothing.
     */
    protected void onMail() {
    }

}
