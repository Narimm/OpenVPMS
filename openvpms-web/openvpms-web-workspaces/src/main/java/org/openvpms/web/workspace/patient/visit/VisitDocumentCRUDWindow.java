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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.mr.PatientDocumentCRUDWindow;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;


/**
 * Visit document CRUD window.
 *
 * @author Tim Anderson
 */
public class VisitDocumentCRUDWindow extends PatientDocumentCRUDWindow {

    /**
     * Constructs a {@code VisitDocumentCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public VisitDocumentCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(PatientDocumentQuery.DOCUMENT_SHORT_NAMES, DocumentAct.class,
                                Messages.get("patient.document.createtype")), context, help);
        setMailContext(new CustomerMailContext(context, help));
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        return getContainer();
    }

    /**
     * Creates a layout context for viewing objects.
     *
     * @return a new layout context
     */
    @Override
    protected LayoutContext createViewLayoutContext() {
        LayoutContext context = super.createViewLayoutContext();
        context.setContextSwitchListener(null);
        return context;
    }
}
