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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;

import static org.openvpms.web.component.im.layout.ArchetypeNodes.include;

/**
 * Layout strategy for <em>act.customerCommunicationEmail</em> acts.
 *
 * @author Tim Anderson
 */
public class EmailCommunicationLayoutStrategy extends CommunicationLayoutStrategy {

    /**
     * The from node name.
     */
    public static final String FROM = "from";

    /**
     * The Cc node name.
     */
    public static final String CC = "cc";

    /**
     * The Bcc node name.
     */
    public static final String BCC = "bcc";

    /**
     * The attachments node name.
     */
    public static final String ATTACHMENTS = "attachments";

    /**
     * Constructs an {@link EmailCommunicationLayoutStrategy}.
     */
    public EmailCommunicationLayoutStrategy() {
        this(null, true);
    }

    /**
     * Constructs an {@link CommunicationLayoutStrategy}.
     *
     * @param message     the message property. May be {@code null}
     * @param showPatient determines if the patient node should be displayed when editing
     */
    public EmailCommunicationLayoutStrategy(Property message, boolean showPatient) {
        super(message, ContactArchetypes.EMAIL, showPatient);
    }

    /**
     * Apply the layout strategy.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        Property attachments = properties.get(ATTACHMENTS);
        if (attachments != null) {
            addComponent(createTextArea(attachments, 2, 10, context));
        }

        return super.apply(object, properties, parent, context);
    }

    /**
     * Add viewers to display contacts.
     *
     * @param properties the properties
     * @param object     the communication object
     * @param context    the layout context
     */
    @Override
    protected void addContactViewers(PropertySet properties, IMObject object, LayoutContext context) {
        super.addContactViewers(properties, object, context);
        addContactViewer(properties.get(FROM), object, context, true);
        addContactViewer(properties.get(CC), object, context, true);
        addContactViewer(properties.get(BCC), object, context, true);
    }

    /**
     * Adds contact editors.
     *
     * @param object     the communication object
     * @param properties the properties
     * @param customer   the customer
     * @param context    the layout context
     */
    @Override
    protected void addContactEditors(IMObject object, PropertySet properties, Party customer, LayoutContext context) {
        super.addContactEditors(object, properties, customer, context);
        addFromEmailSelector(properties.get(FROM), object, context);
        addContactSelector(properties.get(CC), object, customer, context);
        addContactSelector(properties.get(BCC), object, customer, context);
    }

    /**
     * Returns the properties to display in the header.
     *
     * @param properties the properties
     * @return the header properties
     */
    @Override
    protected List<Property> getHeaderProperties(List<Property> properties) {
        return ArchetypeNodes.include(properties, FROM, ADDRESS, CC, BCC, DESCRIPTION);
    }

    /**
     * Excludes empty header properties.
     *
     * @param properties the header properties
     * @return the properties to render
     */
    @Override
    protected List<Property> excludeEmptyHeaderProperties(List<Property> properties) {
        return excludeIfEmpty(properties, FROM, CC, BCC);
    }

    /**
     * Returns the text properties.
     * <p>
     * These are rendered under each other.
     *
     * @param properties the properties
     * @param message    the message property
     * @return the text properties
     */
    @Override
    protected List<Property> getTextProperties(List<Property> properties, Property message) {
        List<Property> result = super.getTextProperties(properties, message);
        result.addAll(include(properties, ATTACHMENTS));
        return result;
    }

    /**
     * Excludes empty text properties.
     *
     * @param properties the text properties
     * @return the properties to render
     */
    @Override
    protected List<Property> excludeEmptyTextProperties(List<Property> properties) {
        return excludeIfEmpty(properties, NOTE, ATTACHMENTS);
    }

    /**
     * Adds an editor for the from address.
     *
     * @param property the from address property
     * @param object   the communication object
     * @param context  the context
     */
    protected void addFromEmailSelector(Property property, IMObject object, LayoutContext context) {
        List<Contact> contacts = getContacts(context.getContext().getPractice());
        contacts.addAll(getContacts(context.getContext().getLocation()));
        addContactSelector(property, object, contacts, context);
    }
}
