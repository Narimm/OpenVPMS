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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ProductQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductParticipationEditor
        extends AbstractParticipationEditor<Product> {

    /**
     * The patient, used to constrain searches to a particular species. May be
     * <tt>null</tt>.
     */
    private Property patient;


    /**
     * Constructs a new <tt>ProductParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public ProductParticipationEditor(Participation participation,
                                      Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.product")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the patient, used to constrain product searches to a set of
     * species.
     *
     * @param patient the patient. May be <tt>null</tt>
     */
    public void setPatient(Property patient) {
        this.patient = patient;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Product> createObjectReferenceEditor(
            Property property) {
        return new AbstractIMObjectReferenceEditor<Product>(
                property, getParent(), getLayoutContext()) {

            @Override
            protected Query<Product> createQuery(String name) {
                Query<Product> query = super.createQuery(name);
                return getQuery(query);
            }
        };
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query<Product> getQuery(Query<Product> query) {
        if (query instanceof ProductQuery && patient != null) {
            IMObjectReference ref = (IMObjectReference) patient.getValue();
            if (ref != null) {
                IMObject patient = IMObjectHelper.getObject(ref);
                if (patient != null) {
                    String species = (String) IMObjectHelper.getValue(
                            patient, "species");
                    if (species != null) {
                        ((ProductQuery) query).setSpecies(species);
                    }
                }
            }
        }
        return query;
    }

}
