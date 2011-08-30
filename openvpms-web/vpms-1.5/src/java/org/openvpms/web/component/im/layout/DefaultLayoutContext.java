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

package org.openvpms.web.component.im.layout;

import org.openvpms.web.component.im.view.IMObjectComponentFactory;


/**
 * Default implmentation of the {@link LayoutContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutContext extends AbstractLayoutContext {

    /**
     * Construct a new <tt>DefaultLayoutContext</tt>.
     */
    public DefaultLayoutContext() {
    }

    /**
     * Construct a new <tt>DefaultLayoutContext</tt>.
     *
     * @param edit if <tt>true</tt> this is an edit context; if
     *             <tt>false</tt> it is a view context.
     */
    public DefaultLayoutContext(boolean edit) {
        super(edit);
    }

    /**
     * Construct a new <tt>DefaultLayoutContext</tt>.
     *
     * @param factory the component factory. May  be <tt>null</tt>
     */
    public DefaultLayoutContext(IMObjectComponentFactory factory) {
        super(factory);
    }

    /**
     * Construct a new <tt>DefaultLayoutContext</tt> from an existing
     * layout context. Increases the layout depth by 1.
     *
     * @param context the context
     */
    public DefaultLayoutContext(LayoutContext context) {
        super(context);
    }

}
