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

package org.openvpms.web.workspace.workflow.messaging;

import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.workspace.BrowserCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;


/**
 * Messaging workspace.
 *
 * @author Tim Anderson
 */
public class MessagingWorkspace extends BrowserCRUDWorkspace<User, Act> {

    /**
     * Constructs a {@code MessagingWorkspace}.
     *
     * @param context the context
     */
    public MessagingWorkspace(Context context) {
        super("workflow", "messaging", context, false);
        setArchetypes(User.class, UserArchetypes.USER);
        setChildArchetypes(Act.class, MessageArchetypes.USER, MessageArchetypes.SYSTEM);
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code true} if {@code shortName} is one of those in {@link #getChildArchetypes()}
     */
    @Override
    public boolean canUpdate(String shortName) {
        return super.canUpdate(shortName) || getChildArchetypes().contains(shortName);
    }

    /**
     * Updates the workspace with the specified object.
     *
     * @param object the object to update the workspace with
     */
    @Override
    public void update(IMObject object) {
        if (getArchetypes().contains(object)) {
            super.update(object);
        } else if (getChildArchetypes().contains(object)) {
            Act act = (Act) object;
            Browser<Act> browser = getBrowser();
            browser.setSelected(act);
            getCRUDWindow().setObject(act);
        }
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Returns the latest version of the user, defaulting to the context user
     * if there is no current user.
     *
     * @return the latest version of the user, or {@link #getObject()} if they are the same
     */
    @Override
    protected User getLatest() {
        User result;
        if (getObject() == null) {
            result = getLatest(getContext().getUser());
        } else {
            result = super.getLatest();
        }
        return result;
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        return new MessageBrowser((MessageQuery) query, new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new MessageQuery(getObject(), new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        return new MessagingCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

}
