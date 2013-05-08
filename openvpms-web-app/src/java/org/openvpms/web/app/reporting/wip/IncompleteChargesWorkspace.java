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

package org.openvpms.web.app.reporting.wip;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.reporting.AbstractReportingWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Workspace to detail customer charges that are works-in-progress, i.e not POSTED.
 *
 * @author Tim Anderson
 */
public class IncompleteChargesWorkspace extends AbstractReportingWorkspace<Act> {

    /**
     * The query.
     */
    private Query<Act> query;


    /**
     * Constructs an {@code IncompleteChargesWorkspace}.
     *
     * @param context the context
     */
    public IncompleteChargesWorkspace(Context context, MailContext mailContext) {
        super("reporting", "wip", Act.class, context, mailContext);
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        IMObjectComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        query = new IncompleteChargesQuery();
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, new IncompleteChargesTableModel(context),
                                                                    context);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add("report", new ActionListener() {
            public void onAction(ActionEvent event) {
                onReport();
            }
        });
    }

    /**
     * Invoked when the 'Report' button is pressed.
     */
    private void onReport() {
        try {
            Context context = getContext();
            DocumentTemplateLocator locator = new ContextDocumentTemplateLocator("WORK_IN_PROGRESS_CHARGES", context);
            IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(query, locator, context);
            String title = Messages.get("reporting.wip.print");
            InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(title, printer, context,
                                                                               getHelpContext().subtopic("report"));
            iPrinter.setMailContext(getMailContext());
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    class IncompleteChargesTableModel extends AbstractActTableModel {

        public IncompleteChargesTableModel(LayoutContext context) {
            super(IncompleteChargesQuery.SHORT_NAMES, context);
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"customer", "status", "startTime", "amount"};
        }
    }
}
