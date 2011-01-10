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

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.alert.AbstractAlertActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>act.patientAlert</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientAlertActEditor extends AbstractAlertActEditor {

    /**
     * Constructs a <tt>PatientAlertActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be <tt>null</tt>
     * @param context the layout context
     */
    public PatientAlertActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context, "lookup.patientAlertType");
        initParticipant("patient", context.getContext().getPatient());
    }

}