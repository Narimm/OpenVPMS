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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.info.PatientContextHelper;


/**
 * CRUD Window for patient alerts.
 *
 * @author Tim Anderson
 */
public class AlertCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Constructs a {@link AlertCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public AlertCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(PatientArchetypes.ALERT, Act.class), ActActions.edit(), context, help);
    }

    /**
     * Invoked when the object has been saved.
     * <p/>
     * If the object is an allergy alert, registered listeners will be notified via the
     * {@link PatientInformationService}.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        super.onSaved(object, isNew);
        checkAllergyUpdate(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Act object) {
        super.onDeleted(object);
        checkAllergyUpdate(object);
    }

    /**
     * Invoked when an object is saved or deleted.
     * <p/>
     * If the object is an allergy alert, registered listeners are notified via the {@link PatientInformationService}.
     *
     * @param object the object
     */
    private void checkAllergyUpdate(Act object) {
        if (TypeHelper.isA(object, PatientArchetypes.ALERT)) {
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
            if (rules.isAllergy(object)) {
                PatientContext context = PatientContextHelper.getPatientContext(object, getContext());
                if (context != null) {
                    PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                    service.updated(context, getContext().getUser());
                }
            }
        }
    }

}
