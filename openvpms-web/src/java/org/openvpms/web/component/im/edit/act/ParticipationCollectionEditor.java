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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.edit.AbstractIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.DefaultIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;


/**
 * Editor for collections of {@link Participation}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ParticipationCollectionEditor
        implements IMObjectCollectionEditor {

    /**
     * The editor to delegate to.
     */
    private AbstractIMObjectCollectionEditor editor;


    /**
     * Constructs a new <code>ParticipationCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public ParticipationCollectionEditor(CollectionProperty property,
                                         IMObject object,
                                         LayoutContext context) {
        String[] shortNames = property.getArchetypeRange();
        int max = property.getMaxCardinality();
        if (max == 1 && shortNames.length == 1) {
            editor = new SingleParticipationCollectionEditor(property, object,
                                                             context);
        } else {
            editor = new DefaultIMObjectCollectionEditor(property, object,
                                                         context);
        }
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getCollection() {
        return editor.getCollection();
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    public IMObject getObject() {
        return editor.getObject();
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return editor.getProperty();
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return editor.getComponent();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        return editor.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        editor.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        editor.addModifiableListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        editor.removeModifiableListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        return editor.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    public boolean validate(Validator validator) {
        return editor.validate(validator);
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    public boolean save() {
        return editor.save();
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return editor.isSaved();
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor, or <code>null</code> if there is no current
     *         editor
     */
    public IMObjectEditor getCurrentEditor() {
        return editor.getCurrentEditor();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return editor.getFocusGroup();
    }
}
