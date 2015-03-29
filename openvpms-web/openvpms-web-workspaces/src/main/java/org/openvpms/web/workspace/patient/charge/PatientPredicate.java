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

package org.openvpms.web.workspace.patient.charge;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;

/**
 * A {@code Predicate} that evaluates true if the supplied object is an {@link Act} that has the same patient
 * as that specified.
 *
 * @author Tim Anderson
 */
public class PatientPredicate<T extends IMObject> implements Predicate<T> {

    /**
     * The patient.
     */
    private final IMObjectReference patient;

    /**
     * Constructs an {@link PatientPredicate}.
     *
     * @param patient the patient. May be {@code null}
     */
    public PatientPredicate(Party patient) {
        this.patient = (patient != null) ? patient.getObjectReference() : null;
    }

    /**
     * Use the specified parameter to perform a test that returns true or false.
     *
     * @param object the object to evaluate
     * @return true or false
     */
    @Override
    public boolean evaluate(T object) {
        boolean result = false;
        if (patient != null && object instanceof Act) {
            ActBean bean = new ActBean((Act) object);
            result = ObjectUtils.equals(patient, bean.getNodeParticipantRef("patient"));
        }
        return result;
    }
}
