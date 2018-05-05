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

import org.openvpms.archetype.rules.doc.TextDocumentHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for <em>act.customerCommunication</em> acts that have a document node.
 *
 * @author Tim Anderson
 */
public abstract class DocumentCommunicationEditor extends AbstractCommunicationEditor {

    /**
     * The message property.
     */
    private final Property message;

    /**
     * A proxy for the message node, which can either be stored as a string, or a document
     * if its length exceeds that supported by the database.
     */
    private final SimpleProperty messageProxy = new SimpleProperty("message", String.class);

    /**
     * The document property.
     */
    private final Property document;

    /**
     * The document handler.
     */
    private final TextDocumentHandler handler;

    /**
     * The supported contact archetype short names.
     */
    private final String contacts;


    /**
     * Constructs an {@link DocumentCommunicationEditor}.
     *
     * @param act      the act to edit
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     * @param contacts the contact archetype short names. May be {@code null}
     */
    public DocumentCommunicationEditor(Act act, IMObject parent, LayoutContext context, String contacts) {
        super(act, parent, context);
        this.contacts = contacts;

        // set up a proxy for the message. This will be saved as a document if it exceeds the character limit of the
        // underlying property
        message = getProperty("message");
        messageProxy.setMaxLength(-1);
        messageProxy.setDisplayName(message.getDisplayName());
        Macros macros = ServiceHelper.getMacros();
        messageProxy.setTransformer(new StringPropertyTransformer(messageProxy, false, macros, null,
                                                                  context.getVariables()));

        // disable macro expansion in the underlying property
        message.setTransformer(new StringPropertyTransformer(message, false));

        handler = new TextDocumentHandler(ServiceHelper.getArchetypeService());

        document = getProperty("document");
        messageProxy.setValue(message.getValue());
        if (!act.isNew()) {
            Document content = getDocument();
            if (content != null) {
                messageProxy.setValue(handler.toString(content));
            }
        }
    }

    /**
     * Determines if the object has been changed.
     *
     * @return {@code true} if the object has been changed
     */
    @Override
    public boolean isModified() {
        return super.isModified() || messageProxy.isModified();
    }

    /**
     * Saves the object.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void saveObject() {
        String text = messageProxy.getString();
        boolean deleteDoc = false;
        Document content = getDocument();
        if (text == null || text.length() <= message.getMaxLength()) {
            message.setValue(text);
            document.setValue(null);
            if (content != null) {
                deleteDoc = true;
            }
        } else {
            message.setValue(null);
            if (content == null) {
                content = handler.create("message", text);
                document.setValue(content.getObjectReference());
            } else {
                handler.update(content, text);
            }
            ServiceHelper.getArchetypeService().save(content);
        }

        super.saveObject();
        if (deleteDoc) {
            // need to save the parent before the document can be deleted
            ServiceHelper.getArchetypeService().remove(content);
        }
    }


    /**
     * Returns the message property.
     *
     * @return the message property
     */
    protected Property getMessage() {
        return messageProxy;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new CommunicationLayoutStrategy(messageProxy, contacts, getShowPatient());
    }

    /**
     * Returns the document.
     *
     * @return the document, or {@code null} if none exists
     */
    private Document getDocument() {
        return (Document) IMObjectHelper.getObject(document.getReference());
    }

}
