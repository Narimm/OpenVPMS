package org.openvpms.web.component.table;

import echopointng.table.PageableTableModel;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.list.DefaultListModel;

import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * A controller for tables containing <code>PageableTableModel</code> backed
 * tables.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class TableNavigator extends Row {

    /**
     * The underlying table.
     */
    private Table _table;

    /**
     * The page selector combobox.
     */
    private SelectField _pageSelector;

    /**
     * The page count.
     */
    private Label _pageCount;


    /**
     * Construct a new <code>TableNavigator</code>.
     *
     * @param table the table to navigate
     */
    public TableNavigator(Table table) {
        _table = table;
        _table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                refresh();
            }
        });
        doLayout();
    }

    protected void doLayout() {
        setCellSpacing(new Extent(10));

        Label page = LabelFactory.create("navigation.page");

        Button first = ButtonFactory.create(null, "navigation.first", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doFirst();
            }
        });
        Button previous = ButtonFactory.create(null, "navigation.previous", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doPrevious();
            }
        });

        _pageSelector = new SelectField();
        _pageSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int selected = _pageSelector.getSelectedIndex();
                PageableTableModel model = getModel();
                model.setCurrentPage(selected);
            }
        });

        _pageCount = LabelFactory.create();

        Button next = ButtonFactory.create(null, "navigation.next", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doNext();
            }
        });

        Button last = ButtonFactory.create(null, "navigation.last", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doLast();
            }
        });

        add(page);
        add(first);
        add(previous);
        add(_pageSelector);
        add(next);
        add(last);
        add(_pageCount);

        refresh();
    }

    protected void doFirst() {
        int page = getModel().getCurrentPage();
        if (page != 0) {
            setCurrentPage(0);
        }
    }

    protected void setCurrentPage(int page) {
        getModel().setCurrentPage(page);
        _pageSelector.setSelectedIndex(page);
    }

    protected void doPrevious() {
        PageableTableModel model = getModel();
        int page = model.getCurrentPage();
        if (page > 0) {
            setCurrentPage(page - 1);
        }
    }

    protected void doNext() {
        PageableTableModel model = getModel();
        int maxPage = model.getTotalRows() / model.getRowsPerPage();
        int page = model.getCurrentPage();
        if (page < maxPage) {
            setCurrentPage(page + 1);
        }
    }

    protected void doLast() {
        PageableTableModel model = getModel();
        int maxPage = model.getTotalRows() / model.getRowsPerPage();
        int page = model.getCurrentPage();
        if (page != maxPage) {
            setCurrentPage(maxPage);
        }
    }

    protected void refresh() {
        PageableTableModel model = getModel();

        int total = model.getTotalPages() + 1;
        _pageCount.setText(Messages.get("label.navigation.page.total", total));

        String[] pages = new String[model.getTotalPages() + 1];
        for (int i = 0; i < pages.length; ++i) {
            pages[i] = "" + (i + 1);
        }
        int selected = model.getCurrentPage();
        _pageSelector.setModel(new DefaultListModel(pages));
        _pageSelector.setSelectedIndex(selected);
    }

    protected PageableTableModel getModel() {
        return (PageableTableModel) _table.getModel();
    }

}
