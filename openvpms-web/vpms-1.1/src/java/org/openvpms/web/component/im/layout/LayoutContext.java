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

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;


/**
 * Layout context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface LayoutContext {

    /**
     * Returns the context.
     *
     * @return the context
     */
    Context getContext();

    /**
     * Sets the context.
     *
     * @param context the context
     */
    void setContext(Context context);

    /**
     * Determines if this is an edit context.
     *
     * @return <code>true</code> if this is an edit context; <code>false</code>
     *         if it is a view context. Defaults to <code>false</code>
     */
    boolean isEdit();

    /**
     * Sets if this is an edit context.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    void setEdit(boolean edit);

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    IMObjectComponentFactory getComponentFactory();

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    void setComponentFactory(IMObjectComponentFactory factory);

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <code>null</code>
     */
    NodeFilter getDefaultNodeFilter();

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <code>null</code>
     */
    void setNodeFilter(NodeFilter filter);

    /**
     * Returns the layout strategy factory.
     *
     * @return the layout strategy factory
     */
    IMObjectLayoutStrategyFactory getLayoutStrategyFactory();

    /**
     * Sets the layout strategy factory.
     *
     * @param factory the layout strategy factory
     */
    void setLayoutStrategyFactory(IMObjectLayoutStrategyFactory factory);

    /**
     * Returns the layout depth.
     *
     * @return the layout depth. If unset, defaults to <code>0</code>
     *         s
     */
    int getLayoutDepth();

    /**
     * Sets the layout depth.
     *
     * @param depth the depth
     */
    void setLayoutDepth(int depth);

}
