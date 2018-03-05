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

package org.openvpms.web.workspace.customer.communication;

import echopointng.DropDown;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.TextDocumentHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.messaging.AbstractMessageLayoutStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.component.im.layout.ArchetypeNodes.exclude;
import static org.openvpms.web.component.im.layout.ArchetypeNodes.include;
import static org.openvpms.web.component.im.layout.ComponentGrid.layout;


/**
 * Layout strategy for <em>act.customerCommunication*</em> acts.
 *
 * @author Tim Anderson
 */
public class CommunicationLayoutStrategy extends AbstractMessageLayoutStrategy {

    /**
     * The author node name.
     */
    public static final String AUTHOR = "author";

    /**
     * The address node name.
     */
    public static final String ADDRESS = "address";

    /**
     * The description node name.
     */
    public static final String DESCRIPTION = "description";

    /**
     * The location node name.
     */
    public static final String LOCATION = "location";

    /**
     * The patient node name.
     */
    public static final String PATIENT = "patient";

    /**
     * The start time node name.
     */
    public static final String START_TIME = "startTime";

    /**
     * The message node name.
     */
    public static final String MESSAGE = "message";

    /**
     * The note node name.
     */
    public static final String NOTE = "note";

    /**
     * The document node name.
     */
    public static final String DOCUMENT = "document";

    /**
     * The reason node name.
     */
    public static final String REASON = "reason";

    /**
     * The contact archetype short name.
     */
    private final String contacts;

    /**
     * If {@code true}, initialise the message proxy, otherwise use that provided.
     */
    private final boolean initMessage;

    /**
     * Determines if the patient node should be displayed when editing.
     */
    private final boolean showPatient;

    /**
     * The property representing the message.
     */
    private Property messageProxy;

    /**
     * Constructs a {@link CommunicationLayoutStrategy}.
     */
    public CommunicationLayoutStrategy() {
        this(null, null, true);
    }

