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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.PageListener;
import org.openvpms.web.echo.table.SortableTableHeaderRenderer;
import org.openvpms.web.echo.table.TableNavigator;


/**
 * Paged table for domain objects.
 *
 * @author Tim Anderson
 */
public class PagedIMTable<T> {

    /**
     * The underlying table.
     */
    private final IMTable<T> table;

    /**
     * The navigator.
     */
    private TableNavigator navigator;

    /**
     * The container component.
     */
    private final Container container;

    /**
     * The focus group.
     */
    private FocusGroup group = new FocusGroup(PagedIMTable.class.getSimpleName());

    /**
     * Used to hide the navigator.
     */
    private static final Extent ZERO_PX = new Extent(0);

    /**
     * Constructs a {@link PagedIMTable}.
     *
     * @param model the table model
     */
    public PagedIMTable(IMTableModel<T> model) {
        this(model, false);
    }

    /**
     * Constructs a {@link PagedIMTable}.
     *
     * @param model the model to render results
     * @param set   the result set
     */
    public PagedIMTable(IMTableModel<T> model, ResultSet<T> set) {
        this(model);
        setResultSet(set);
    }

    /**
     * Constructs a {@link PagedIMTable}.
     *
     * @param model        the model to render results
     * @param useSplitPane if {@code true}, render the navigator and table in a split pane. This ensures that the
     *                     navigator doesn't scroll with the table
     */
    public PagedIMTable(IMTableModel<T> model, boolean useSplitPane) {
        container = (useSplitPane) ? new SplitPaneContainer() : new DefaultContainer();
        IMTableModel<T> paged;
        if (!(model instanceof PagedIMTableModel)) {
            paged = new PagedIMTableModel<>(model);
        } else {
            paged = model;
        }
        table = new IMTable<>(paged);
        table.setDefaultHeaderRenderer(new SortableTableHeaderRenderer());
        table.setRolloverEnabled(false);
        table.addPageListener(new PageListener() {
            public void onAction(ActionEvent event) {
                doPage(event);
            }
        });
        group.add(table);
        this.container.setTable(table);
    }

    /**
     * Sets the result set.
     *
     * @param set the set
     */
    public void setResultSet(ResultSet<T> set) {
        PagedIMTableModel<T, T> model = getModel();
        model.setResultSet(set);
        int pages = set.getEstimatedPages();
        boolean actual = set.isEstimatedActual();

        // only display the table navigator if:
        // . the no. of pages != 0 and is an estimation
        // . the no. of pages are known and > 1
        if (navigator == null) {
            navigator = new TableNavigator(table);
            container.setNavigator(navigator);
            group.add(0, navigator);
        }
        if ((!actual && pages > 0) || pages > 1) {
            container.showNavigator(true);
        } else {
            container.showNavigator(false);
        }
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if no object is selected
     */
    public T getSelected() {
        return table.getSelected();
    }

    /**
     * Sets the selected object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        table.setSelected(object);
    }

    /**
     * Returns the result set.
     *
     * @return the result set, or {@code null} if none has been set
     */
    public ResultSet<T> getResultSet() {
        return getModel().getResultSet();
    }

    /**
     * Returns the underlying table.
     *
     * @return the underlying table
     */
    public IMTable<T> getTable() {
        return table;
    }

    /**
     * Returns the underlying table model.
     *
     * @return the underlying model
     */
    @SuppressWarnings("unchecked")
    public PagedIMTableModel<T, T> getModel() {
        return (PagedIMTableModel<T, T>) table.getModel();
    }

    /**
     * Returns the table navigator.
     *
     * @return the table navigator, or {@code null} if there is no result set
     */
    public TableNavigator getNavigator() {
        return navigator;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return container.getComponent();
    }

    /**
     * Invoked when the table navigator changes pages.
     *
     * @param event the page event
     */
    private void doPage(ActionEvent event) {
        if (navigator != null) {
            String key = event.getActionCommand();
            if (PageListener.PAGE_PREVIOUS.equals(key)) {
                navigator.previous();
            } else if (PageListener.PAGE_NEXT.equals(key) || PageListener.PAGE_NEXT_TOP.equals(key)) {
                navigator.next();
            } else if (PageListener.PAGE_FIRST.equals(key)) {
                navigator.first();
            } else if (PageListener.PAGE_LAST.equals(key)) {
                navigator.last();
            } else if (PageListener.PAGE_PREVIOUS_BOTTOM.equals(key)) {
                if (navigator.previous()) {
                    int rows = table.getModel().getRowCount();
                    if (rows > 0) {
                        table.getSelectionModel().setSelectedIndex(rows - 1, true);
                    }
                }
            }

            // refocus on the table
            FocusHelper.setFocus(table);
        }
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }

    private interface Container {

        void setTable(IMTable table);

        void setNavigator(TableNavigator navigator);

        void showNavigator(boolean show);

        Component getComponent();
    }

    private class DefaultContainer implements Container {

        private final Component container;

        public DefaultContainer() {
            this.container = ColumnFactory.create(Styles.CELL_SPACING);
        }

        @Override
        public void setTable(IMTable table) {
            container.add(table);
        }

        @Override
        public void setNavigator(TableNavigator navigator) {
            container.add(navigator, 0);
        }

        @Override
        public void showNavigator(boolean show) {
            if (navigator != null) {
                navigator.setVisible(show);
            }
        }

        @Override
        public Component getComponent() {
            return container;
        }
    }

    private class SplitPaneContainer implements Container {

        private final SplitPane container;
        private Extent position;

        public SplitPaneContainer() {
            this.container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "PagedIMTable");
            position = container.getSeparatorPosition();
            if (position == null && container.getStyleName() != null) {
                position = StyleSheetHelper.getExtent(SplitPane.class, container.getStyleName(),
                                                      SplitPane.PROPERTY_SEPARATOR_POSITION);
            }
            container.add(LabelFactory.create()); // spacer until the navigator is displayed
        }

        @Override
        public void setTable(IMTable table) {
            container.add(ColumnFactory.create(Styles.INSET, table));
        }

        @Override
        public void setNavigator(TableNavigator navigator) {
            if (container.getComponentCount() == 2) {
                // remove spacer
                container.remove(0);
            }
            container.add(ColumnFactory.create(Styles.INSET, navigator), 0);
        }

        @Override
        public void showNavigator(boolean show) {
            container.setSeparatorPosition(show ? position : ZERO_PX);
        }

        @Override
        public Component getComponent() {
            return container;
        }
    }

}
