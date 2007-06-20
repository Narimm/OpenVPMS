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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Reporting workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportingWorkspace extends AbstractWorkspace<Entity> {

    /**
     * The current user. May be <code>null</code>.
     */
    private User _user;

    /**
     * The selected report. May be <code>null</code>.
     */
    private Entity _object;

    /**
     * The workspace component.
     */
    private SplitPane workspace;

    /**
     * The entity browser.
     */
    private Browser<Entity> browser;

    /**
     * The action button row.
     */
    private Row _buttons;

    /**
     * The run button.
     */
    private Button _run;

    /**
     * Run button identifier.
     */
    private static final String RUN_ID = "run";

    /**
     * Button row style.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Construct a new <code>MessagingWorkspace</code>.
     */
    public ReportingWorkspace() {
        super("reporting", "reports");
        _user = GlobalContext.getInstance().getUser();
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
    public void setObject(Entity object) {
        _object = object;
        if (object != null) {
            enableButtons(true);
        } else {
            enableButtons(false);
        }
    }

    /**
     * Returns the object to to be run by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public Entity getObject() {
        return _object;
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Entity) {
            setObject((Entity) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Entity.class.getName());
        }
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        Component root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "MessagingWorkspace.MainLayout");
        Component heading = super.doLayout();
        root.add(heading);
        if (_user != null) {
            layoutWorkspace(_user, root);
        }
        return root;
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current user has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        User user = GlobalContext.getInstance().getUser();
        user = IMObjectHelper.reload(user);
        return IMObjectHelper.isSame(_user, user);
    }

    /**
     * Lays out the workspace.
     *
     * @param user      the user
     * @param container the container
     */
    protected void layoutWorkspace(User user, Component container) {
        ReportQuery query = createQuery(user);
        browser = createBrowser(query);
        browser.addQueryListener(new QueryBrowserListener<Entity>() {
            public void query() {
                selectFirst();
            }

            public void selected(Entity object) {
                setObject(object);
            }
        });

        _buttons = RowFactory.create(ROW_STYLE);
        layoutButtons(_buttons);
        enableButtons(false);
        if (workspace != null) {
            container.remove(workspace);
        }
        workspace = createWorkspace(browser);
        container.add(workspace);
        if (!query.isAuto()) {
            browser.query();
        }
    }

    /**
     * Creates the workspace.
     *
     * @param browser the entity browser
     * @return a new split pane representing the workspace
     */
    private SplitPane createWorkspace(Browser<Entity> browser) {
        Column entities = ColumnFactory.create("Inset", browser.getComponent());
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "ReportingWorkspace.Layout", getButtons(), entities);
    }

    /**
     * Creates the Entity browser.
     *
     * @param query the entity query
     * @return a new act browser
     */
    private TableBrowser<Entity> createBrowser(ReportQuery query) {
        return IMObjectTableBrowserFactory.create(query);
    }

    /**
     * Creates a new query.
     *
     * @param user the user to query
     * @return a new query
     */
    private ReportQuery createQuery(User user) {
        return new ReportQuery(user);
    }

    /**
     * Selects the first available report.
     */
    private void selectFirst() {
        List<Entity> objects = browser.getObjects();
        if (!objects.isEmpty()) {
            Entity current = objects.get(0);
            browser.setSelected(current);
            setObject(current);
        } else {
            setObject(null);
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(Row buttons) {
        buttons.add(getRunButton());
    }

    /**
     * Returns the run button.
     *
     * @return the run button
     */
    protected Button getRunButton() {
        if (_run == null) {
            _run = ButtonFactory.create(RUN_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onRun();
                }
            });
        }
        return _run;
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    protected void enableButtons(boolean enable) {
        if (enable) {
            if (_buttons.indexOf(_run) == -1) {
                _buttons.add(_run);
            }
        } else {
            _buttons.remove(_run);
        }
    }

    /**
     * Returns the button row.
     *
     * @return the button row
     */
    protected Row getButtons() {
        return _buttons;
    }

    /**
     * Invoked when the run button is pressed. Runs the
     * selected report.
     * TODO:  Currently set to use Birt Viewer app deployed
     * in local Tomcat passing report file name defined in template.
     * Need to create proper report generator implementation.
     */
    protected void onRun() {
        TemplateHelper helper = new TemplateHelper();
        Document doc = helper.getDocumentFromTemplate(getObject());
        //String uri = "http://localhost:8080/openvpms-viewer/frameset?__report=report/" + doc.getName();
        //Command command = new BrowserOpenWindowCommand(
        //        uri, "OpenVPMS Report Viewer",
        //        "width=800,height=600,resizable=yes,scrollbars=yes");
        //OpenVPMSApp.getInstance().enqueueCommand(command);
    }

}
