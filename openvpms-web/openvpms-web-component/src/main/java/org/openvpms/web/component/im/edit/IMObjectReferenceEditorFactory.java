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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.lang.reflect.InvocationTargetException;


/**
 * A factory for {@link IMObjectReferenceEditor} instances.
 *
 * @author Tim Anderson
 */
public class IMObjectReferenceEditorFactory {

    /**
     * Editor implementations.
     */
    private static ArchetypeHandlers<IMObjectReferenceEditor> editors;

    /**
     * Prevent construction.
     */
    private IMObjectReferenceEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     * @return an editor for {@code property}
     * @throws OpenVPMSException     for any OpenVPMS error
     * @throws IllegalStateException if a registered editor cannot be created for any other error
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectReferenceEditor<T> create(Property property, IMObject parent,
                                                                         LayoutContext context) {
        IMObjectReferenceEditor<T> result = null;

        String[] archetypes = property.getArchetypeRange();
        ArchetypeHandler handler = getEditors().getHandler(archetypes);

        if (handler != null) {
            try {
                result = (IMObjectReferenceEditor<T>) handler.create(
                        new Object[]{property, parent, context}, new Class[]{Property.class, IMObject.class,
                                                                             LayoutContext.class});
            } catch (InvocationTargetException exception) {
                if (exception.getCause() instanceof OpenVPMSException) {
                    throw (OpenVPMSException) exception.getCause();
                }
                throw new IllegalStateException("Failed to construct " + handler.getType().getName() + " for "
                                                + property.getName(), exception);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to construct " + handler.getType().getName() + " for "
                                                + property.getName(), throwable);
            }
        }
        if (result == null) {
            result = new DefaultIMObjectReferenceEditor<>(property, parent, context);
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers<IMObjectReferenceEditor> getEditors() {
        if (editors == null) {
            editors = new ArchetypeHandlers<>("IMObjectReferenceEditorFactory.properties",
                                              IMObjectReferenceEditor.class,
                                              ArchetypeServiceHelper.getArchetypeService());
        }
        return IMObjectReferenceEditorFactory.editors;
    }

}
