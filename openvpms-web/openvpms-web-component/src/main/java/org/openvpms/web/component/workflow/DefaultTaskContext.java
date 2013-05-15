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
 */

package org.openvpms.web.component.workflow;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Default implementation of the {@link TaskContext} interface.
 *
 * @author Tim Anderson
 */
public class DefaultTaskContext extends LocalContext implements TaskContext {

    /**
     * The help context.
     */
    private final HelpContext help;


    /**
     * Constructs a {@code DefaultTaskContext}.
     *
     * @param help the help context
     */
    public DefaultTaskContext(HelpContext help) {
        this(null, help);
    }

    /**
     * Constructs a {@code DefaultTaskContext} that inherits values from the specified context.
     *
     * @param parent the parent context. May be {@code null}
     * @param help   the help context
     */
    public DefaultTaskContext(Context parent, HelpContext help) {
        super(parent);
        this.help = help;
    }

    /**
     * Constructs a {@code DefaultTaskContext} that populates the specified context, and optionally inherits values
     * from a parent context.
     *
     * @param context the context
     * @param parent  the parent context. May be {@code null}
     * @param help    the help context
     */
    public DefaultTaskContext(Context context, Context parent, HelpContext help) {
        super(context, parent);
        this.help = help;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        return help;
    }
}
