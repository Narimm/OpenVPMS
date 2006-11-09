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
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
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
     * The list box.
     */
    private ListBox _list;

    /**
     * The selected value.
     */
    private Object _selected;

    /**
     * The selected index;
     */
    private int _index = -1;

    /**
     * Dialog style name.
     */
    private static final String STYLE = "SelectionDialog";

    /**
     * Content label style.
     */
    private static final String LABEL_STYLE = "SelectionDialog.Label";


    /**
     * Creates a new <code>SelectionDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param list    the list of items to select from
     */
    public SelectionDialog(String title, String message, List list) {
        this(title, message, new DefaultListModel(list.toArray()));
    }

    /**
     * Creates a new <code>SelectionDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param list    the list of items to select from
     */
    public SelectionDialog(String title, String message, ListModel list) {
        this(title, message, new ListBox(list));
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

        _list = list;
        _list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelected();
            }
        });
        Label prompt = LabelFactory.create(null, LABEL_STYLE);
        prompt.setText(message);
        Column column = ColumnFactory.create(prompt, _list);
        getLayout().add(column);
    }

    /**
     * Returns the selected item.
     *
     * @return the selected item, or <code>null</code> if no item was selected.
     */
    public Object getSelected() {
        return _selected;
    }

    /**
     * Returns the selected index.
     *
     * @return the selected index, or <code>-1</code> if no item was selected.
     */
    public int getSelectedIndex() {
        return _index;
    }

    /**
     * Get the selected object (if any), and close the window.
     */
    protected void onSelected() {
        _selected = _list.getSelectedValue();
        _index = _list.getSelectionModel().getMinSelectedIndex();
        close();
    }
}
