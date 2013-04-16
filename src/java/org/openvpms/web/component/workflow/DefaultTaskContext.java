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
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.help.HelpContext;


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
     * Constructs a new {@code TaskContext} that inherits values from the global context.
     *
     * @param help the help context
     */
    public DefaultTaskContext(HelpContext help) {
        this(help, true);
    }

    /**
     * Constructs a {@code DefaultTaskContext} that inherits values from the specified context. If the specified context
     * is {@code null} then no inheritance occurs.
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
     * @param help   the help context
     */
    public DefaultTaskContext(Context context, Context parent, HelpContext help) {
        super(context, parent);
        this.help = help;
    }

    /**
     * Constructs a new {@code DefaultTaskContext} that inherits values
     * from the global context if inherit is {@code true}.
     *
     * @param inherit if {@code true} inherit values from the global context
     */
    public DefaultTaskContext(HelpContext help, boolean inherit) {
        super((inherit) ? GlobalContext.getInstance() : null);
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
