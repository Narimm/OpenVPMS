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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.List;

/**
 * An {@link IMObjectCollectionEditor} for collections with a maximum cardinality of {@code 1} that
 * provides an {@link IMObjectSelector} to select the collection object.
 *
 * @author Tim Anderson
 */
public class SelectorIMObjectCollectionEditor extends AbstractIMObjectCollectionEditor {

    /**
     * The property editor.
     */
    private PropertyEditor selector;


    /**
     * Constructs an {@link SelectorIMObjectCollectionEditor}.
     *
     * @param property the property
     * @param context  the layout context
     */
    public SelectorIMObjectCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
        selector = new Selector(property, context);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return selector.getFocusGroup();
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    @Override
    protected Component doLayout(LayoutContext context) {
        return selector.getComponent();
    }

    private class Selector extends AbstractSelectorPropertyEditor<IMObject> {

        /**
         * Constructs an [@link Selector}.
         *
         * @param property the property
         * @param context  the context
         */
        public Selector(CollectionProperty property, LayoutContext context) {
            super(property, context);
            updateSelector();
        }

        /**
         * Returns the object corresponding to the property.
         *
         * @return the object. May be {@code null}
         */
        @Override
        protected IMObject getObject() {
            List<IMObject> objects = getCollectionPropertyEditor().getObjects();
            return !objects.isEmpty() ? objects.get(0) : null;
        }

        /**
         * Updates the underlying property with the specified value.
         *
         * @param property the property
         * @param value    the value to update with. May be {@code null}
         * @return {@code true} if the property was modified
         */
        @Override
        protected boolean updateProperty(Property property, IMObject value) {
            boolean result = false;
            CollectionPropertyEditor editor = getCollectionPropertyEditor();
            List<IMObject> objects = editor.getObjects();
            if (value == null) {
                for (IMObject object : objects) {
                    result |= editor.remove(object);
                }
            } else if ((objects.size() == 1 && !ObjectUtils.equals(objects.get(0), value) || objects.size() > 1)) {
                for (IMObject object : objects) {
                    editor.remove(object);
                }
                editor.add(value);
                result = true;
            }
            return result;
        }
    }
}
