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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;


/**
 * Represents a view of an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectView {

    /**
     * Returns the object being viewed.
     *
     * @return the object being viewed
     */
    IMObject getObject();

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    Component getComponent();

    /**
     * Determines if the view has been rendered.
     *
     * @return <tt>true</tt> if the view has been rendered, otherwise  <tt>false</tt>
     */
    boolean hasComponent();

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the component hasn't been
     *         rendered
     */
    FocusGroup getFocusGroup();

    /**
     * Returns the current layout.
     *
     * @return the layout. May be <code>null</code>
     */
    IMObjectLayoutStrategy getLayout();

    /**
     * Changes the layout.
     *
     * @param layout the new layout strategy
     */
    void setLayout(IMObjectLayoutStrategy layout);

    /**
     * Sets a listener to be notified when the component is rendered.
     *
     * @param listener the listener
     */
    void setLayoutListener(ActionListener listener);

    /**
     * Returns the help context for the view.
     *
     * @return the help context
     */
    HelpContext getHelpContext();

}
