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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Messaging workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessagingWorkspace extends AbstractWorkspace<User> {

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
    private CRUDWindow<Act> window;

    /**
     * The act browser.
     */
    private Browser<Act> browser;

    /**
     * The message query.
     */
    private Query<Act> query;


    /**
     * Construct a new <code>MessagingWorkspace</code>.
     */
    public MessagingWorkspace() {
        super("workflow", "messaging");
        user = GlobalContext.getInstance().getUser();
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
    public void setObject(User object) {
        user = object;
        layoutWorkspace(root);
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public User getObject() {
        return user;
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof User) {
            setObject((User) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + User.class.getName());
        }
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
                "MessagingWorkspace.MainLayout");
        Component heading = super.doLayout();
        root.add(heading);
        if (user != null) {
            layoutWorkspace(root);
        }
        return root;
    }

    /**
     * Determines if the workspace should be refreshed.
     * This implementation always returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same
     */
    @Override
    protected User getLatest() {
        return super.getLatest(GlobalContext.getInstance().getUser());
    }

    /**
     * Lays out the workspace.
     *
     * @param container the container
     */
    protected void layoutWorkspace(Component container) {
        if (query == null) {
            query = new MessageQuery(user);
        }
        if (browser == null) {
            browser = createBrowser(query);
            browser.addQueryListener(new QueryBrowserListener<Act>() {
                public void query() {
                    selectFirst();
                }

                public void selected(Act object) {
                    window.setObject(object);
                }
            });
        }
        if (window == null) {
            window = createCRUDWindow();
            window.setListener(new CRUDWindowListener<Act>() {
                public void saved(Act object, boolean isNew) {
                    browser.query();
                }

                public void deleted(Act object) {
                    browser.query();
                }

                public void refresh(Act object) {
                    browser.query();
                }
            });
            if (workspace != null) {
                container.remove(workspace);
            }
        }
        if (workspace == null) {
            workspace = createWorkspace(browser, window);
        }
        container.add(workspace);
        if (!query.isAuto()) {
            browser.query();
        }
    }

    /**
     * Creates the workspace.
     *
     * @param browser the act browser
     * @param window  the CRUD window
     * @return a new split pane representing the workspace
     */
    private SplitPane createWorkspace(Browser<Act> browser, CRUDWindow window) {
        Column acts = ColumnFactory.create("Inset", browser.getComponent());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "MessagingWorkspace.Layout", acts,
                                       window.getComponent());
    }

    /**
     * Creates the CRUD window.
     *
     * @return a new CRUD window
     */
    private CRUDWindow<Act> createCRUDWindow() {
        return new MessagingCRUDWindow(
                DescriptorHelper.getDisplayName("act.userMessage"),
                new ShortNameList("act.userMessage"));
    }

    /**
     * Creates the act browser.
     *
     * @param query the act query
     * @return a new act browser
     */
    private TableBrowser<Act> createBrowser(Query<Act> query) {
        return IMObjectTableBrowserFactory.create(query);
    }

    /**
     * Selects the first available message.
     */
    private void selectFirst() {
        List<Act> objects = browser.getObjects();
        if (!objects.isEmpty()) {
            Act current = objects.get(0);
            browser.setSelected(current);
            window.setObject(current);
        } else {
            window.setObject(null);
        }
    }

}
