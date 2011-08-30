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

package org.openvpms.web.component.im.edit.invoice;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerInvoiceEditor extends InvoiceEditor {

    /**
     * Constructs a new <code>CustomerInvoiceEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public CustomerInvoiceEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    public boolean save() {
        boolean saved = super.save();
        if (saved) {
            saved = processMedication();
        }
        return saved;
    }

    /**
     * Links medication acts associated with the invoice to the current
     * visit for the associated patient.
     *
     * @return <code>true</code> if medication was processed successfully
     */
    private boolean processMedication() {
        boolean saved = false;
        try {
            ActRelationshipCollectionEditor editor = getEditor();
            List<Act> medications = new ArrayList<Act>();
            for (Act act : editor.getActs()) {
                if (TypeHelper.isA(act, "act.customerAccountInvoiceItem")) {
                    ActBean bean = new ActBean(act);
                    for (Act medication :
                            bean.getActs("act.patientMedication")) {
                        medications.add(medication);
                    }
                }
            }
            Date startTime = ((Act) getObject()).getActivityStartTime();
            if (startTime == null) {
                startTime = new Date();
            }
            if (!medications.isEmpty()) {
                MedicalRecordRules rules = new MedicalRecordRules();
                rules.addToEvents(medications, startTime);
            }
            saved = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return saved;
    }

}