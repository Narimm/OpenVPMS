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

package org.openvpms.web.component.im.clinician;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.property.Property;

/**
 * Editor for <em>security.user</em> references with clinician classifications.
 * <p>
 * This adds the selected clinician to the context.
 *
 * @author Tim Anderson
 */
public class ClinicianReferenceEditor extends AbstractIMObjectReferenceEditor<User> {

    /**
     * Constructs a {@link ClinicianReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public ClinicianReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Sets the value property to the supplied object.
     *
     * @param object the object. May  be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     * the existing value
     */
    @Override
    public boolean setObject(User object) {
        Context context = getLayoutContext().getContext();
        if (!UserHelper.useLoggedInClinician(context)) {
            context.setClinician(object);
        }
        return super.setObject(object);
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<User> createQuery(String name) {
        ClinicianQuery query = new ClinicianQuery(getProperty().getArchetypeRange(), getContext());
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
        ClinicianQuery query = new ClinicianQuery(getContext());
        query.setAllLocations(true); // don't restrict the clinician to a particular location after is has been entered
        return query.selects(reference);
    }
}
