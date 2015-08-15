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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for collections of {@link SequencedRelationship}s with cardinality > 1, or that have multiple archetypes.
 * <p/>
 * If the relationships have a <em>sequence</em> node, the collection will be ordered on it, and controls displayed to
 * move relationships up or down within the collection.
 *
 * @author Tim Anderson
 */
public abstract class MultipleSequencedRelationshipCollectionEditor extends RelationshipCollectionEditor {

    /**
     * Determines if the collection has a sequence node.
     * If so, the collection is automatically ordered on the sequence.
     */
    private boolean sequenced;

    /**
     * The relationships being displayed, used when the collection is sequenced.
     */
    private List<RelationshipState> relationships;

    /**
     * The table, if the collection is sequenced.
     */
    private SequencedTable<RelationshipState> table;


    /**
     * Constructs an {@link MultipleSequencedRelationshipCollectionEditor}.
     *
     * @param editor  the collection property editor
     * @param object  the parent object
     * @param context the layout context
     */
    public MultipleSequencedRelationshipCollectionEditor(RelationshipCollectionPropertyEditor editor, IMObject object,
                                                         LayoutContext context) {
        super(editor, object, context);
        sequenced = SequencedRelationshipCollectionHelper.hasSequenceNode(editor.getArchetypeRange());
    }

    /**
     * Creates a new object, subject to a short name being selected, and current collection cardinality. This must be
     * registered with the collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    @Override
    public IMObject create() {
        SequencedRelationship relationship = (SequencedRelationship) super.create();
        if (sequenced && relationships != null) {
            if (!relationships.isEmpty()) {
                RelationshipState state = relationships.get(relationships.size() - 1);
                SequencedRelationship last = (SequencedRelationship) state.getRelationship();
                int sequence = last.getSequence();
                relationship.setSequence(++sequence);
            }
        }
        return relationship;
    }

    /**
     * Creates a new result set for display.
     * <p/>
     * If the relationships have a <em>sequence</em> node, they will be ordered on this.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<RelationshipState> createResultSet() {
        ResultSet<RelationshipState> result;
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<RelationshipState> relationships = new ArrayList<>(editor.getRelationships());
        if (sequenced) {
            SequencedRelationshipCollectionHelper.sortStates(relationships);
            SequencedRelationshipCollectionHelper.sequenceStates(relationships);
            result = new ListResultSet<>(relationships, ROWS);
            this.relationships = relationships;
        } else {
            result = super.createResultSet();
        }
        return result;
    }

    /**
     * Lays out the component in the specified container.
     *
     * @param container the container
     * @param context   the layout context
     */
    @Override
    protected void doLayout(Component container, LayoutContext context) {
        if (sequenced) {
            doSequenceLayout(container);
        } else {
            super.doLayout(container, context);
        }
    }

    /**
     * Lays out the component in the specified container.
     *
     * @param container the container
     */
    protected void doSequenceLayout(Component container) {
        table = new SequencedTable<RelationshipState>(getTable()) {
            @Override
            public List<RelationshipState> getObjects() {
                return relationships;
            }

            /**
             * Swaps two objects.
             *
             * @param object1 the first object
             * @param object2 the second object
             */
            @Override
            protected void swap(RelationshipState object1, RelationshipState object2) {
                IMObjectEditor editor1 = getEditor(object1.getRelationship());
                IMObjectEditor editor2 = getEditor(object2.getRelationship());
                Property property1 = editor1.getProperty("sequence");
                Property property2 = editor2.getProperty("sequence");
                int value1 = property1.getInt();
                int value2 = property2.getInt();
                property1.setValue(value2);
                property2.setValue(value1);

                populateTable();

                getTable().getTable().setSelected(object1);
                enableNavigation(true);
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
     * Enable/disables the buttons.
     * <p/>
     * This delegates to the superclass, before enabling/disabling the move up/move down buttons.
     *
     * @param enable if {@code true} enable buttons (subject to criteria), otherwise disable them
     */
    @Override
    protected void enableNavigation(boolean enable) {
        super.enableNavigation(enable);
        if (table != null) {
            table.enableNavigation(enable);
        }
    }

}
