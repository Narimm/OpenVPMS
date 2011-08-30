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

package org.openvpms.web.component.im.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;
import org.openvpms.web.component.property.CollectionProperty;

import java.lang.reflect.Constructor;


/**
 * Factory for {@link IMObjectCollectionViewer}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCollectionViewerFactory {

    /**
     * Viewer implementations.
     */
    private static ArchetypeHandlers<IMObjectCollectionViewer> viewers;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(IMObjectCollectionViewerFactory.class);


    /**
     * Prevent construction.
     */
    private IMObjectCollectionViewerFactory() {
    }

    /**
     * Creates a new viewer.
     *
     * @param collection the collection to view
     * @param object     the parent of the collection
     * @param context    the layout context. May  be <tt>null</tt>
     * @return a viewer for the collection
     */
    public static IMObjectCollectionViewer create(CollectionProperty collection,
                                                  IMObject object,
                                                  LayoutContext context) {
        IMObjectCollectionViewer result = null;

        String[] shortNames = collection.getArchetypeRange();
        ArchetypeHandler handler = getViewers().getHandler(shortNames);
        if (handler != null) {
            Class type = handler.getType();
            Constructor ctor = getConstructor(type, collection, object,
                                              context);
            if (ctor != null) {
                try {
                    result = (IMObjectCollectionViewer) ctor.newInstance(
                            collection, object, context);
                } catch (Throwable throwable) {
                    log.error(throwable, throwable);
                }
            } else {
                log.error("No valid constructor found for class: "
                        + type.getName());
            }
        }
        if (result == null) {
            result = new DefaultIMObjectCollectionViewer(collection, object,
                                                         context);
        }
        return result;
    }

    /**
     * Returns the Viewers.
     *
     * @return the Viewers
     */
    private static synchronized ArchetypeHandlers<IMObjectCollectionViewer>
            getViewers() {
        if (viewers == null) {
            viewers = new ArchetypeHandlers<IMObjectCollectionViewer>(
                    "IMObjectCollectionViewerFactory.properties",
                    IMObjectCollectionViewer.class);
        }
        return viewers;
    }

    /**
     * Returns a constructor to construct a new viewer.
     *
     * @param type       the Viewer type
     * @param collection the collection property
     * @param object     the parent of the collection
     * @param context    the layout context. May be <tt>null</tt>
     * @return a constructor to construct the viewer, or <code>null</code> if
     *         none can be found
     */
    private static Constructor getConstructor(Class type,
                                              CollectionProperty collection,
                                              IMObject object,
                                              LayoutContext context) {
        Constructor[] ctors = type.getConstructors();

        for (Constructor ctor : ctors) {
            // check parameters
            Class[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 3) {
                Class ctorCollection = ctorTypes[0];
                Class ctorObj = ctorTypes[1];
                Class ctorCtx = ctorTypes[2];

                if (ctorCollection.isAssignableFrom(collection.getClass())
                        && ctorObj.isAssignableFrom(object.getClass())
                        && ((context != null && ctorCtx.isAssignableFrom(
                        context.getClass()))
                        || (context == null
                        && LayoutContext.class.isAssignableFrom(ctorCtx)))) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
