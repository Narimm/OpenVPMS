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

package org.openvpms.web.component.im.layout;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;


/**
 * Default implementation of the {@link LayoutContext} interface.
 *
 * @author Tim Anderson
 */
public class DefaultLayoutContext extends AbstractLayoutContext {

    /**
     * Constructs a {@code DefaultLayoutContext}.
     *
     * @param help the help context
     */
    public DefaultLayoutContext(HelpContext help) {
        super(help);
    }

    /**
     * Constructs a {@code DefaultLayoutContext}.
     *
     * @param context the context
     * @param help    the help context
     */
    public DefaultLayoutContext(Context context, HelpContext help) {
        super(help);
        setContext(context);
    }

    /**
     * Constructs a new {@code DefaultLayoutContext}.
     *
     * @param edit if {@code true} this is an edit context; if {@code false} it is a view context.
     * @param help the help context
     */
    public DefaultLayoutContext(boolean edit, HelpContext help) {
        super(edit, help);
    }

    /**
     * Construct a new {@code DefaultLayoutContext}.
     *
     * @param factory the component factory. May  be {@code null}
     * @param help    the help context
     */
    public DefaultLayoutContext(IMObjectComponentFactory factory, HelpContext help) {
        super(factory, help);
    }

    /**
     * Construct a new {@code DefaultLayoutContext} from an existing
     * layout context. Increases the layout depth by 1.
     *
     * @param context the context
     */
    public DefaultLayoutContext(LayoutContext context) {
        super(context);
    }

}
