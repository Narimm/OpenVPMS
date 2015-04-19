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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

import java.lang.reflect.Constructor;


/**
 * Factory for {@link IMObjectCollectionEditor} instances.
 * <p/>
 * This uses two configuration files:
 * <ol>
 * <li>EditableIMObjectCollectionEditorFactory.properties - used for implementations of
 * {@link EditableIMObjectCollectionEditor}</li>
 * <li>IMObjectCollectionEditorFactory.properties - used for implementations of {@link IMObjectCollectionEditor}
 * that have a maximum cardinality of 1</li>
 * </ol>
 * The former is used if the collection property returns {@code true} for
 * {@link CollectionProperty#isParentChild()}, indicating that the collection items are editable.
 *
 * @author Tim Anderson
 */
public class IMObjectCollectionEditorFactory {

    /**
     * Editors that implement {@link IMObjectCollectionEditor}.
     */
    private static ArchetypeHandlers<IMObjectCollectionEditor> editors;

    /**
     * Editors that implement {@link EditableIMObjectCollectionEditor}.
     */
    private static ArchetypeHandlers<EditableIMObjectCollectionEditor> editable;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectCollectionEditorFactory.class);


    /**
     * Prevent construction.
     */
    private IMObjectCollectionEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param property the collection to edit
     * @param object   the parent of the collection
     * @param context  the layout context
     * @return an editor for {@code collection}
     */
    public static IMObjectCollectionEditor create(CollectionProperty property, IMObject object, LayoutContext context) {
        IMObjectCollectionEditor result = null;
        String[] shortNames = property.getArchetypeRange();

        if (property.isParentChild()) {
            ArchetypeHandler<EditableIMObjectCollectionEditor> handler = getEditable().getHandler(shortNames);
            if (handler != null) {
                Class type = handler.getType();
                result = create(type, property, object, context);
            }
            if (result == null) {
                result = new DefaultIMObjectCollectionEditor(property, object, context);
            }
        } else {
            if (property.getMaxCardinality() == 1) {
                ArchetypeHandler<IMObjectCollectionEditor> handler = getEditors().getHandler(shortNames);
                if (handler != null) {
                    Class type = handler.getType();
                    result = create(type, property, object, context);
                }
            }
            if (result == null) {
                if (property.getMaxCardinality() == 1) {
                    result = new SelectFieldIMObjectCollectionEditor(property, object, context);
                } else {
                    result = new PaletteIMObjectCollectionEditor(property, object, context);
                }
            }
        }

        return result;
    }

    /**
     * Creates a new editor.
     *
     * @param type       the type to create
     * @param collection the collection property
     * @param object     the parent object
     * @param context    the layout context
     * @return a new editor, or {@code null} if it couldn't be created
     */
    private static IMObjectCollectionEditor create(Class type, CollectionProperty collection, IMObject object,
                                                   LayoutContext context) {
        IMObjectCollectionEditor result = null;
        Constructor ctor = getConstructor(type, collection, object, context);
        if (ctor != null) {
            try {
                result = (IMObjectCollectionEditor) ctor.newInstance(collection, object, context);
            } catch (Throwable throwable) {
                log.error(throwable, throwable);
            }
        } else {
            log.error("No valid constructor found for class: " + type.getName());
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers<IMObjectCollectionEditor> getEditors() {
        if (editors == null) {
            editors = new ArchetypeHandlers<IMObjectCollectionEditor>("IMObjectCollectionEditorFactory.properties",
                                                                      IMObjectCollectionEditor.class);
        }
        return editors;
    }

    /**
     * Returns the editors that implement {@link EditableIMObjectCollectionEditor}.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers<EditableIMObjectCollectionEditor> getEditable() {
        if (editable == null) {
            editable = new ArchetypeHandlers<EditableIMObjectCollectionEditor>(
                    "EditableIMObjectCollectionEditorFactory.properties", EditableIMObjectCollectionEditor.class);
        }
        return editable;
    }

    /**
     * Returns a constructor to construct a new editor.
     *
     * @param type       the editor type
     * @param collection the collection property
     * @param object     the parent of the collection
     * @param context    the layout context. May be {@code null}
     * @return a constructor to construct the editor, or {@code null} if none can be found
     */
    @SuppressWarnings("unchecked")
    private static Constructor getConstructor(Class type, CollectionProperty collection, IMObject object,
                                              LayoutContext context) {
        Constructor[] ctors = type.getConstructors();

        for (Constructor ctor : ctors) {
            // check parameters
            Class[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 3) {
                Class ctorCollection = ctorTypes[0];
                Class ctorObj = ctorTypes[1];
                Class ctorLayout = ctorTypes[2];

                if (ctorCollection.isAssignableFrom(collection.getClass())
                    && ctorObj.isAssignableFrom(object.getClass())
                    && ((context != null && ctorLayout.isAssignableFrom(context.getClass()))
                        || (context == null && LayoutContext.class.isAssignableFrom(ctorLayout)))) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
