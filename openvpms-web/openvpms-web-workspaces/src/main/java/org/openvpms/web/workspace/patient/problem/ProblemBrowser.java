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

package org.openvpms.web.workspace.patient.problem;

import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;


/**
 * Patient problem record browser.
 *
 * @author Tim Anderson
 */
public class ProblemBrowser extends AbstractPatientHistoryBrowser {

    /**
     * Constructs a {@link ProblemBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public ProblemBrowser(ProblemQuery query, LayoutContext context) {
        this(query, (ProblemTableModel) IMObjectTableModelFactory.create(ProblemTableModel.class, context), context);
    }

    /**
     * Constructs a {@link ProblemBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param model   the table model
     * @param context the layout context
     */
    public ProblemBrowser(ProblemQuery query, ProblemTableModel model, LayoutContext context) {
        super(query, model, context);
    }
}
