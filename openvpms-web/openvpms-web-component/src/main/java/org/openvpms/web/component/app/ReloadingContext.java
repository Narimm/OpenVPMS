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

package org.openvpms.web.component.app;


import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * A context that reloads objects.
 *
 * @author Tim Anderson
 */
public class ReloadingContext extends DelegatingContext {

    /**
     * Determines if an object has been reloaded.
     */
    private final Set<IMObjectReference> reloaded;

    /**
     * If {@code true}, always reload objects, otherwise only load them on first access.
     */
    private final boolean reloadAlways;

    /**
     * Constructs a {@link ReloadingContext}.
     *
     * @param context the context to delegate to
     */
    public ReloadingContext(Context context) {
        this(context, false);
    }

    /**
     * Constructs a {@link ReloadingContext}.
     *
     * @param context      the context to delegate to
     * @param reloadAlways if {@code true}, always reload objects, otherwise only load them on first access.
     */
    public ReloadingContext(Context context, boolean reloadAlways) {
        super(context);
        this.reloadAlways = reloadAlways;
        reloaded = (!reloadAlways) ? new HashSet<IMObjectReference>() : null;
    }

    /**
     * Helper to invoke a get method and return the result.
     * <p/>
     * This first invokes the method on the context. If that returns {@code null} and the parent context
     * is non-null and not the same context, invokes it on the parent.
     *
     * @param methodName the name of the method to invoke
     * @return the method return value. May be {@code null}
     * @throws RuntimeException if the method cannot be invoked
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <T> T get(String methodName) {
        T result = super.get(methodName);
        if (result instanceof IMObject) {
            if (reloadAlways) {
                result = (T) IMObjectHelper.reload((IMObject) result);
            } else {
                IMObjectReference ref = ((IMObject) result).getObjectReference();
                if (!reloaded.contains(ref)) {
                    result = (T) IMObjectHelper.reload((IMObject) result);
                    reloaded.add(ref);
                }
            }
        }
        return result;
    }
}

