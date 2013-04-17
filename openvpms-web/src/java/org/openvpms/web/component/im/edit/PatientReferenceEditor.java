/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;


/**
 * Editor for <em>party.patientpet</em> references.
 *
 * @author Tim Anderson
 */
public class PatientReferenceEditor
        extends AbstractIMObjectReferenceEditor<Party> {

    /**
     * Determines if all patients should be allowed.
     */
    private boolean allPatients;


    /**
     * Constructs a {@code PatientReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object
     * @param context  the layout context
     */
    public PatientReferenceEditor(Property property, IMObject parent,
                                  LayoutContext context) {
        this(property, parent, context, false);
    }

    /**
     * Constructs a {@code PatientReferenceEditor}.
     *
     * @param property    the reference property
     * @param parent      the parent object
     * @param context     the layout context
     * @param allPatients if {@code true}, enable all patients to be
     *                    selected
     */
    public PatientReferenceEditor(Property property, IMObject parent,
                                  LayoutContext context, boolean allPatients) {
        super(property, parent, context);
        this.allPatients = allPatients;
    }

    /**
     * Sets the value of the reference to the supplied object.
     * <p/>
     * This implementation updates the global context.
     *
     * @param object the object. May  be {@code null}
     */
    @Override
    public boolean setObject(Party object) {
        ContextHelper.setPatient(getContext(), object);
        return super.setObject(object);
    }

    /**
     * Determines if all patients can be selected.
     *
     * @param all if {@code true} enable all patients to be selected
     */
    public void setAllPatients(boolean all) {
        allPatients = all;
    }

    /**
     * Creates a query to select objects.
     *
     * @param name a name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Query<Party> createQuery(String name) {
        Query<Party> query = super.createQuery(name);
        if (query instanceof PatientQuery) {
            // constrain patients to the current customer
            ((PatientQuery) query).setShowAllPatients(allPatients);
        }
        return query;
    }
}
