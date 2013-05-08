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
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.DefaultListSelectionModel;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * A controller for tables containing <tt>PageableTableModel</tt> backed tables.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableNavigator extends Row {

    /**
     * The underlying table.
     */
    private Table table;

    /**
     * The 'first' button.
     */
    private Button first;

    /**
     * The 'previous' button.
     */
    private Button previous;

    /**
     * The 'next' button.
     */
    private Button next;

    /**
     * The 'last' button.
     */
    private Button last;

    /**
     * The page selector combobox.
     */
    private SelectField pageSelector;

    /**
     * The page count.
     */
    private Label pageCount;


    /**
     * Construct a new <tt>TableNavigator</tt>.
     *
     * @param table the table to navigate
     */
    public TableNavigator(Table table) {
        this.table = table;
        this.table.getModel().addTableModelListener(new TableModelListener() {
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
        first.setFocusTraversalIndex(newValue);
        previous.setFocusTraversalIndex(newValue);
        pageSelector.setFocusTraversalIndex(newValue);
        next.setFocusTraversalIndex(newValue);
        last.setFocusTraversalIndex(newValue);
    }

    /**
     * Sets whether the component participates in the focus traversal order
     * (tab order).
     *
     * @param newValue true if the component participates in the focus
     *                 traversal order
     */
    @Override
    public void setFocusTraversalParticipant(boolean newValue) {
        first.setFocusTraversalParticipant(newValue);
        previous.setFocusTraversalParticipant(newValue);
        pageSelector.setFocusTraversalParticipant(newValue);
        next.setFocusTraversalParticipant(newValue);
        last.setFocusTraversalParticipant(newValue);
    }

    /**
     * Displays the first page.
     *
     * @return <tt>true</tt> if the page was changed
     */
    public boolean first() {
        boolean result = false;
        int page = getModel().getPage();
        if (page != 0) {
            result = changePage(0);
        }
        return result;
    }

    /**
     * Displays the previous page.
     *
     * @return <tt>true</tt> if the page was changed
     */
    public boolean previous() {
        boolean result = false;
        PageableTableModel model = getModel();
        int page = model.getPage();
        if (page > 0) {
            result = changePage(page - 1);
        }
        return result;
    }

    /**
     * Displays the next page.
     *
     * @return <tt>true</tt> if the page was changed
     */
    public boolean next() {
        PageableTableModel model = getModel();
        int page = model.getPage();
        return changePage(page + 1);
    }

    /**
     * Displays the last page.
     *
     * @return <tt>true</tt> if the page was changed
     */
    public boolean last() {
        boolean result = false;
        PageableTableModel model = getModel();
        int lastPage = getLastPage();
        int page = model.getPage();
        if (page != lastPage) {
            result = changePage(lastPage);
        }
        return result;
    }

    /**
     * Determines if there is a row prior to the selected row.
     *
     * @return <tt>true</tt> if there is a row prior to the selected row or <tt>false</tt> if no row is selected, or
     *         the first row is selected
     */
    public boolean hasPreviousRow() {
        boolean result = false;
        int index = getSelected();
        if (index != -1) {
            if (index > 0) {
                result = true;
            } else {
                int page = getModel().getPage();
                if (page > 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Navigates to the row before the selected row, and selects it.
     * <p/>
     * If no row is seleced, or there is no previous row, nothing is done.
     *
     * @return <tt>true</tt> if the previous row was selected
     */
    public boolean selectPreviousRow() {
        boolean result = false;
        int index = getSelected();
        if (index != -1) {
            if (index > 0) {
                select(index - 1);
                result = true;
            } else if (previous()) {
                int count = getModel().getRowCount();
                if (count > 0) {
                    select(count - 1);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Determines if there is a row after to the selected row.
     *
     * @return <tt>true</tt> if there is a row after the selected row or <tt>false</tt> if no row is selected, or
     *         there are no more rows
     */
    public boolean hasNextRow() {
        boolean result = false;
        int index = getSelected();
        if (index != -1) {
            PageableTableModel model = getModel();
            int count = model.getRowCount();
            if (index + 1 < count) {
                result = true;
            } else {
                int page = getModel().getPage();
                if (model.hasPage(page + 1)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Navigates to the row after the selected row, and selects it.
     * <p/>
     * If no row is seleced, or there is no next row, nothing is done.
     *
     * @return <tt>true</tt> if the next row was selected
     */
    public boolean selectNextRow() {
        boolean result = false;
        int index = getSelected();
        if (index != -1) {
            PageableTableModel model = getModel();
            int count = model.getRowCount();
            if (index + 1 < count) {
                select(index + 1);
                result = true;
            } else if (next()) {
                if (model.getRowCount() > 0) {
                    select(0);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        setCellSpacing(new Extent(10));

        Label page = LabelFactory.create("navigation.page");

        first = ButtonFactory.create(
            null, "navigation.first", new ActionListener() {
            public void onAction(ActionEvent event) {
                first();
            }
        });
        previous = ButtonFactory.create(
            null, "navigation.previous", new ActionListener() {
            public void onAction(ActionEvent event) {
                previous();
            }
        });

        pageSelector = new SelectField();
        pageSelector.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                int selected = pageSelector.getSelectedIndex();
                PageableTableModel model = getModel();
                model.setPage(selected);
            }
        });

        pageCount = LabelFactory.create();

        next = ButtonFactory.create(
            null, "navigation.next", new ActionListener() {
            public void onAction(ActionEvent event) {
                next();
            }
        });

        last = ButtonFactory.create(
            null, "navigation.last", new ActionListener() {
            public void onAction(ActionEvent event) {
                last();
            }
        });

        add(page);
        add(first);
        add(previous);
        add(pageSelector);
        add(next);
        add(last);
        add(pageCount);

        refresh();
    }

    /**
     * Attempts to change to the specified page.
     *
     * @param page the new page
     * @return <tt>true</tt> if the page was changed
     */
    protected boolean changePage(int page) {
        boolean result = false;
        PageableTableModel model = getModel();
        if (model.setPage(page)) {
            table.setSelectionModel(new DefaultListSelectionModel());
            pageSelector.setSelectedIndex(page);
            result = true;
        } else {
            // failed to set the current page
            if (model.getPage() == (page - 1)) {
                setTotal(model.getPage() + 1);
            }
        }
        return result;
    }

    /**
     * Refreshes the page selector and page count, if necessary.
     */
    protected void refresh() {
        PageableTableModel model = getModel();
        int pages = getPages();
        if (pages != pageSelector.getModel().size()) {
            setTotal(pages);

            String[] pageNos = new String[pages];
            for (int i = 0; i < pageNos.length; ++i) {
                pageNos[i] = "" + (i + 1);
            }
            pageSelector.setModel(new DefaultListModel(pageNos));
        }
        int selected = model.getPage();
        pageSelector.setSelectedIndex(selected);
    }

    /**
     * Returns the table model.
     *
     * @return the table model
     */
    protected PageableTableModel getModel() {
        return (PageableTableModel) table.getModel();
    }

    /**
     * Sets the page total label.
     *
     * @param pages the total no. of pages
     */
    private void setTotal(int pages) {
        String total;
        if (getModel().isEstimatedActual()) {
            total = Messages.get("navigation.page.total", pages);
        } else {
            total = Messages.get("navigation.page.totalunknown", pages);
        }
        pageCount.setText(total);
    }

    /**
     * Returns the estimated no. of pages.
     *
     * @return an estimate of the no. of pages
     */
    private int getPages() {
        PageableTableModel model = getModel();
        return model.getEstimatedPages();
    }

    /**
     * Returns the index of the last page. Note that this operation may
     * be expensive.
     *
     * @return the index of the last page
     */
    private int getLastPage() {
        int pages = getModel().getPages();
        return (pages > 0) ? pages - 1 : 0;
    }

    /**
     * Returns the selected row.
     *
     * @return the seleted row, or <tt>-1</tt> if no row is selected
     */
    private int getSelected() {
        return table.getSelectionModel().getMinSelectedIndex();
    }


    /**
     * Selects the row at the given index.
     *
     * @param index the index to select
     */
    private void select(int index) {
        table.getSelectionModel().setSelectedIndex(index, true);
    }

}
