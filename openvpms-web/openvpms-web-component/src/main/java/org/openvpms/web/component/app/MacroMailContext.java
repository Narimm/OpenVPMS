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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import org.openvpms.web.component.mail.MailContext;

/**
 * A {@link MailContext} that enables a macro context to be specified, but delegates all other behaviour to an underlying mail context.
 * <p/>
 * This is to enable sharing of common state.
 *
 * @author Tim Anderson
 */
public class MacroMailContext extends DelegatingMailContext {

    /**
     * The macro context.
     */
    private final Object macroContext;

    /**
     * Constructs a {@link MacroMailContext}.
     *
     * @param context      the context to delegate to
     * @param macroContext the object to evaluate macros against. May be {@code null}
     */
    public MacroMailContext(MailContext context, Object macroContext) {
        super(context);
        this.macroContext = macroContext;
    }

    /**
     * Returns the object to evaluate macros against.
     *
     * @return the object to evaluate macros against. May be {@code null}
     */
    @Override
    public Object getMacroContext() {
        return macroContext;
    }
}
