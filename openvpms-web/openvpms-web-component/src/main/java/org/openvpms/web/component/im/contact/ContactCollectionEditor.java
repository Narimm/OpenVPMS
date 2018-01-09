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

package org.openvpms.web.component.im.contact;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Edits a collection of contacts.
 * <p/>
 * This:
 * <ul>
 * <li>ensures that only one contact of a particular type can be 'preferred'.</li>
 * <li>removes any new contacts that have been added, but are not modified. This behaviour is enabled via
 * {@link #setExcludeUnmodifiedContacts}</li>
 * </ul>
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
        super(new ContactCollectionPropertyEditor(property), object, context);
    }

    /**
     * Determines if new contacts that have not been modified should be excluded from save.
     *
     * @param exclude if {@code true}, don't save contacts that are new, and have not been modified
     */
    public void setExcludeUnmodifiedContacts(boolean exclude) {
        getCollectionPropertyEditor().setExcludeUnmodified(exclude);
    }

    /**
     * Creates a new object, subject to a short name being selected, and current collection cardinality. This must be
     * registered with the collection.
     * <p/>
     * This implementation will return any existing unmodified contact of the requested short
     *
     * @param shortName the archetype short name. May be {@code null}
     * @return a new object, or {@code null} if the object can't be created
     */
    @Override
    public IMObject create(String shortName) {
        Contact contact = (Contact) getCollectionPropertyEditor().getUnmodified(shortName);
        if (contact == null || getEditor(contact).isModified()) {
            contact = (Contact) super.create(shortName);
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
                for (org.openvpms.component.model.party.Contact c : ((Party) getObject()).getContacts()) {
                    Contact contact = (Contact) c;
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
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    @Override
    protected ContactCollectionPropertyEditor getCollectionPropertyEditor() {
        return (ContactCollectionPropertyEditor) super.getCollectionPropertyEditor();
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     * @return the editor
     */
    @Override
    protected IMObjectEditor edit(IMObject object) {
        IMObjectEditor editor = super.edit(object);
        if (excludedFromValidation(editor) && !editor.isValid()) {
            enableNavigation(true);
        }
        return editor;
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     * <p/>
     * This implementation overrides the default behaviour by ignoring objects that are new and unmodified if they are
     * excluded from validation and saving.
     *
     * @param validator the validator
     * @return {@code true} if the object is valid, otherwise {@code false}
     */
    @Override
    protected boolean addCurrentEdits(Validator validator) {
        boolean valid = true;
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            if (!excludedFromValidation(editor)) {
                valid = editor.validate(validator);
                if (valid) {
                    addEdited(editor);
                }
            } else {
                // add it even if it is invalid. It will be removed on save, if it doesn't change
                addEdited(editor);
            }
        }
        return valid;
    }

    /**
     * Determines if an editor is excluded from validation.
     *
     * @param editor the editor
     * @return {@code true} if the editor is excluded from validation
     */
    private boolean excludedFromValidation(IMObjectEditor editor) {
        return getCollectionPropertyEditor().excludeUnmodified() && editor.getObject().isNew()
               && !editor.isModified();
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

    private static class ContactCollectionPropertyEditor extends AbstractCollectionPropertyEditor {

        /**
         * New objects that may not have been modified.
         */
        private Set<IMObject> pending = new HashSet<>();

        /**
         * Determines if new, unmodified objects should excluded.
         */
        private boolean excludeUnmodified;

        /**
         * Constructs an {@link AbstractCollectionPropertyEditor}.
         *
         * @param property the collection property
         */
        public ContactCollectionPropertyEditor(CollectionProperty property) {
            super(property);
        }

        /**
         * Determines if new, unmodified objects should be excluded from validation and saving.
         *
         * @param excludeUnmodified if {@code true}, exclude unmodified objects from validation and saving
         */
        public void setExcludeUnmodified(boolean excludeUnmodified) {
            this.excludeUnmodified = excludeUnmodified;
        }

        /**
         * Determines if new, unmodified objects should be excluded from validation and saving.
         *
         * @return {@code true} if unmodified objects are excluded from validation and saving
         */
        public boolean excludeUnmodified() {
            return excludeUnmodified;
        }

        /**
         * Returns the first unmodified object with the specified short name.
         *
         * @param shortName the contact archetype short name
         * @return the the first unmodified object, or {@code null} if none is found
         */
        public IMObject getUnmodified(String shortName) {
            return shortName != null ? IMObjectHelper.getObject(shortName, pending) : null;
        }

        /**
         * Returns the objects in the collection.
         *
         * @return the objects in the collection
         */
        @Override
        @SuppressWarnings("unchecked")
        public List<IMObject> getObjects() {
            List<IMObject> result;
            if (pending.isEmpty()) {
                result = super.getObjects();
            } else {
                result = new ArrayList<>();
                result.addAll(super.getObjects());
                for (IMObject object : pending) {
                    if (!result.contains(object)) {
                        result.add(object);
                    }
                }
            }
            return result;
        }

        /**
         * Adds an object to the collection, if it doesn't exist.
         *
         * @param object the object to add
         * @return {@code true} if the object was added, otherwise {@code false}
         */
        @Override
        public boolean add(IMObject object) {
            boolean added;
            if (excludeUnmodified && object.isNew()) {
                added = pending.add(object);
                if (added) {
                    resetValid();
                }
            } else {
                added = super.add(object);
            }
            return added;
        }

        /**
         * Removes an object from the collection.
         * This removes any associated editor.
         *
         * @param object the object to remove
         * @return {@code true} if the object was removed
         */
        @Override
        public boolean remove(IMObject object) {
            boolean removed = super.remove(object);
            if (pending.remove(object)) {
                removed = true;
                resetValid();
            }
            return removed;
        }

        /**
         * Validates the object.
         *
         * @param validator the validator
         * @return {@code true} if the object and its descendants are valid otherwise {@code false}
         */
        @Override
        protected boolean doValidation(Validator validator) {
            // commit any pending objects that have been modified
            for (IMObject object : pending.toArray(new IMObject[pending.size()])) {
                IMObjectEditor editor = getEditor(object);
                if (editor != null && editor.isModified()) {
                    getProperty().add(object);
                    pending.remove(object);
                }
            }
            boolean result = true;
            IArchetypeService service = ServiceHelper.getArchetypeService();
            for (IMObject object : super.getObjects()) {
                result = doValidation(object, validator, service);
            }
            return result;
        }

        /**
         * Saves the collection.
         *
         * @throws OpenVPMSException if the save fails
         */
        @Override
        protected void doSave() {
            super.doSave();
            if (!pending.isEmpty()) {
                for (IMObject object : new ArrayList<>(pending)) {
                    if (!object.isNew()) {
                        pending.remove(object);
                    }
                }
            }
        }
    }
}
