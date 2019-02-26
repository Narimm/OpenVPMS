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

package org.openvpms.web.workspace.patient.insurance.policy;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.identity.IdentityCollectionEditor;
import org.openvpms.web.component.im.edit.identity.IdentityEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * Editor for <em>act.patientInsurancePolicy</em>.
 *
 * @author Tim Anderson
 */
public class PolicyEditor extends AbstractActEditor {

    /**
     * The insurance rules.
     */
    private final InsuranceRules rules;

    /**
     * The persistent policy number, used to verify that the policy isn't changed once claims have
     * been submitted.
     */
    private String savedPolicyNumber;

    /**
     * Constructs an {@link PolicyEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public PolicyEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        rules = ServiceHelper.getBean(InsuranceRules.class);
        if (act.isNew()) {
            initParticipant("customer", context.getContext().getCustomer());
            initParticipant("patient", context.getContext().getPatient());
        }
        addStartEndTimeListeners();
        savedPolicyNumber = rules.getPolicyNumber(act);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new PolicyEditor(reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Sets the policy number.
     *
     * @param policyNumber the policy number. May be {@code null}
     */
    public void setPolicyNumber(String policyNumber) {
        IdentityCollectionEditor editor = (IdentityCollectionEditor) getEditor("insurerId");
        IdentityEditor currentEditor = (IdentityEditor) editor.getCurrentEditor();
        if (currentEditor != null) {
            currentEditor.getProperty("identity").setValue(policyNumber);
        } else if (policyNumber != null) {
            ActIdentity identity = (ActIdentity) editor.create();
            identity.setIdentity(policyNumber);
            editor.add(identity);
        }
    }

    /**
     * Save any edits.
     * <p>
     * This uses {@link #saveChildren()} to save the children prior to invoking {@link #saveObject()}.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        Act object = getObject();
        if (!object.isNew()) {
            // verify that associated claim statuses haven't updated since the policy was edited.
            // NOTE: there is still a small possibility that a POSTED claim could be submitted immediately after this,
            // with the original saved policy number.
            String policyNumber = rules.getPolicyNumber(object);
            if (!StringUtils.equals(policyNumber, savedPolicyNumber)) {
                if (!rules.canChangePolicyNumber(object)) {
                    throw new IllegalStateException("Cannot change policy number as claims have been submitted");
                }
            }
        }
        super.doSave();

        savedPolicyNumber = rules.getPolicyNumber(object);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PolicyLayoutStrategy();
    }

}
