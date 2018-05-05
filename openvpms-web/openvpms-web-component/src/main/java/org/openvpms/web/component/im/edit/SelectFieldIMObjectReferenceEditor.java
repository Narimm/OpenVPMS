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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.SimpleListModel;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link IMObjectCollectionEditor} that lists the available objects for selection in a {@code SelectField}.
 *
 * @author Tim Anderson
 */
public class SelectFieldIMObjectReferenceEditor<T extends IMObject>
        extends AbstractPropertyEditor implements IMObjectReferenceEditor<T> {

    /**
     * The objects, keyed on reference.
     */
    private final Map<IMObjectReference, T> map = new LinkedHashMap<>();

    /**
     * The select field.
     */
    private final SelectField field;

    /**
     * The focus group.
     */
    private final FocusGroup focus;

    /**
     * The field renderer.
     */
    private DelegatingRenderer renderer;

    /**
     * Constructs an {@link SelectFieldIMObjectReferenceEditor}.
     *
     * @param property the property being edited
     * @param all      if {@code true}, add a localised 'All'
     */
    public SelectFieldIMObjectReferenceEditor(Property property, List<T> objects, boolean all) {
        super(property);
        field = BoundSelectFieldFactory.create(property, new SimpleListModel<IMObjectReference>(all, false));
        setObjects(objects);

        renderer = new DelegatingRenderer();
        field.setCellRenderer(renderer);
        focus = new FocusGroup("SelectFieldIMObjectReferenceEditor", field);
    }

    /**
     * Sets the renderer for the select field.
     *
     * @param renderer the renderer
     */
    public void setListCellRenderer(ListCellRenderer renderer) {
        this.renderer.setCellRenderer(renderer);
    }

    /**
     * Sets the objects available for selection.
     *
     * @param objects the objects
     */
    @SuppressWarnings("unchecked")
    public void setObjects(List<T> objects) {
        SimpleListModel<IMObjectReference> model = (SimpleListModel<IMObjectReference>) field.getModel();
        map.clear();
        List<IMObjectReference> list = new ArrayList<>();
        for (T object : objects) {
            IMObjectReference reference = object.getObjectReference();
            map.put(reference, object);
            list.add(reference);
        }
        model.setObjects(list);
    }

    /**
     * Sets the value property to the supplied object.
     *
     * @param object the object. May  be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     * the existing value
     */
    public boolean setObject(T object) {
        boolean modified;
        Property property = getProperty();
        if (object != null) {
            modified = property.setValue(object.getObjectReference());
        } else {
            modified = property.setValue(null);
        }
        return modified;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none is selected
     */
    public T getObject() {
        return map.get(getProperty().getReference());
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        return field;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return {@code true} if the reference is null; otherwise {@code false}
     */
    @Override
    public boolean isNull() {
        return getProperty().getValue() == null;
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if {@code true}, objects may be created
     */
    @Override
    public void setAllowCreate(boolean create) {
        // no-op
    }

    /**
     * Determines if objects may be created.
     *
     * @return {@code false}
     */
    @Override
    public boolean allowCreate() {
        return false;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && isValidReference(validator);
    }

    /**
     * Determines if the reference is valid.
     *
     * @param validator the validator
     * @return {@code true} if the reference is valid, otherwise {@code false}
     */
    protected boolean isValidReference(Validator validator) {
        IMObjectReference reference = getProperty().getReference();
        boolean result = true;
        if (reference != null && !reference.isNew()) {
            result = isValidReference(reference, validator);
        }
        return result;
    }

    /**
     * Determines if the reference is valid, logging a validation error if not.
     *
     * @param reference the reference
     * @param validator the validator
     * @return {@code true} if the reference is valid, otherwise {@code false}
     */
    protected boolean isValidReference(IMObjectReference reference, Validator validator) {
        boolean result = map.containsKey(reference);
        if (!result) {
            ArchetypeId archetypeId = reference.getArchetypeId();
            String displayName = DescriptorHelper.getDisplayName(archetypeId.getShortName());
            String message = Messages.format("imobject.invalidreference", displayName);
            validator.add(this, new ValidatorError(getProperty(), message));
        }
        return result;
    }

    private class DelegatingRenderer implements ListCellRenderer {

        private ListCellRenderer renderer;

        public DelegatingRenderer() {
            setCellRenderer(IMObjectListCellRenderer.NAME);
        }

        public void setCellRenderer(ListCellRenderer renderer) {
            this.renderer = renderer;
        }

        /**
         * Renders an item in a list.
         *
         * @param list  the list component
         * @param value the item value
         * @param index the item index
         * @return the rendered form of the list cell
         */
        @Override
        public Object getListCellRendererComponent(Component list, Object value, int index) {
            IMObjectReference reference = (IMObjectReference) value;
            T object = map.get(reference);
            return renderer.getListCellRendererComponent(list, object, index);
        }
    }
}
