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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.i18n.time.LookupDateDurationFormatter;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.lookup.ILookupService;

import javax.annotation.PreDestroy;

/**
 * An {@link PatientAgeFormatter} that refreshes its internal state when the configuration updates.
 *
 * @author Tim Anderson
 */
public class RefreshablePatientAgeFormatter extends PatientAgeFormatter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Listener for archetype service events.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs a {@link RefreshablePatientAgeFormatter}.
     *
     * @param lookups the lookup service
     * @param rules   the practice rules, used to determine the practice
     * @param service the archetype service
     */
    public RefreshablePatientAgeFormatter(ILookupService lookups, PracticeRules rules, IArchetypeService service) {
        super(lookups, rules, service);
        this.service = service;
        listener = new AbstractArchetypeServiceListener() {
            public void saved(IMObject object) {
                refresh();
            }

            public void removed(IMObject object) {
                refresh();
            }
        };
        service.addListener(PracticeArchetypes.PRACTICE, listener);
        service.addListener(LookupDateDurationFormatter.DURATION_FORMATS, listener);
        service.addListener(LookupDateDurationFormatter.DURATION_FORMAT, listener);
    }

    /**
     * Disposes of this formatter.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
        service.removeListener(LookupDateDurationFormatter.DURATION_FORMATS, listener);
        service.removeListener(LookupDateDurationFormatter.DURATION_FORMAT, listener);
    }

}
