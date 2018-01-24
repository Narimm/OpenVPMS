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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DelegatingIMTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.table.TableHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Table browser for {@link IMObject}s that supports multiple selection by adding a check box column.
 *
 * @author Tim Anderson
 */
public class MultiSelectTableBrowser<T extends IMObject> extends IMObjectTableBrowser<T> {

    /**
     * The set of selected objects.
     */
    private final SelectionTracker<T> tracker;

    /**
     * Constructs a {@link MultiSelectTableBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public MultiSelectTableBrowser(Query<T> query, LayoutContext context) {
        this(query, new IMObjectSelections<>(), context);
    }

    /**
     * Constructs a {@link MultiSelectTableBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param tracker the selection tracker, used to determine initial selections
     * @param context the layout context
     */
    public MultiSelectTableBrowser(Query<T> query, SelectionTracker<T> tracker, LayoutContext context) {
        super(query, context);
        this.tracker = tracker;
    }

    /**
     * Returns the selections.
     *
     * @return the selections
     */
    public Collection<T> getSelections() {
        return tracker.getSelected();
    }

    /**
     * Returns the selection tracker.
     *
     * @return the selection tracker
     */
    protected SelectionTracker<T> getSelectionTracker() {
        return tracker;
    }

    /**
     * Creates a new table model that wraps the table model returned by {@link #createChildTableModel(LayoutContext)},
     * adding a selection column.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected MultiSelectTableModel createTableModel(LayoutContext context) {
        IMTableModel<T> model = createChildTableModel(context);
        return new MultiSelectTableModel(model);
    }

    /**
     * Creates a new table model the displays the content of the objects.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<T> createChildTableModel(LayoutContext context) {
        return super.createTableModel(context);
    }

    /**
     * Notifies listeners when an object is selected.
     * <p>
     * This implementation toggles the checkbox.
     *
     * @param selected the selected object
     */
    @Override
    protected void notifySelected(T selected) {
        ((MultiSelectTableModel) getTableModel()).toggleSelection(selected);
        super.notifySelected(selected);
    }

    /**
     * Invoked when a check box is toggled.
     * <p>
     * This notifies listeners.
     *
     * @param selected the selected object
     */
    protected void notifyToggled(T selected) {
        super.notifySelected(selected);
    }

    public interface SelectionTracker<T> {

        /**
         * Determines if an object is selected.
         *
         * @param object the object
         * @return {@code true} if the object is selected
         */
        boolean isSelected(T object);

        /**
         * Marks an object as selected.
         *
         * @param object   the object
         * @param selected if {@code true}, select the object, otherwise deselect it
         */
        void setSelected(T object, boolean selected);

        /**
         * Returns the selected objects.
         *
         * @return the selected objects
         */
        Collection<T> getSelected();

        /**
         * Determines if an object can be selected.
         *
         * @param object the object
         * @return {@code true} if the object can be selected
         */
        boolean canSelect(T object);
    }

    protected class MultiSelectTableModel extends DelegatingIMTableModel<T, T> {

        /**
         * The selection column.
         */
        private final TableColumn selectionColumn;

        /**
         * The check boxes to track selection.
         */
        private List<CheckBox> selections = new ArrayList<>();

        /**
         * Constructs a {@link MultiSelectTableModel}.
         *
         * @param model the model
         */
        public MultiSelectTableModel(IMTableModel<T> model) {
            super(model);
            TableColumnModel columns = model.getColumnModel();
            selectionColumn = new TableColumn(TableHelper.getNextModelIndex(columns));
            columns.addColumn(selectionColumn);
            columns.moveColumn(TableHelper.getColumnOffset(columns, selectionColumn.getModelIndex()), 0);
        }

        /**
         * Returns the column that tracks selections.
         *
         * @return the selection column
         */
        public TableColumn getSelectionColumn() {
            return selectionColumn;
        }

        /**
         * Sets the objects to display.
         *
         * @param objects the objects to display
         */
        @Override
        public void setObjects(List<T> objects) {
            super.setObjects(objects);
            selections = new ArrayList<>();
            for (T object : objects) {
                CheckBox box;
                if (tracker.canSelect(object)) {
                    box = CheckBoxFactory.create(tracker.isSelected(object));
                    box.addActionListener(new ActionListener() {
                        @Override
                        public void onAction(ActionEvent event) {
                            tracker.setSelected(object, box.isSelected());
                            notifyToggled(object);
                        }
                    });
                } else {
                    box = CheckBoxFactory.create();
                    box.setEnabled(false);
                }
                selections.add(box);
            }
        }

        /**
         * Returns the selected objects.
         *
         * @return the selected objects
         */
        public List<T> getSelected() {
            List<T> result = new ArrayList<>();
            for (int i = 0; i < selections.size(); ++i) {
                CheckBox check = selections.get(i);
                if (check.isSelected()) {
                    result.add(getObjects().get(i));
                }
            }
            return result;
        }


        @Override
        public Object getValueAt(int column, int row) {
            return (column == selectionColumn.getModelIndex()) ? selections.get(row) : super.getValueAt(column, row);
        }

        public void toggleSelection(T object) {
            int index = getObjects().indexOf(object);
            if (index != -1) {
                CheckBox checkBox = selections.get(index);
                boolean isSelected = checkBox.isSelected();
                checkBox.setSelected(!isSelected);
                tracker.setSelected(object, !isSelected);
            }
        }
    }
}
