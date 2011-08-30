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

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Editor for <em>act.userMessage</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserMessageActEditor extends ActEditor {

    /**
     * Construct a new <code>UserMessageActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public UserMessageActEditor(Act act, IMObject parent,
                                LayoutContext context) {
        super(act, parent, context);

        initParticipant("from", context.getContext().getUser());
        initParticipant("to", context.getContext().getUser());
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("patient", context.getContext().getPatient());
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        getCustomerEditor().addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onCustomerChanged();
            }
        });
    }

    /**
     * Returns the customer editor.
     *
     * @return the customer editor
     */
    private CustomerParticipationEditor getCustomerEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor)
                getEditor("customer");
        return (CustomerParticipationEditor) editor.getCurrentEditor();
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor
     */
    private PatientParticipationEditor getPatientEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor)
                getEditor("patient");
        return (PatientParticipationEditor) editor.getCurrentEditor();
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no
     * relationship exists between the two..
     */
    private void onCustomerChanged() {
        try {
            Party customer = getCustomerEditor().getEntity();
            Party patient = getPatientEditor().getEntity();
            PatientRules rules = new PatientRules();
            if (customer != null && patient != null) {
                if (!rules.isOwner(customer, patient)) {
                    getPatientEditor().setEntity(null);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
