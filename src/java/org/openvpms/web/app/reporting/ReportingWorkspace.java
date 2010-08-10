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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * Reporting workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportingWorkspace extends AbstractReportingWorkspace<Entity> {

    /**
     * The current user. May be <tt>null</tt>.
     */
    private User user;

    /**
     * The entity browser.
     */
    private Browser<Entity> browser;

    /**
     * The run button.
     */
    private Button run;

    /**
     * Run button identifier.
     */
    private static final String RUN_ID = "run";


    /**
     * Construct a new <tt>ReportingWorkspace</tt>.
     */
    public ReportingWorkspace() {
        super("reporting", "reports", Entity.class);
        user = GlobalContext.getInstance().getUser();
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        if (user != null) {
            layoutWorkspace(user, container);
        }
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current user has changed.
     *
     * @return <tt>true</tt> if the workspace should be refreshed, otherwise
     *         <tt>false</tt>
     */
    @Override
    protected boolean refreshWorkspace() {
        User user = GlobalContext.getInstance().getUser();
        user = IMObjectHelper.reload(user);
        return IMObjectHelper.isSame(this.user, user);
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
        browser.addBrowserListener(new BrowserListener<Entity>() {
            public void query() {
                selectFirst();
            }

            public void selected(Entity object) {
                setObject(object);
            }

            public void browsed(Entity object) {
                setObject(object);
            }
        });
        Column entities = ColumnFactory.create("Inset", browser.getComponent());
        container.add(entities);
        if (!query.isAuto()) {
            browser.query();
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getRunButton());
    }

    /**
     * Returns the run button.
     *
     * @return the run button
     */
    protected Button getRunButton() {
        if (run == null) {
            run = ButtonFactory.create(RUN_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onRun();
                }
            });
        }
        return run;
    }

    /**
     * Invoked when the run button is pressed. Runs the selected report.
     */
    protected void onRun() {
        try {
            SQLReportPrinter printer = new SQLReportPrinter(getObject());
            InteractiveSQLReportPrinter iPrinter
                    = new InteractiveSQLReportPrinter(printer);
            iPrinter.print();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates the Entity browser.
     *
     * @param query the entity query
     * @return a new act browser
     */
    private Browser<Entity> createBrowser(ReportQuery query) {
        return BrowserFactory.create(query);
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

}
