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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * An editor for collections of <em>descriptor.assertionProperty</em>, <em>descriptor.propertyMap</em> and
 * <em>descriptor.PropertyList</em>.
 * <p/>
 * This does not make objects persistent, as this is handled via the parent object.
 *
 * @author Tim Anderson
 */
public class PropertyDescriptorCollectionEditor extends IMObjectTableCollectionEditor {

    /**
     * Constructs a {@link PropertyDescriptorCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public PropertyDescriptorCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(new Editor(property), object, context);
    }

    private static class Editor extends AbstractCollectionPropertyEditor {

        /**
         * Constructs an {@link Editor}.
         *
         * @param property the collection property
         */
        public Editor(CollectionProperty property) {
            super(property);
        }

        @Override
        protected void save(IMObject object, IArchetypeService service) {
            // don't save the object as it is stored by the parent
        }
    }
}
