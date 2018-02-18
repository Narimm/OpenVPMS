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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.component.im.relationship.SequencedRelationshipCollectionHelper.sort;

/**
 * Editor for collections of {@link SequencedRelationship}s with 0..N cardinality.
 * This displays the targets of the relationships in the table.
 *
 * @author Tim Anderson
 */
public class MultipleSequencedRelationshipCollectionTargetEditor extends MultipleRelationshipCollectionTargetEditor {

    /**
     * The table.
     */
    private SequencedTable<IMObject> table;

    /**
     * Constructs a {@link MultipleSequencedRelationshipCollectionTargetEditor}.
     *
     * @param editor  the property editor
     * @param object  the parent object
     * @param context the layout context
     */
    public MultipleSequencedRelationshipCollectionTargetEditor(RelationshipCollectionTargetPropertyEditor editor,
                                                               IMObject object, LayoutContext context) {
        super(editor, object, context, SequencedCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Lays out the component in the specified container.
     *
     * @param container the container
     * @param context   the layout context
     */
    @Override
    protected void doLayout(Component container, LayoutContext context) {
        table = new SequencedTable<IMObject>(getTable()) {

            @Override
            public List<IMObject> getObjects() {
                return MultipleSequencedRelationshipCollectionTargetEditor.super.getObjects();
            }

            @Override
            protected void swap(IMObject object1, IMObject object2) {
                RelationshipCollectionTargetPropertyEditor editor = getCollectionPropertyEditor();
                SequencedRelationship r1 = (SequencedRelationship) editor.getRelationship(object1);
                SequencedRelationship r2 = (SequencedRelationship) editor.getRelationship(object2);
                if (r1 != null && r2 != null) {
                    int value1 = r1.getSequence();
                    int value2 = r2.getSequence();
                    r1.setSequence(value2);
                    r2.setSequence(value1);

                    swapped(object1, object2);

                    populateTable();
                    getTable().getTable().setSelected(object1);
                    enableNavigation(true);
                }
            }
        };
        FocusGroup focusGroup = getFocusGroup();

        if (!isCardinalityReadOnly()) {
            Row row = createControls(focusGroup);
            container.add(row);
        }

        table.layout(container, focusGroup);
        populateTable();
        enableNavigation(true);
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    @Override
    protected RelationshipCollectionTargetPropertyEditor getCollectionPropertyEditor() {
        return (RelationshipCollectionTargetPropertyEditor) super.getCollectionPropertyEditor();
    }

    /**
     * Invoked when two objects are swapped.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object1 the first object
     * @param object2 the second object
     */
    protected void swapped(IMObject object1, IMObject object2) {

    }

    /**
     * Enable/disables the buttons.
     * <p/>
     * Note that the delete button is enabled if {@link #getCurrentEditor()} or {@link #getSelected()} return non-null.
     *
     * @param enable if {@code true} enable buttons (subject to criteria), otherwise disable them
     */
    @Override
    protected void enableNavigation(boolean enable) {
        super.enableNavigation(enable);
        table.enableNavigation(enable);
    }

    /**
     * Default implementation of the {@link CollectionResultSetFactory} interface.
     *
     * @author Tim Anderson
     */
    private static class SequencedCollectionResultSetFactory extends AbstractCollectionResultSetFactory
            implements CollectionResultSetFactory {

        /**
         * The singleton instance.
         */
        public static final CollectionResultSetFactory INSTANCE = new SequencedCollectionResultSetFactory();

        /**
         * Creates a new result set.
         *
         * @param property the collection property
         * @param context  the context
         * @return a new result set
         */
        @Override
        @SuppressWarnings("unchecked")
        public ResultSet<IMObject> createResultSet(CollectionPropertyEditor property, Context context) {
            RelationshipCollectionTargetPropertyEditor editor = (RelationshipCollectionTargetPropertyEditor) property;
            List<Map.Entry<IMObject, IMObjectRelationship>> entries = sort(editor.getTargets());
            List<IMObject> sorted = new ArrayList<>();
            for (Map.Entry<IMObject, IMObjectRelationship> entry : entries) {
                sorted.add(entry.getKey());
            }
            return new IMObjectListResultSet<>(sorted, DEFAULT_ROWS);
        }

        /**
         * Default constructor.
         */
        private SequencedCollectionResultSetFactory() {

        }

    }

}
