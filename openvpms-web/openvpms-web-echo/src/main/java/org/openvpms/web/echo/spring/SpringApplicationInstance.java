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

package org.openvpms.web.echo.spring;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.context.ApplicationContext;

/**
 * An {@code ApplicationInstance} for integrating Echo with Spring.
 *
 * @author Tim Anderson
 */
public abstract class SpringApplicationInstance extends ApplicationInstance {

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * Sets the application context.
     *
     * @param context the application context
     */
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Returns the application context.
     *
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Locks the application, until the user re-enters their password.
     * <p/>
     * Note that this method may be invoked outside a servlet request.
     */
    public abstract void lock();

    /**
     * Unlocks the application.
     * <p/>
     * Note that this method may be invoked outside a servlet request.
     */
    public abstract void unlock();

}
