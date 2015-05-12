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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.contact;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;

/**
 * Edits a collection of contacts.
 * <p/>
 * This ensures that only one contact of a particular type can be 'preferred'.
 *
 * @author Tim Anderson
 */
public class ContactCollectionEditor extends IMObjectTableCollectionEditor {

    /**
     * Preferred node identifier.
     */
    private static final String PREFERRED = "preferred";

    /**
     * Constructs a {@link ContactCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public ContactCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Creates a new object, subject to a short name being selected, and current collection cardinality. This must be
     * registered with the collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    @Override
    public IMObject create() {
        Contact contact = (Contact) super.create();
        if (contact != null) {
            boolean preferred = isPreferred(contact);
            if (preferred) {
                for (IMObject object : getCurrentObjects()) {
                    if (object.getArchetypeId().equals(contact.getArchetypeId()) && isPreferred(object)) {
                        IMObjectBean bean = new IMObjectBean(contact);
                        bean.setValue(PREFERRED, false);
                        break;
                    }
                }
            }
        }
        return contact;
    }

    /**
     * Invoked when the current editor is modified.
     */
    @Override
    protected void onCurrentEditorModified() {
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            Contact current = (Contact) editor.getObject();
            Property property = editor.getProperty(PREFERRED);
            if (property != null && property.getBoolean()) {
                for (Contact contact : ((Party) getObject()).getContacts()) {
                    if (!current.equals(contact) && current.getArchetypeId().equals(contact.getArchetypeId())) {
                        IMObjectEditor contactEditor = getEditor(contact);
                        if (contactEditor != null) {
                            contactEditor.getProperty(PREFERRED).setValue(false);
                        }
                    }
                }
            }
        }
        super.onCurrentEditorModified();
    }

    /**
     * Determines if a contact is preferred.
     *
     * @param contact the contact
     * @return {@code true} if the contact is preferred
     */
    private boolean isPreferred(IMObject contact) {
        IMObjectBean bean = new IMObjectBean(contact);
        return bean.hasNode(PREFERRED) && bean.getBoolean(PREFERRED);
    }

}
