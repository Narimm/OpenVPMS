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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.collections4.ComparatorUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.list.BoldListCell;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.BoundLookupField;
import org.openvpms.web.component.im.lookup.DefaultLookupPropertyEditor;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An editor for the patient 'breed' property that adds a 'New Breed' to allow a custom breed to be specified.
 *
 * @author Tim Anderson
 */
public class BreedEditor extends DefaultLookupPropertyEditor {

    /**
     * The list renderer.
     */
    private static final LookupListCellRenderer RENDERER = new BreedLookupListCellRenderer();

    /**
     * Constructs a {@link BreedEditor}.
     *
     * @param property the property being edited
     * @param parent   the parent object
     */
    public BreedEditor(Property property, IMObject parent) {
        super(property, create(property, parent));
    }

    /**
     * Determines if 'New Breed' is selected.
     *
     * @return {@code true} if 'New Breed' is selected, otherwise {@code false}
     */
    public boolean isNewBreed() {
        LookupField field = getComponent();
        BreedLookupListModel model = (BreedLookupListModel) field.getModel();
        return model.isNewBreed(field.getSelectedIndex());
    }

    /**
     * Selects the 'New Breed' cell.
     */
    public void selectNewBreed() {
        LookupField field = getComponent();
        BreedLookupListModel model = (BreedLookupListModel) field.getModel();
        field.setSelectedIndex(model.getNewBreedIndex());
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public LookupField getComponent() {
        return (LookupField) super.getComponent();
    }

    /**
     * Creates a lookup field for the breed.
     *
     * @param breed  the breed property
     * @param parent the parent object
     * @return a new field
     */
    private static LookupField create(Property breed, IMObject parent) {
        NodeLookupQuery query = new NodeLookupQuery(parent, breed);
        BreedLookupListModel model = new BreedLookupListModel(query, !breed.isRequired());
        BoundLookupField field = new BoundLookupField(breed, model);
        LookupFieldFactory.setDefaultStyle(field);
        field.setCellRenderer(RENDERER);
        return field;
    }

    private static class BreedLookupListModel extends LookupListModel {

        private static final Comparator<String> COMPARATOR
                = ComparatorUtils.nullLowComparator(ComparatorUtils.<String>naturalComparator());

        /**
         * The index of 'New Breed' in the breed list.
         */
        private int newBreedIndex;

        /**
         * Constructs a {@link BreedLookupListModel}.
         *
         * @param source the lookup source
         * @param none   if {@code true}, add a localised "None"
         */
        public BreedLookupListModel(LookupQuery source, boolean none) {
            super(source, false, none);
        }

        /**
         * Determines if the specified index indicates 'New Breed'.
         *
         * @param index the index
         * @return {@code true} if the index indicates 'New Breed'
         */
        public boolean isNewBreed(int index) {
            return index == newBreedIndex;
        }

        /**
         * Returns the 'New Breed' breed index.
         *
         * @return the index
         */
        public int getNewBreedIndex() {
            return newBreedIndex;
        }

        /**
         * Sets the objects.
         *
         * @param objects the objects to populate the list with.
         * @param all     if {@code true}, add a localised "All"
         * @param none    if {@code true}, add a localised "None"
         */
        @Override
        protected void initObjects(List<? extends Lookup> objects, boolean all, boolean none) {
            super.initObjects(objects, all, none);
            Lookup newBreed = new Lookup(null, null, BreedLookupListCellRenderer.NEW_BREED);
            List<Lookup> lookups = getObjects();
            newBreedIndex = Collections.binarySearch(lookups, newBreed, new Comparator<Lookup>() {
                @Override
                public int compare(Lookup o1, Lookup o2) {
                    String name1 = o1 != null ? o1.getName() : null;
                    String name2 = o2 != null ? o2.getName() : null;
                    return COMPARATOR.compare(name1, name2);
                }
            });
            if (newBreedIndex < 0) {
                newBreedIndex = -newBreedIndex - 1;
                lookups.add(newBreedIndex, null);
            }
        }
    }

    private static class BreedLookupListCellRenderer extends LookupListCellRenderer {

        public static final String NEW_BREED = Messages.get("patient.newbreed");

        /**
         * Renders an item in a list.
         *
         * @param list  the list component
         * @param value the item value. May be <tt>null</tt>
         * @param index the item index
         * @return the rendered form of the list cell
         */
        @Override
        public Object getListCellRendererComponent(Component list, Object value, int index) {
            AbstractListComponent component = ((AbstractListComponent) list);
            ListModel model = component.getModel();
            if (model instanceof BreedLookupListModel && ((BreedLookupListModel) model).isNewBreed(index)) {
                return new BoldListCell(NEW_BREED);
            }
            return super.getListCellRendererComponent(list, value, index);
        }
    }
}
