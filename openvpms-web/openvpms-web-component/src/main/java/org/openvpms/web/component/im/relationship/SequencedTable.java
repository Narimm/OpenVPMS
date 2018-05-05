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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.button.ButtonColumn;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * A table that provides buttons to move rows up and down the table.
 *
 * @author Tim Anderson
 */
public abstract class SequencedTable<T> {

    /**
     * The move up/down buttons.
     */
    private ButtonColumn moveButtons;

    /**
     * The underlying table.
     */
    private final PagedIMTable<T> table;

    /**
     * 'Move up' button identifier.
     */
    private static final String MOVEUP_ID = "moveup";

    /**
     * 'Move down' button identifier.
     */
    private static final String MOVEDOWN_ID = "movedown";

    /**
     * Constructs a {@link SequencedTable}.
     *
     * @param table the underlying table
     */
    public SequencedTable(PagedIMTable<T> table) {
        this.table = table;
    }

    /**
     * Returns the underlying objects.
     *
     * @return the underlying objects
     */
    public abstract List<T> getObjects();

    /**
     * Lays out the component with controls to change the sequence of relationships.
     *
     * @param container  the container
     * @param focusGroup the focus group
     */
    public void layout(Component container, FocusGroup focusGroup) {
        focusGroup.add(table.getFocusGroup());
        moveButtons = new ButtonColumn(focusGroup);
        moveButtons.addButton(MOVEUP_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMoveUp();
            }
        });
        moveButtons.addButton(MOVEDOWN_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMoveDown();
            }
        });

        table.getTable().setWidth(Styles.FULL_WIDTH);
        Row row = RowFactory.create(moveButtons);

        GridLayoutData alignTop = new GridLayoutData();
        alignTop.setAlignment(Alignment.ALIGN_TOP);
        row.setLayoutData(alignTop);
        table.getComponent().setLayoutData(alignTop);
        Grid grid = GridFactory.create(2, table.getComponent(), row);
        grid.setWidth(Styles.FULL_WIDTH);

        container.add(grid);
    }

    /**
     * Enable/disables the buttons.
     *
     * @param enable if {@code true} enable buttons (subject to criteria), otherwise disable them
     */
    public void enableNavigation(boolean enable) {
        if (moveButtons != null) {
            T object = table.getSelected();
            boolean moveUp = false;
            boolean moveDown = false;
            if (enable && object != null) {
                List<T> objects = getObjects();
                int index = objects.indexOf(object);
                if (index > 0) {
                    moveUp = true;
                }
                if (index < objects.size() - 1) {
                    moveDown = true;
                }
            }
            moveButtons.getButtons().setEnabled(MOVEUP_ID, moveUp);
            moveButtons.getButtons().setEnabled(MOVEDOWN_ID, moveDown);
        }
    }

    /**
     * Swaps two objects.
     *
     * @param object1 the first object
     * @param object2 the second object
     */
    protected abstract void swap(T object1, T object2);

    /**
     * Moves the selected relationship up in the table.
     */
    private void onMoveUp() {
        T selected = table.getTable().getSelected();
        if (selected != null) {
            int index = getObjects().indexOf(selected);
            if (index > 0) {
                swap(index, index - 1);
            }
        }
    }

    /**
     * Moves the selected relationship down in the table.
     */
    private void onMoveDown() {
        T selected = table.getTable().getSelected();
        if (selected != null) {
            List<T> objects = getObjects();
            int index = objects.indexOf(selected);
            if (index < objects.size() - 1) {
                swap(index, index + 1);
            }
        }
    }

    /**
     * Swaps two objects in the table.
     *
     * @param index1 the index of the first objects
     * @param index2 the index of the second objects
     */
    private void swap(int index1, int index2) {
        List<T> objects = getObjects();
        T r1 = objects.get(index1);
        T r2 = objects.get(index2);
        if (r1 != null && r2 != null) {
            swap(r1, r2);
        }
    }

}
