package org.openvpms.web.component.dialog;

import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;

import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;


/**
 * A modal dialog that prompts the user to select an item from a list.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
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
        super(title, STYLE, Buttons.OK_CANCEL);
        setModal(true);

        _list = new ListBox(list);
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
    @Override
    protected void onOK() {
        _selected = _list.getSelectedValue();
        _index = _list.getSelectionModel().getMinSelectedIndex();
        super.onOK();
    }
}
