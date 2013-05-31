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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.style.Styles;


/**
 * Factory for editors of {@link IMObject} instances.
 *
 * @author Tim Anderson
 */
public class NodeEditorFactory extends AbstractEditableComponentFactory {

    /**
     * Collects the editors created by this factory.
     */
    private Editors editors;

    /**
     * Constructs an {@code NodeEditorFactory}.
     *
     * @param editors the editors
     * @param context the layout context
     */
    public NodeEditorFactory(Editors editors, LayoutContext context) {
        super(context, Styles.EDIT);
        this.editors = editors;
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param object   the parent object
     * @return a new editor for {@code property}
     */
    @Override
    protected Editor createCollectionEditor(CollectionProperty property, IMObject object) {
        Editor editor = super.createCollectionEditor(property, object);
        editors.add(editor);
        return editor;
    }

    /**
     * Creates an editor for an {@link IMObject}.
     *
     * @param object the object to edit
     * @param parent the object's parent. May be {@code null}
     * @return a new editor for {@code object}
     */
    @Override
    protected IMObjectEditor getObjectEditor(IMObject object, IMObject parent, LayoutContext context) {
        IMObjectEditor editor = super.getObjectEditor(object, parent, context);
        editors.add(editor);
        return editor;
    }

    /**
     * Returns an editor for an object reference property.
     *
     * @param property the object reference property
     * @param object   the parent object
     * @return a new editor for {@code property}
     */
    @Override
    protected Editor createObjectReferenceEditor(Property property, IMObject object) {
        Editor editor = super.createObjectReferenceEditor(property, object);
        editors.add(editor);
        return editor;
    }

    /**
     * Creates a {@link PropertyEditor} for a component.
     *
     * @param property  the property
     * @param component the component
     * @return a new editor
     */
    @Override
    protected PropertyEditor createPropertyEditor(Property property, Component component) {
        PropertyEditor editor = super.createPropertyEditor(property, component);
        editors.add(editor);
        return editor;
    }
}
