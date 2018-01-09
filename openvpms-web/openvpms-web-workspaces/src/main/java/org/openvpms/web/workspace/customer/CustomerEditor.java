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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.contact.ContactCollectionEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
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

        CollectionProperty contacts = getCollectionProperty("contacts");
        if (contacts != null) {
            ContactCollectionEditor editor = createContactCollectionEditor(customer, context, contacts);
            getEditors().add(editor);

            if (contacts.getMinCardinality() == 0) {
                editor.setExcludeUnmodifiedContacts(true);
            }
            addContact(editor, ContactArchetypes.LOCATION);
            addContact(editor, ContactArchetypes.PHONE);
            addContact(editor, ContactArchetypes.EMAIL);
        }
        getLayoutContext().getContext().setCustomer(customer);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new CustomerEditor(reload((Party) getObject()), getParent(), getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        DefaultLayoutStrategy strategy = new DefaultLayoutStrategy(NODES);
        ContactCollectionEditor editor = getContacts();
        if (editor != null) {
            strategy.addComponent(new ComponentState(editor));
        }
        return strategy;
    }

    /**
     * Creates a new editor for contacts.
     *
     * @param customer the customer
     * @param context  the context
     * @param contacts the contacts property
     * @return a new editor for contacts
     */
    protected ContactCollectionEditor createContactCollectionEditor(Party customer, LayoutContext context,
                                                                    CollectionProperty contacts) {
        return new ContactCollectionEditor(contacts, customer, context);
    }

    /**
     * Returns the contacts editor.
     *
     * @return the contacts editor, or {@code null} if none is registered
     */
    protected ContactCollectionEditor getContacts() {
        Editor editor = getEditor("contacts", false);
        return (editor instanceof ContactCollectionEditor) ? (ContactCollectionEditor) editor : null;
    }

    /**
     * Add a contact if no instance currently exists.
     *
     * @param editor    the contact editor
     * @param shortName the contact archetype short name
     * @return the contact, or {@code null} if none was added
     */
    protected Contact addContact(ContactCollectionEditor editor, String shortName) {
        Contact contact = null;
        if (IMObjectHelper.getObject(shortName, editor.getCurrentObjects()) == null) {
            contact = (Contact) IMObjectCreator.create(shortName);
            if (contact != null) {
                ServiceHelper.getArchetypeService().deriveValues(contact);
                editor.add(contact);
            }
        }
        return contact;
    }
}
