package org.openvpms.web.component.palette;

import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListSelectionModel;
import org.apache.commons.collections.ListUtils;

import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.LabelFactory;


/**
 * A component used to make a number of selections from a list. Items available
 * for selection are displayed in an 'available' list box. Selected Items are
 * displayed in a 'selected' list box. Items are moved from one to the other
 * using a pair of buttons.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Palette extends Row {

    /**
     * The set of unselected items.
     */
    private final List _unselected;

    /**
     * The set of selected items.
     */
    private final List _selected;

    /**
     * The unselected item list box.
     */
    private ListBox _unselectedList;

    /**
     * The selected item list box.
     */
    private ListBox _selectedList;

    /**
     * Construct a new <code>Palette</code>,
     *
     * @param items    all items that may be selected
     * @param selected the selected items
     */
    public Palette(List items, List selected) {
        setStyleName("Palette");
        _selected = selected;
        _unselected = ListUtils.subtract(items, selected);

        doLayout();
    }

    /**
     * Set the cell renderer.
     *
     * @param renderer the cell renderer
     */
    public void setCellRenderer(ListCellRenderer renderer) {
        _unselectedList.setCellRenderer(renderer);
        _selectedList.setCellRenderer(renderer);
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        final int mode = ListSelectionModel.MULTIPLE_SELECTION;
        _unselectedList = new ListBox(_unselected.toArray());
        _unselectedList.setStyleName("Palette.List");
        _unselectedList.setSelectionMode(mode);

        _selectedList = new ListBox(_selected.toArray());
        _selectedList.setStyleName("Palette.List");
        _selectedList.setSelectionMode(mode);

        Button add = ButtonFactory.create("right_add", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });
        Button remove = ButtonFactory.create("left_remove", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRemove();
            }
        });
        Label available = LabelFactory.create("available", "Palette.ListLabel");
        Label selected = LabelFactory.create("selected", "Palette.ListLabel");
        Column left = ColumnFactory.create("Palette.ListColumn", available, _unselectedList);
        Column middle = ColumnFactory.create("ControlColumn", add, remove);
        Column right = ColumnFactory.create("Palette.ListColumn", selected, _selectedList);
        add(left);
        add(middle);
        add(right);
    }

    /**
     * Invoked when the add button is pressed.
     */
    protected void onAdd() {
        Object[] values = _unselectedList.getSelectedValues();
        move(_unselectedList, _selectedList);
        add(values);
    }

    /**
     * Invoked when the remove button is pressed.
     */
    protected void onRemove() {
        Object[] values = _selectedList.getSelectedValues();
        move(_selectedList, _unselectedList);
        remove(values);
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    protected void add(Object[] values) {
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    protected void remove(Object[] values) {
    }

    /**
     * Move items from one list to another.
     *
     * @param from the source list box
     * @param to   the target list box
     */
    protected void move(ListBox from, ListBox to) {
        Object[] values = from.getSelectedValues();
        DefaultListModel toModel = (DefaultListModel) to.getModel();
        DefaultListModel fromModel = (DefaultListModel) from.getModel();
        for (Object value : values) {
            toModel.add(value);
            fromModel.remove(value);
        }
    }

}
