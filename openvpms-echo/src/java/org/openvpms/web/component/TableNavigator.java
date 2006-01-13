/* 
 * This file is part of the Echo2 Table Extension (hereinafter "ETE").
 * Copyright (C) 2002-2005 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package org.openvpms.web.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import echopointng.table.PageableTableModel;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;

import org.openvpms.web.util.Messages;


/**
 * A controller for tables containing <code>PageableTableModel</code> backed
 * tables.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
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
        _table.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals(Table.MODEL_CHANGED_PROPERTY))
                {
                    refresh();
                }
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
