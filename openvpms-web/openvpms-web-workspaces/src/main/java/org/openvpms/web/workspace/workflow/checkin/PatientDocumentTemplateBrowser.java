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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An {@link IMObjectTableBrowser} that enables multiple document templates to be selected.
 *
 * @author Tim Anderson
 */
class PatientDocumentTemplateBrowser extends IMObjectTableBrowser<Entity> {

    /**
     * The set of selected templates.
     */
    private Set<IMObjectReference> selections;

    /**
     * Constructs a {@link PatientDocumentTemplateBrowser}.
     *
     * @param query   the document template query
     * @param context the context
     */
    public PatientDocumentTemplateBrowser(Query<Entity> query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Returns the list of selected templates.
     *
     * @return the selected templates
     */
    public List<Entity> getSelectedList() {
        List<Entity> result;
        if (selections.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<Entity>();
            for (IMObjectReference reference : selections) {
                Entity template = (Entity) IMObjectHelper.getObject(reference, null);
                if (template != null) {
                    result.add(template);
                }
            }
        }
        return result;
    }

    /**
     * Determines if any templates are selected.
     *
     * @return {@code true} if templates are selected, otherwise {@code false}
     */
    public boolean hasSelections() {
        return !selections.isEmpty();
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<Entity> createTableModel(LayoutContext context) {
        selections = new HashSet<IMObjectReference>();
        return new PrintTableModel(selections);
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    @Override
    protected void notifySelected(Entity selected) {
        ((PrintTableModel) getTableModel()).toggleSelection(selected);
        super.notifySelected(selected);
    }

    protected void notifyToggled(Entity selected) {
        super.notifySelected(selected);
    }

    private class PrintTableModel extends BaseIMObjectTableModel<Entity> {

        /**
         * The print check boxes.
         */
        private List<CheckBox> print = new ArrayList<CheckBox>();

        /**
         * The print column.
         */
        private final int PRINT_INDEX = NEXT_INDEX;

        /**
         * Determines the selections.
         */
        private Set<IMObjectReference> selections;


        /**
         * Constructs a {@link PrintTableModel}.
         *
         * @param selections the selections
         */
        public PrintTableModel(Set<IMObjectReference> selections) {
            super(null);
            setTableColumnModel(createTableColumnModel(false));
            this.selections = selections;
        }

        /**
         * Sets the objects to display.
         *
         * @param objects the objects to display
         */
        @Override
        public void setObjects(List<Entity> objects) {
            super.setObjects(objects);
            print = new ArrayList<CheckBox>();
            for (final Entity object : objects) {
                boolean selected = selections.contains(object.getObjectReference());
                final CheckBox e = CheckBoxFactory.create(selected);
                e.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        setSelected(object, e.isSelected());
                        notifyToggled(object);
                    }
                });
                print.add(e);
            }
        }

        public void toggleSelection(Entity object) {
            int index = getObjects().indexOf(object);
            if (index != -1) {
                CheckBox checkBox = print.get(index);
                boolean selected = checkBox.isSelected();
                checkBox.setSelected(!selected);
                setSelected(object, !selected);
            }
        }

        private void setSelected(Entity object, boolean selected) {
            if (selected) {
                selections.add(object.getObjectReference());
            } else {
                selections.remove(object.getObjectReference());
            }
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Entity object, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == PRINT_INDEX) {
                result = print.get(row);
            } else {
                result = super.getValue(object, column, row);
            }
            return result;
        }

        /**
         * Creates a new column model.
         *
         * @param showId        if {@code true}, show the ID
         * @param showArchetype if {@code true} show the archetype
         * @return a new column model
         */
        protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype) {
            TableColumnModel model = new DefaultTableColumnModel();
            TableColumn column = createTableColumn(PRINT_INDEX, "batchprintdialog.print");
            model.addColumn(column);
            return super.createTableColumnModel(showId, showArchetype, model);
        }

    }
}
