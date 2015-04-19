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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Local context.
 * <p/>
 * This can inherit context information from a parent context, but any changes are kept locally.
 *
 * @author Tim Anderson
 */
public class LocalContext extends DelegatingContext {

    /**
     * Constructs a {@code LocalContext}, with no inheritance.
     */
    public LocalContext() {
        this(null);
    }

    /**
     * Constructs a {@code LocalContext} that inherits values from the specified context. If the specified context is
     * {@code null} then no inheritance occurs.
     *
     * @param parent the parent context. May be {@code null}
     */
    public LocalContext(Context parent) {
        this(new DefaultContext(), parent);
    }

    /**
     * Constructs a {@code LocalContext}
     *
     * @param context the context
     * @param parent  the parent context. May be {@code null}
     */
    public LocalContext(Context context, Context parent) {
        super(context, parent);
    }

    /**
     * Returns the parent context.
     *
     * @return the parent context. May be {@code null}
     */
    public Context getParent() {
        return getFallback();
    }

    /**
     * Creates a copy of a context.
     * <p/>
     * The copy does not inherit from the supplied context.
     *
     * @param context the context to copy
     * @return the copy
     */
    public static LocalContext copy(Context context) {
        LocalContext result = new LocalContext();
        for (IMObject object : context.getObjects()) {
            result.addObject(object);
        }
        // can't differentiate these two by archetype, so assign explicitly
        result.setUser(context.getUser());
        result.setClinician(context.getClinician());

        result.setScheduleDate(context.getScheduleDate());
        result.setWorkListDate(context.getWorkListDate());
        return result;
    }

    private static final class DefaultContext extends AbstractContext {

    }

}
