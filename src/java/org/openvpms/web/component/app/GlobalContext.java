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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Application context information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class GlobalContext extends AbstractContext {

    /**
     * The context listeners.
     */
    private List<ContextListener> listeners = new ArrayList<ContextListener>();


    /**
     * Restrict construction.
     */
    protected GlobalContext() {
    }

    /**
     * Adds a listener.
     *
     * @param listener the listener to add
     */
    public void addListener(ContextListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ContextListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the context associated with the current thread.
     *
     * @return the context associated with the current thread, or <tt>null</tt>
     */
    public static GlobalContext getInstance() {
        ContextApplicationInstance instance
                = ContextApplicationInstance.getInstance();
        return (instance != null) ? instance.getContext() : null;
    }

    /**
     * Sets a context object.
     *
     * @param key    the context key
     * @param object the object
     */
    @Override
    public void setObject(String key, IMObject object) {
        IMObject current = getObject(key);
        if (current != object) {
            // only update the context if the objects have different instances,
            // to avoid cyclic notifications
            super.setObject(key, object);
            notifyListeners(key, object);
        }
    }

    /**
     * Notifies listeners of a change of objects.
     *
     * @param key   the context key
     * @param value the context value
     */
    private void notifyListeners(String key, IMObject value) {
        ContextListener[] list = listeners.toArray(new ContextListener[0]);
        for (ContextListener listener : list) {
            listener.changed(key, value);
        }
    }
}