    /**
     * Constructs a {@link CommunicationLayoutStrategy}.
     *
     * @param message     the message property. May be {@code null}
     * @param contacts    contact archetype short names. May be {@code null}
     * @param showPatient determines if the patient node should be displayed when editing
     */
    public CommunicationLayoutStrategy(Property message, String contacts, boolean showPatient) {
        this.messageProxy = message;
        this.contacts = contacts;
        this.showPatient = showPatient;
        initMessage = messageProxy == null;
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
        ActBean bean = new ActBean((Act) object);
        if (initMessage) {
            Property message = properties.get(MESSAGE);
            if (bean.hasNode(DOCUMENT)) {
                Document content = (Document) IMObjectHelper.getObject(bean.getReference(DOCUMENT));
                if (content != null) {
                    TextDocumentHandler handler = new TextDocumentHandler(ServiceHelper.getArchetypeService());
                    messageProxy = new SimpleProperty(MESSAGE, handler.toString(content), String.class,
                                                      message.getDisplayName());
                } else {
                    messageProxy = message;
                }
            }
        }
        if (!context.isEdit()) {
            addContactViewers(properties, object, context);
        } else if (contacts != null) {
            Party customer = (Party) bean.getNodeParticipant("customer");
            if (customer != null) {
                addContactEditors(object, properties, customer, context);
            }
        }

        ComponentState description = createComponent(properties.get(DESCRIPTION), object, context);
        if (description.getComponent() instanceof TextComponent) {
            ((TextComponent) description.getComponent()).setWidth(Styles.FULL_WIDTH);
        }
        addComponent(description);

        addComponent(createTextArea(messageProxy, 10, 20, context));
        Property note = properties.get(NOTE);
        if (note != null) {
            addComponent(createTextArea(note, 2, 10, context));
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
    protected void addContactViewers(PropertySet properties, IMObject object, LayoutContext context) {
        Property address = properties.get(ADDRESS);
        if (address != null) {
            addContactViewer(address, object, context, false);
        }
    }

    /**
     * Adds a contact viewer.
     *
     * @param property the property
     * @param object   the communication object
     * @param context  the layout context
     * @param optional if {@code true} and the property is empty, don't add a viewer
     */
    protected void addContactViewer(Property property, IMObject object, LayoutContext context, boolean optional) {
        String value = property.getString();
        if (!optional || !StringUtils.isEmpty(value)) {
            ComponentState state;
            int lines = StringUtils.countMatches(value, "\n") + 1;
            if (lines > 1) {
                state = createComponent(property, object, context);
                Component component = state.getComponent();
                if (component instanceof TextArea) {
                    TextArea text = (TextArea) component;
                    text.setHeight(new Extent(lines + 1, Extent.EM));
                }
            } else {
                // force it to display in one line
                DelegatingProperty p = new DelegatingProperty(property) {
                    @Override
                    public int getMaxLength() {
                        return 255;
                    }
                };
                state = createComponent(p, object, context);
            }
            addComponent(state);
        }
    }

    /**
     * Adds contact editors.
     *
     * @param properties the properties
     * @param object     the communication object
     * @param customer   the customer
     * @param context    the layout context
     */
    protected void addContactEditors(IMObject object, PropertySet properties, Party customer, LayoutContext context) {
        Property address = properties.get("address");
        if (address != null) {
            addContactSelector(address, object, customer, context);
        }
    }

    /**
     * Adds a contact editor.
     *
     * @param property the contact property
     * @param object   the communication object
     * @param customer the customer
     * @param context  the layout context
     */
    protected void addContactSelector(Property property, IMObject object, Party customer, LayoutContext context) {
        List<Contact> contacts = getContacts(customer);
        addContactSelector(property, object, contacts, context);
    }

    /**
     * Adds a contact selector.
     *
     * @param property the contact property
     * @param object   the communication object
     * @param contacts the available contacts
     * @param context  the layout context
     */
    protected void addContactSelector(final Property property, IMObject object, List<Contact> contacts,
                                      LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        ComponentState address = factory.create(property, object);
        Component addressComponent = address.getComponent();
        if (addressComponent instanceof TextArea) {
            ((TextArea) addressComponent).setHeight(new Extent(2, Extent.EM));
        }
        if (!contacts.isEmpty()) {
            final DropDown contactDropDown = new DropDown();
            final IMObjectTable<Contact> table = new IMObjectTable<>();
            table.setObjects(contacts);
            table.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    Contact contact = table.getSelected();
                    if (contact != null) {
                        property.setValue(formatContact(contact));
                    }
                    contactDropDown.setExpanded(false);
                }
            });
            contactDropDown.setTarget(addressComponent);
            contactDropDown.setPopUpAlwaysOnTop(true);
            contactDropDown.setFocusOnExpand(true);
            contactDropDown.setPopUp(table);
            contactDropDown.setFocusComponent(table);
            addComponent(new ComponentState(contactDropDown, property));
        } else {
            addComponent(address);
        }
    }

    /**
     * Formats a contact.
     * <p>
     * This version returns the contact description
     *
     * @param contact the contact to format
     * @return the formatted contact
     */
    protected String formatContact(Contact contact) {
        return contact.getDescription();
    }

    /**
     * Returns the contacts for a party.
     *
     * @param party the party
     * @return the contacts
     */
    protected List<Contact> getContacts(Party party) {
        List<Contact> result = new ArrayList<>();
        for (org.openvpms.component.model.party.Contact contact : party.getContacts()) {
            if (contact.isA(contacts)) {
                result.add((Contact) contact);
            }
        }
        return result;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        ArchetypeDescriptor archetype = context.getArchetypeDescriptor(object);
        ArchetypeNodes nodes = getArchetypeNodes();
        NodeFilter filter = getNodeFilter(object, context);

        List<Property> simple = nodes.getSimpleNodes(properties, archetype, object, filter);
        List<Property> complex = nodes.getComplexNodes(properties, archetype, object, filter);

        List<Property> author = include(simple, AUTHOR);
        List<Property> header = getHeaderProperties(simple);
        List<Property> patient = include(simple, LOCATION, PATIENT);
        List<Property> fields = exclude(simple, START_TIME, MESSAGE, NOTE);
        List<Property> text = getTextProperties(simple, messageProxy);
        fields.removeAll(author);
        fields.removeAll(header);
        fields.removeAll(patient);
        fields.removeAll(text);

        if (!showPatient) {
            patient = ArchetypeNodes.exclude(patient, PATIENT);
        }

        if (!context.isEdit()) {
            // hide empty nodes in view layout
            author = excludeEmptyAuthorProperties(author);
            patient = excludeEmptyPatientProperties(patient);
            header = excludeEmptyHeaderProperties(header);
            fields = excludeEmptyFields(fields);
            text = excludeEmptyTextProperties(text);
        }

        ComponentGrid componentGrid = new ComponentGrid();
        ComponentSet authorSet = createComponentSet(object, author, context);
        ComponentSet headerSet = createComponentSet(object, header, context);
        ComponentSet patientSet = createComponentSet(object, patient, context);
        ComponentSet fieldSet = createComponentSet(object, fields, context);
        ComponentSet textSet = createComponentSet(object, text, context);
        componentGrid.add(authorSet);
        componentGrid.set(0, 3, layout(Alignment.ALIGN_RIGHT), createDate((Act) object));
        componentGrid.add(headerSet, 1, 2);
        if (patientSet.size() != 0) {
            // in view mode, display location and patient on separate rows as it looks odd on wide-screens.
            // In the dialog (width-constrained) it looks better on one row.
            int columns = (context.isEdit()) ? patientSet.size() : 1;
            componentGrid.add(patientSet, columns);
        }
        componentGrid.add(fieldSet, 2);
        componentGrid.add(textSet, 1, 2);
        Grid grid = createGrid(componentGrid);
        grid.setWidth(Styles.FULL_WIDTH);

        Component child = ColumnFactory.create(Styles.LARGE_INSET, grid);
        doComplexLayout(object, parent, complex, child, context);

        container.add(child);
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
    protected List<Property> getTextProperties(List<Property> properties, Property message) {
        List<Property> result = new ArrayList<>();
        result.add(message);
        result.addAll(include(properties, NOTE));
        return result;
    }

    /**
     * Returns the properties to display in the header.
     * <p>
     * Note; this does not exclude empty properties the returned properties are used to determine the other nodes
     * to display. Empty properties are excluded via {@link #excludeEmptyHeaderProperties(List)} instead.
     *
     * @param properties the properties
     * @return the header properties
     */
    protected List<Property> getHeaderProperties(List<Property> properties) {
        return include(properties, ADDRESS, DESCRIPTION);
    }

    /**
     * Excludes empty properties related to the author.
     *
     * @param properties the author properties
     * @return the properties to render
     */
    protected List<Property> excludeEmptyAuthorProperties(List<Property> properties) {
        return excludeIfEmpty(properties, AUTHOR);
    }

    /**
     * Excludes empty properties related to the patient.
     *
     * @param properties the patient properties
     * @return the properties to render
     */
    protected List<Property> excludeEmptyPatientProperties(List<Property> properties) {
        return excludeIfEmpty(properties, PATIENT, LOCATION);
    }

    /**
     * Excludes empty header properties.
     * <p>
     * This implementation returns the properties unchanged.
     *
     * @param properties the header properties
     * @return the properties to render
     */
    protected List<Property> excludeEmptyHeaderProperties(List<Property> properties) {
        return properties;
    }

    /**
     * Excludes empty fields.
     *
     * @param properties the fields
     * @return the properties to render
     */
    protected List<Property> excludeEmptyFields(List<Property> properties) {
        return excludeIfEmpty(properties, REASON);
    }

    /**
     * Excludes empty text properties.
     *
     * @param properties the text properties
     * @return the properties to render
     */
    protected List<Property> excludeEmptyTextProperties(List<Property> properties) {
        return excludeIfEmpty(properties, NOTE);
    }

    /**
     * Helper to exclude properties if they are empty.
     *
     * @param properties the properties
     * @param names      the names of the properties to exclude
     * @return the filtered properties
     */
    protected List<Property> excludeIfEmpty(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<>(properties);
        for (Property property : properties) {
            for (String name : names) {
                if (property.getName().equals(name) && property.isEmpty()) {
                    result.remove(property);
                }
            }
        }
        return result;
    }

    /**
     * Creates a component for a multi-line text node.
     *
     * @param property the property
     * @param minLines the minimum lines to display
     * @param maxLines the maximum lines to display
     * @param context  the layout context
     * @return a new component
     */
    protected ComponentState createTextArea(Property property, int minLines, int maxLines, LayoutContext context) {
        TextArea text = BoundTextComponentFactory.createTextArea(property);

        int lines = StringUtils.countMatches(property.getString(), "\n") + 1;
        if (lines < minLines) {
            lines = minLines;
        } else if (lines > maxLines) {
            lines = maxLines;
        }
        text.setHeight(new Extent(lines + 1, Extent.EM));
        text.setWidth(Styles.FULL_WIDTH);
        text.setStyleName(context.isEdit() ? Styles.EDIT : Styles.DEFAULT);
        return new ComponentState(text, property);
    }

}
