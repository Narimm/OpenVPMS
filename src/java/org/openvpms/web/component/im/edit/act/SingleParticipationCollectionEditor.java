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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.AbstractIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Editor for collections of {@link Participation}s with 0..1 or 1..1
 * cardinality.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class SingleParticipationCollectionEditor
        extends AbstractIMObjectCollectionEditor {

    /**
     * Inline editing container.
     */
    private Column _container;


    /**
     * Constructs a new <code>SingleParticipationCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public SingleParticipationCollectionEditor(CollectionProperty property,
                                               IMObject object,
                                               LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected Component doLayout(LayoutContext context) {
        _container = ColumnFactory.create();
        Component component = createComponent();
        _container.add(component);
        return _container;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid;
        AbstractParticipationEditor editor = getParticipationEditor();
        if (editor != null && getCollection().getMinCardinality() == 0) {
            if (!editor.isNull()) {
                valid = editor.validate(validator);
            } else {
                // entity is null. Exclude the participation from validation
                // as it will be removed on save
                valid = true;
            }
        } else {
            valid = super.validate(validator);
        }

        return valid;
    }

    /**
     * Saves any current edits.
     *
     * @return <code>true</code> if edits were saved successfully, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean doSave() {
        boolean saved;
        AbstractParticipationEditor editor = getParticipationEditor();
        if (editor != null && getCollection().getMinCardinality() == 0
                && editor.isNull()) {
            getCollectionPropertyEditor().remove(editor.getObject());
            setCurrentEditor(null);
            saved = super.doSave();
            _container.removeAll();
            _container.add(createComponent());
        } else {
            saved = super.doSave();
        }
        return saved;
    }

    /**
     * Returns the participation editor, or <code>null</code> if there is
     * no current participation editor
     *
     * @return the participation editor. May be <code>null</code>
     */
    protected AbstractParticipationEditor getParticipationEditor() {
        IMObjectEditor editor = getCurrentEditor();
        return (editor instanceof AbstractParticipationEditor) ?
                (AbstractParticipationEditor) editor : null;
    }

    private Component createComponent() {
        Component component;
        CollectionPropertyEditor collection = getCollectionPropertyEditor();
        String[] shortNames = collection.getArchetypeRange();
        List<IMObject> objects = collection.getObjects();
        IMObject object;
        String shortName = shortNames[0];
        if (objects.isEmpty()) {
            object = IMObjectCreator.create(shortName);
        } else {
            object = objects.get(0);
        }
        if (object != null) {
            IMObjectEditor editor = getEditor(object);
            setCurrentEditor(editor);
            component = editor.getComponent();
        } else {
            String message = Messages.get("imobject.create.failed",
                                          shortName);
            Label label = LabelFactory.create();
            label.setText(message);
            component = label;
        }
        return component;
    }

}
