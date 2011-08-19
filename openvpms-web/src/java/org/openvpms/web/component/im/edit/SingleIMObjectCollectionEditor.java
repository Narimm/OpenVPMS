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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Editor for collections of {@link IMObject}s with 0..1 or 1..1 cardinality.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class SingleIMObjectCollectionEditor
        extends AbstractIMObjectCollectionEditor {

    /**
     * Constructs a <tt>SingleIMObjectCollectionEditor</tt>.
     *
     * @param editor  the collection property
     * @param object  the parent object
     * @param context the layout context
     */
    public SingleIMObjectCollectionEditor(CollectionProperty editor,
                                          IMObject object,
                                          LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Construct a new <tt>SingleIMObjectCollectionEditor</tt>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected SingleIMObjectCollectionEditor(CollectionPropertyEditor editor,
                                             IMObject object,
                                             LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        mapObject();
        return isEmpty() || super.validate(validator);
    }

    /**
     * Refreshes the collection display.
     */
    public void refresh() {
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <tt>null</tt> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        IMObjectEditor editor = getCurrentEditor();
        return (editor != null) ? editor.getFocusGroup() : null;
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected Component doLayout(LayoutContext context) {
        return createComponent();
    }

    /**
     * Invoked when the collection or an editor changes. Notifies registered listeners.
     * <p/>
     * This implementation ensures that optional collection items are mapped to the collection when they change,
     * using {@link #mapObject}.
     *
     * @param modifiable the modifiable to pass to the listeners
     */
    @Override
    protected void modified(Modifiable modifiable) {
        super.modified(modifiable);
        mapObject();
    }

    /**
     * Saves any current edits.
     *
     * @return <tt>true</tt> if edits were saved successfully, otherwise
     *         <tt>false</tt>
     */
    @Override
    protected boolean doSave() {
        boolean saved;
        mapObject();
        if (isEmpty()) {
            // save the collection, excluding the current editor
            saved = getCollectionPropertyEditor().save();
        } else {
            saved = super.doSave();
        }
        return saved;
    }

    /**
     * Determines if the object being edited is empty.
     *
     * @return <tt>true</tt> if the object is empty
     */
    protected abstract boolean isEmpty();

    /**
     * Adds/removes the object to/from the collection.
     * If the object is empty and the min cardinality is zero, removes the
     * object from the collection otherwise adds it.
     */
    private void mapObject() {
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            CollectionPropertyEditor collection = getCollectionPropertyEditor();
            if (isEmpty()) {
                collection.remove(editor.getObject());
            } else if (editor.isModified()) {
                collection.add(editor.getObject());
            }
        }
    }

    /**
     * Creates the component.
     *
     * @return the component
     */
    private Component createComponent() {
        Component component;
        CollectionPropertyEditor collection = getCollectionPropertyEditor();
        String[] shortNames = collection.getArchetypeRange();
        List<IMObject> objects = collection.getObjects();
        IMObject object;
        String shortName = shortNames[0];
        if (objects.isEmpty()) {
            object = IMObjectCreator.create(shortName);
            IMObjectCreationListener listener = getCreationListener();
            if (object != null && listener != null) {
                listener.created(object);
            }
        } else {
            object = objects.get(0);
        }
        if (object != null) {
            IMObjectEditor editor = getEditor(object);
            setCurrentEditor(editor);
            component = editor.getComponent();
            mapObject();
        } else {
            String message = Messages.get("imobject.create.failed", shortName);
            Label label = LabelFactory.create();
            label.setText(message);
            component = label;
        }
        return component;
    }
}
