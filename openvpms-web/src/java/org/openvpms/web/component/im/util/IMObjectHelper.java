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

package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;

import java.util.Collection;


/**
 * {@link IMObject} helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(IMObjectHelper.class);


    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference. May be <code>null</code>
     * @return the object corresponding to <code>reference</code> or
     *         <code>null</code> if none exists
     */
    public static IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = Context.getInstance().getObject(reference);
            if (result == null) {
                try {
                    IArchetypeService service
                            = ArchetypeServiceHelper.getArchetypeService();
                    result = ArchetypeQueryHelper.getByObjectReference(
                            service, reference);
                } catch (OpenVPMSException error) {
                    _log.error(error, error);
                }
            }
        }
        return result;
    }

    /**
     * Reloads an object.
     *
     * @param object the object to reload. May be <code>null</code>
     * @return the object, or <code>null</code> if it couldn't be reloaded
     */
    public static IMObject reload(IMObject object) {
        IMObject result = null;
        if (object != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                result = ArchetypeQueryHelper.getByObjectReference(
                        service, object.getObjectReference());
            } catch (OpenVPMSException error) {
                _log.error(error, error);
            }
        }
        return result;
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Object getValue(IMObject object, String node) {
        IMObjectBean bean = new IMObjectBean(object);
        return (bean.hasNode(node)) ? bean.getValue(node) : null;
    }

    /**
     * Returns the first object instance from a collection with matching short
     * name.
     *
     * @param shortName the short name
     * @param objects   the objects to search
     * @return the first object from the collection with matching short name, or
     *         <code>null</code> if none exists.
     */
    public static <T extends IMObject> T
            getObject(String shortName, Collection<T> objects) {
        T result = null;
        for (T object : objects) {
            if (TypeHelper.isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }

}

