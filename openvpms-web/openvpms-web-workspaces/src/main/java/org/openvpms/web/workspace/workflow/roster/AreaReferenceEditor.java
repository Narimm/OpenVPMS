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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;

/**
 * Roster area reference editor.
 *
 * @author Tim Anderson
 */
public class AreaReferenceEditor extends AbstractIMObjectReferenceEditor<Entity> {

    /**
     * The practice location to constrain areas to.
     */
    private Party location;

    /**
     * Constructs an {@link AreaReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public AreaReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        super(property, parent, context);
        location = context.getContext().getLocation();
    }

    /**
     * Sets the location to constrain areas to.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = location;
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Entity> createQuery(String name) {
        AreaQuery query = new AreaQuery(getContext());
        query.setLocation(location);
        query.setValue(name);
        return query;
    }

    /**
     * Determines if a reference is valid.
     *
     * @param reference the reference to check
     * @return {@code true} if the query selects the reference
     */
    @Override
    protected boolean isValidReference(IMObjectReference reference) {
        AreaQuery query = new AreaQuery(getContext());
        query.setLocation(location);
        return query.selects(reference);
    }
}
