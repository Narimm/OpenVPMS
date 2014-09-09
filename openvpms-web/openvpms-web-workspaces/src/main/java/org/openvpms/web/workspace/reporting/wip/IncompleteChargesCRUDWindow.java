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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.wip;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

/**
 * CRUD window for incomplete charges.
 *
 * @author Tim Anderson
 */
class IncompleteChargesCRUDWindow extends ResultSetCRUDWindow<Act> {

    /**
     * Constructs an {@link IncompleteChargesCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public IncompleteChargesCRUDWindow(Archetypes<Act> archetypes, Query<Act> query, ResultSet<Act> set, Context context,
                                       HelpContext help) {
        super(archetypes, Actions.INSTANCE, query, set, context, help);
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
            IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(getQuery(), locator, context);
            String title = Messages.get("reporting.wip.print");
            InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(title, printer, context,
                                                                               getHelpContext().subtopic("report"));
            iPrinter.setMailContext(getMailContext());
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }


    private static class Actions extends ActActions<Act> {

        public static final IMObjectActions<Act> INSTANCE = new Actions();

        /**
         * Determines if objects can be created.
         *
         * @return {@code false}
         */
        @Override
        public boolean canCreate() {
            return false;
        }

        /**
         * Determines if an act can be edited.
         *
         * @param act the act to check
         * @return {@code true} if the act status isn't {@code POSTED}
         */
        @Override
        public boolean canEdit(Act act) {
            return false;
        }

        /**
         * Determines if an object can be deleted.
         *
         * @param object the object to check
         * @return {@code true} if the object can be deleted
         */
        @Override
        public boolean canDelete(Act object) {
            return false;
        }
    }
}

