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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.common.*;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Default act editor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultActEditor extends ActEditor {

    /**
     * Construct a new <code>DefaultActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public DefaultActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);

  //      initParticipation("customer", Context.getInstance().getCustomer());
        initParticipation("patient", Context.getInstance().getPatient());
    }

    /**
     * Initialises the customer participation, if the act has one.
     */
    protected void initCustomer() {
        Property property = getProperty("customer");
        if (property != null) {
            Participation participant = getParticipation(property);
            if (participant != null) {
                IMObject customer = Context.getInstance().getCustomer();
                if (customer == null) {
                    Party patient = Context.getInstance().getPatient();
                    if (patient != null) {
                        // @todo - need to ensure the relationship is active
                        EntityRelationship relationship
                                = (EntityRelationship) IMObjectHelper.getObject(
                                "entityRelationship.patientOwner",
                                patient.getEntityRelationships());
                        if (relationship != null) {
                            IMObjectReference ref = relationship.getSource();

                        }
                    }
                }
            }
        }
    }

    /**
     * Initialises a participation, if it exists and is empty.
     *
     * @param name   the participation name
     * @param entity the participation entity
     */
    protected void initParticipation(String name, IMObject entity) {
        Property property = getProperty(name);
        if (property != null) {
            Participation participant = getParticipation(property);
            if (participant != null) {
                if (participant.getAct() == null) {
                    participant.setAct(getObject().getObjectReference());
                }
                if (entity != null && participant.getEntity() == null) {
                    participant.setEntity(entity.getObjectReference());
                }
            }
        }
    }

    /**
     * Helper to return a participation.
     *
     * @param property the participation property
     * @return the participation
     */
    protected Participation getParticipation(Property property) {
        Object value = null;
        if (property instanceof CollectionProperty) {
            CollectionProperty c = (CollectionProperty) property;
            Object[] values = c.getValues().toArray();
            if (values.length > 0) {
                value = values[0];
            } else {
                String[] shortNames = DescriptorHelper.getShortNames(
                        property.getDescriptor());
                if (shortNames.length == 1) {
                    value = IMObjectCreator.create(shortNames[0]);
                    if (value != null) {
                        c.add(value);
                    }
                }
            }
        } else {
            value = property.getValue();
        }
        return (value instanceof Participation) ? (Participation) value : null;
    }

    /**
     * Update totals when an act item changes.
     * <p/>
     * todo - workaround for OVPMS-211
     */
    protected void updateTotals() {
        // no-op
    }
}
