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

package org.openvpms.web.component.table;

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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableNavigator extends Row {

    /**
     * The underlying table.
     */
    private Table _table;

    /**
     * The 'first' button.
     */
    private Button _first;

    /**
     * The 'previous' button.
     */
    private Button _previous;

    /**
     * The 'next' button.
     */
    private Button _next;

    /**
     * The 'last' button.
     */
    private Button _last;

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

    /**
     * Sets the focus traversal (tab) index of the component.
     *
     * @param newValue the new focus traversal index
     * @see #getFocusTraversalIndex()
     */
    @Override
    public void setFocusTraversalIndex(int newValue) {
        _first.setFocusTraversalIndex(newValue);
        _previous.setFocusTraversalIndex(newValue);
        _pageSelector.setFocusTraversalIndex(newValue);
        _next.setFocusTraversalIndex(newValue);
        _last.setFocusTraversalIndex(newValue);
    }

    protected void doLayout() {
        setCellSpacing(new Extent(10));

        Label page = LabelFactory.create("navigation.page");

        _first = ButtonFactory.create(
                null, "navigation.first", new ActionListener() {
            public void actionPerformed(
                    ActionEvent event) {
                doFirst();
            }
        });
        _previous = ButtonFactory.create(
                null, "navigation.previous", new ActionListener() {
            public void actionPerformed(
                    ActionEvent event) {
                doPrevious();
            }
        });

        _pageSelector = new SelectField();
        _pageSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int selected = _pageSelector.getSelectedIndex();
                PageableTableModel model = getModel();
                model.setPage(selected);
            }
        });

        _pageCount = LabelFactory.create();

        _next = ButtonFactory.create(
                null, "navigation.next", new ActionListener() {
            public void actionPerformed(
                    ActionEvent event) {
                doNext();
            }
        });

        _last = ButtonFactory.create(
                null, "navigation.last", new ActionListener() {
            public void actionPerformed(
                    ActionEvent event) {
                doLast();
            }
        });

        add(page);
        add(_first);
        add(_previous);
        add(_pageSelector);
        add(_next);
        add(_last);
        add(_pageCount);

        refresh();
    }

    protected void doFirst() {
        int page = getModel().getPage();
        if (page != 0) {
            setCurrentPage(0);
        }
    }

    protected void setCurrentPage(int page) {
        getModel().setPage(page);
        _pageSelector.setSelectedIndex(page);
    }

    protected void doPrevious() {
        PageableTableModel model = getModel();
        int page = model.getPage();
        if (page > 0) {
            setCurrentPage(page - 1);
        }
    }

    protected void doNext() {
        PageableTableModel model = getModel();
        int page = model.getPage();
        if (page < getLastPage()) {
            setCurrentPage(page + 1);
        }
    }

    protected void doLast() {
        PageableTableModel model = getModel();
        int lastPage = getLastPage();
        int page = model.getPage();
        if (page != lastPage) {
            setCurrentPage(lastPage);
        }
    }

    protected void refresh() {
        PageableTableModel model = getModel();

        int pages = model.getPages();
        if (pages != _pageSelector.getModel().size()) {
            String total = Messages.get("label.navigation.page.total", pages);
            _pageCount.setText(total);

            String[] pageNos = new String[pages];
            for (int i = 0; i < pageNos.length; ++i) {
                pageNos[i] = "" + (i + 1);
            }
            _pageSelector.setModel(new DefaultListModel(pageNos));
        }
        int selected = model.getPage();
        _pageSelector.setSelectedIndex(selected);
    }

    protected PageableTableModel getModel() {
        return (PageableTableModel) _table.getModel();
    }

    /**
     * Returns the index of the last page.
     *
     * @return the index of the last page
     */
    private int getLastPage() {
        int pages = getModel().getPages();
        return (pages > 0) ? pages - 1 : 0;
    }

}
