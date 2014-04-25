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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;


/**
 * Editor for <em>party.customer*</em> parties.
 *
 * @author Tim Anderson
 */
public class CustomerEditor extends AbstractIMObjectEditor {

    /**
     * Treats the account type as a simple node.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().simple("type");

    /**
     * Constructs a {@link CustomerEditor}.
     *
     * @param customer the object to edit
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context. May be {@code null}.
     */
    public CustomerEditor(Party customer, IMObject parent, LayoutContext context) {
        super(customer, parent, context);

        if (customer.isNew()) {
            // add default contacts for new customers that don't have any
            if (customer.getContacts().isEmpty()) {
                PartyRules rules = new PartyRules(ServiceHelper.getArchetypeService());
                customer.setContacts(rules.getDefaultContacts());
            }

            // initialise the practice location if one is not already present
            Party location = context.getContext().getLocation();
            CollectionProperty property = getCollectionProperty("practice");
            if (location != null && property != null && property.size() == 0) {
                String[] range = property.getArchetypeRange();
                if (range.length == 1) {
                    IMObject object = IMObjectCreator.create(range[0]);
                    if (object instanceof IMObjectRelationship) {
                        IMObjectRelationship relationship = (IMObjectRelationship) object;
                        relationship.setTarget(location.getObjectReference());
                        property.add(object);
                    }
                }
            }
        }

        getLayoutContext().getContext().setCustomer(customer);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new DefaultLayoutStrategy(NODES);
    }
}
