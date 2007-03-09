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

package org.openvpms.web.component.workflow;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.LocalContext;


/**
 * Default implementation of the {@link TaskContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultTaskContext extends LocalContext implements TaskContext {

    /**
     * Constructs a new <tt>TaskContext</tt> that inherits values from the
     * global context.
     */
    public DefaultTaskContext() {
        this(true);
    }

    /**
     * Constructs a new <tt>DefaultTaskContext</tt> that inherits values
     * from the specified context. If the specified context is <tt>null</tt>
     * then no inheritance occurs.
     *
     * @param parent the parent context. May be <tt>null</tt>
     */
    public DefaultTaskContext(Context parent) {
        super(parent);
    }

    /**
     * Constructs a new <tt>DefaultTaskContext</tt> that inherits values
     * from the global context if inherit is <tt>true</tt>.
     *
     * @param inherit if <tt>true</tt> inherit values from the global context
     */
    public DefaultTaskContext(boolean inherit) {
        super((inherit) ? GlobalContext.getInstance() : null);
    }
}
