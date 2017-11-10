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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.palette.Palette;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An editor for product.* locations nodes that displays available locations in one column, and the locations that
 * the product is excluded from in another.
 *
 * @author Tim Anderson
 */
public class ProductLocationCollectionEditor extends AbstractIMObjectCollectionEditor {

    /**
     * The focus group.
     */
    private FocusGroup focusGroup;

    /**
     * Constructs a {@link ProductLocationCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public ProductLocationCollectionEditor(CollectionProperty property, Entity object, LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    @Override
    protected Component doLayout(LayoutContext context) {
        List<Party> locations = Collections.emptyList();
        Party practice = context.getContext().getPractice();
        if (practice != null) {
            locations = ServiceHelper.getBean(PracticeRules.class).getLocations(practice);
        }
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        LocationSelector palette = new LocationSelector(locations, editor);
        palette.setCellRenderer(IMObjectListCellRenderer.NAME);
        focusGroup = new FocusGroup(editor.getProperty().getDisplayName());
        focusGroup.add(palette);
        return palette;
    }

    private static class LocationSelector extends Palette<IMObject> {

        private final CollectionPropertyEditor editor;

        /**
         * Constructs a {@link LocationSelector}.
         *
         * @param locations all locations that may be selected
         * @param editor    the collection property editor
         */
        public LocationSelector(List<Party> locations, CollectionPropertyEditor editor) {
            super(new ArrayList<>(locations), getLocations(editor, locations));
            this.editor = editor;
        }

        /**
         * Add items to the 'selected' list.
         *
         * @param values the values to add.
         */
        @Override
        protected void add(Object[] values) {
            String shortName = editor.getArchetypeRange()[0];
            for (Object value : values) {
                EntityLink link = (EntityLink) IMObjectCreator.create(shortName);
                link.setTarget(((IMObject) value).getObjectReference());
                editor.add(link);
            }
        }

        /**
         * Remove items from the 'selected' list.
         *
         * @param values the values to remove
         */
        @Override
        protected void remove(Object[] values) {
            for (Object value : values) {
                IMObject object = (IMObject) value;
                IMObjectReference reference = object.getObjectReference();
                for (IMObject link : new ArrayList<IMObject>(editor.getObjects())) {
                    if (ObjectUtils.equals(((EntityLink) link).getTarget(), reference)) {
                        editor.remove(link);
                    }
                }
            }
        }

        /**
         * Sorts the locations by name.
         *
         * @param values the list to sort
         */
        @Override
        protected void sort(List<IMObject> values) {
            IMObjectSorter.sort(values, "name");
        }

        /**
         * Creates a label for the 'available' column.
         *
         * @return a new label
         */
        @Override
        protected Label createAvailableLabel() {
            return createLabel("product.location.available");
        }

        /**
         * Creates a label for the 'selected' column.
         *
         * @return a new label
         */
        @Override
        protected Label createSelectedLabel() {
            return createLabel("product.location.notavailable");
        }

        /**
         * Returns the selected locations.
         *
         * @param editor the collection
         * @param active the active locations
         * @return the selected locations
         */
        private static List<IMObject> getLocations(CollectionPropertyEditor editor, List<Party> active) {
            List<IMObject> locations = new ArrayList<>();
            for (IMObject object : editor.getObjects()) {
                IMObjectReference target = ((IMObjectRelationship) object).getTarget();
                IMObject location = IMObjectHelper.getObject(target, active);
                if (location == null) {
                    location = IMObjectHelper.getObject(target);
                }
                if (location != null) {
                    locations.add(location);
                }
            }
            return locations;
        }

    }

}
