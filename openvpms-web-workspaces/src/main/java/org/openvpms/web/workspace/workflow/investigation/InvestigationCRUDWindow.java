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

package org.openvpms.web.workspace.workflow.investigation;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.help.HelpContext;

/**
 * CRUD Window for <em>act.patientInvestigation</em> acts.
 *
 * @author Tim Anderson
 */
class InvestigationCRUDWindow extends ResultSetCRUDWindow<Act> {

    /**
     * Constructs an {@link InvestigationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public InvestigationCRUDWindow(Archetypes<Act> archetypes, Query<Act> query, ResultSet<Act> set, Context context,
                                   HelpContext help) {
        super(archetypes, InvestigationActions.INSTANCE, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createViewButton());
        buttons.add(createEditButton());
    }

    private static class InvestigationActions extends ActActions<Act> {

        public static final IMObjectActions<Act> INSTANCE = new InvestigationActions();

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
