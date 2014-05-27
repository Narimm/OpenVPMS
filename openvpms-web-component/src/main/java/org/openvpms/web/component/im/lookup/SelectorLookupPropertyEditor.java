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

package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.edit.AbstractSelectorPropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collection;

/**
 * An editor for lookup properties that provides an {@link IMObjectSelector} to display and query lookups.
 * <p/>
 * Note that this editor is only applicable to nodes where a lookup assertion with a "source" is used.
 *
 * @author Tim Anderson
 */
public class SelectorLookupPropertyEditor extends AbstractSelectorPropertyEditor<Lookup>
        implements LookupPropertyEditor {

    /**
     * The short name to query.
     */
    private final String shortName;

    /**
     * The parent object.
     */
    private final IMObject parent;


    /**
     * Constructs a {@link SelectorLookupPropertyEditor}.
     *
     * @param shortName the archetype short name to query
     * @param property  the property being edited
     * @param parent    the parent object
     * @param context   the layout context
     */
    public SelectorLookupPropertyEditor(String shortName, Property property, IMObject parent, LayoutContext context) {
        super(property, context);
        this.shortName = shortName;
        this.parent = parent;
        updateSelector();
    }

    /**
     * Returns the object corresponding to the property.
     *
     * @return the object. May be {@code null}
     */
    @Override
    protected Lookup getObject() {
        Property property = getProperty();
        if (property.isCollection()) {
            Collection values = ((CollectionProperty) property).getValues();
            return (!values.isEmpty()) ? (Lookup) values.iterator().next() : null;
        }
        return ServiceHelper.getLookupService().getLookup(parent, property.getName());
    }

    /**
     * Updates the underlying property with the specified value.
     *
     * @param property the property
     * @param lookup   the value to update with. May be {@code null}
     * @return {@code true} if the property was modified
     */
    @Override
    protected boolean updateProperty(Property property, Lookup lookup) {
        boolean modified = false;
        if (!property.isCollection()) {
            String value = (lookup != null) ? lookup.getCode() : null;
            modified = property.setValue(value);
        } else {
            // if its a collection property add the selected value to the collection, replacing any existing values
            CollectionProperty collection = (CollectionProperty) property;
            Collection values = collection.getValues();
            if (lookup == null) {
                // nothing selected, so remove any existing value
                if (!values.isEmpty()) {
                    for (Object value : values) {
                        collection.remove(value);
                    }
                    modified = true;
                }
            } else {
                // replace any existing values with the selected value
                if (!values.contains(lookup)) {
                    for (Object value : values) {
                        collection.remove(value);
                    }
                    collection.add(lookup);
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Lookup> createQuery(String name) {
        Query<Lookup> query = QueryFactory.create(shortName, getLayoutContext().getContext());
        query.setValue(name);
        return query;
    }
}
