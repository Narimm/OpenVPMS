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

package org.openvpms.web.echo.util;

import org.apache.commons.lang.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Manages a collection of {@link Runnable} callbacks that are held via weak reference to allow them to be garbage
 * collected.
 *
 * @author Tim Anderson
 */
public class WeakReferenceCallbacks {

    /**
     * The callbacks.
     */
    private final List<WeakReference<Runnable>> callbacks
            = Collections.synchronizedList(new ArrayList<WeakReference<Runnable>>());

    /**
     * Adds a callback.
     * <p/>
     * The caller must hold a strong reference to the callback, to prevent it being garbage collected.
     *
     * @param callback the callback
     */
    public void add(Runnable callback) {
        callbacks.add(new WeakReference<>(callback));
    }

    /**
     * Removes a callback.
     *
     * @param callback the callback
     */
    public void remove(Runnable callback) {
        synchronized (callbacks) {
            ListIterator<WeakReference<Runnable>> iterator = callbacks.listIterator();
            while (iterator.hasNext()) {
                WeakReference<Runnable> ref = iterator.next();
                Runnable delegate = ref.get();
                if (delegate == null || ObjectUtils.equals(callback, delegate)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Calls the callbacks.
     */
    public void call() {
        ArrayList<WeakReference<Runnable>> copy = new ArrayList<>(callbacks);
        ListIterator<WeakReference<Runnable>> iterator = copy.listIterator();
        while (iterator.hasNext()) {
            WeakReference<Runnable> ref = iterator.next();
            Runnable delegate = ref.get();
            if (delegate != null) {
                delegate.run();
            } else {
                iterator.remove();
            }
        }
    }

}
