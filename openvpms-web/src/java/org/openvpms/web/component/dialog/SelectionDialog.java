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
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.list.KeyListBox;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;

import java.util.List;


/**
 * A modal dialog that prompts the user to select an item from a list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-04-03 06:42:28Z $
 */
public class SelectionDialog extends PopupDialog {

    /**
     * The list.
     */
    private AbstractListComponent list;

    /**
     * The selected value.
     */
    private Object selected;

    /**
     * The selected index;
     */
    private int index = -1;

    /**
     * The focus, prior to the dialog being shown.
     */
    private FocusCommand focus;

    /**
     * Dialog style name.
     */
    private static final String STYLE = "SelectionDialog";


    /**
     * Creates a new <tt>SelectionDialog</tt>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param list    the list of items to select from
     */
    public SelectionDialog(String title, String message, List list) {
        this(title, message, new DefaultListModel(list.toArray()));
    }

    /**
     * Creates a new <tt>SelectionDialog</tt>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param list    the list of items to select from
     */
    public SelectionDialog(String title, String message, ListModel list) {
        this(title, message, new KeyListBox(list));
    }

    /**
     * Creates a new <code>SelectionDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param list    the list of items to select from
     */
    public SelectionDialog(String title, String message, ListBox list) {
        super(title, STYLE, CANCEL);
        setModal(true);
        focus = new FocusCommand();

        this.list = list;
        if (list.getSelectedValue() == null && list.getModel().size() != 0) {
            list.getSelectionModel().setSelectedIndex(0, true);
        }
        this.list.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected();
            }
        });
        Label prompt = LabelFactory.create(true, true);
        prompt.setStyleName("bold");
        prompt.setText(message);
        Column column = ColumnFactory.create("CellSpacing", prompt, this.list);
        getLayout().add(ColumnFactory.create("Inset", column));
        getFocusGroup().add(list);
        FocusHelper.setFocus(list);
    }

    /**
     * Returns the selected item.
     *
     * @return the selected item, or <tt>null</tt> if no item was selected.
     */
    public Object getSelected() {
        return selected;
    }

    /**
     * Returns the selected index.
     *
     * @return the selected index, or <tt>-1</tt> if no item was selected.
     */
    public int getSelectedIndex() {
        return index;
    }

    /**
     * Processes a user request to close the window
     * <p/>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        focus.restore();
        super.userClose();
    }

    /**
     * Get the selected object (if any), and close the window.
     */
    protected void onSelected() {
        index = list.getSelectionModel().getMinSelectedIndex();
        selected = (index != -1) ? list.getModel().get(index) : null;
        close();
    }

}
