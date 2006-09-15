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

package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.DefaultCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Messaging workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessagingWorkspace extends AbstractWorkspace {

    /**
     * The current user. May be <code>null</code>.
     */
    private User user;

    /**
     * The root component.
     */
    private SplitPane root;

    /**
     * The workspace component.
     */
    private SplitPane workspace;

    /**
     * The CRUD window.
     */
    private CRUDWindow window;

    /**
     * The query.
     */
    private ActQuery query;

    /**
     * The act browser.
     */
    private Browser<Act> browser;


    /**
     * Construct a new <code>MessagingWorkspace</code>.
     */
    public MessagingWorkspace() {
        super("workflow", "messaging");
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<IMObject> rows = ArchetypeQueryHelper.get(
                service, "system", "security", "user", auth.getName(),
                true, 0, 1).getRows();
        if (!rows.isEmpty()) {
            user = (User) rows.get(0);
            GlobalContext.getInstance().setUser(user);
        }
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        // don't want this workspace participating in context changes, so
        // return false
        return false;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        user = (User) object;
        layoutWorkspace(user, root);
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public IMObject getObject() {
        return user;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "AbstractViewWorkspace.Layout");
        Component heading = super.doLayout();
        root.add(heading);
        if (user != null) {
            layoutWorkspace(user, root);
        }
        return root;
    }

    /**
     * Lays out the workspace.
     *
     * @param user      the user
     * @param container the container
     */
    protected void layoutWorkspace(User user, Component container) {
        query = createQuery(user);
        browser = createBrowser(query);
        window = createCRUDWindow();
        if (workspace != null) {
            container.remove(workspace);
        }
        workspace = createWorkspace(browser, window);
        container.add(workspace);
    }

    private SplitPane createWorkspace(Browser<Act> browser, CRUDWindow window) {
        Component acts = GroupBoxFactory.create(browser.getComponent());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "MessagingWorkspace.Layout", acts,
                                       window.getComponent());
    }

    private CRUDWindow createCRUDWindow() {
        return new DefaultCRUDWindow(
                DescriptorHelper.getDisplayName("act.userMessage"),
                new ShortNameList("act.userMessage"));
    }

    private TableBrowser<Act> createBrowser(ActQuery query) {
        return new TableBrowser<Act>(query, null);
    }

    private ActQuery createQuery(User user) {
        String[] shortNames = {"act.userMessage"};
        String[] statuses = {};

        return new DefaultActQuery(user, "user", "participation.user",
                                   shortNames, statuses);
    }

}
