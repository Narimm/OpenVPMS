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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.practice;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;

import javax.annotation.PreDestroy;

/**
 * Practice service.
 *
 * @author Tim Anderson
 */
public class PracticeService {

    /**
     * The practice;
     */
    private Party practice;


    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The listener for practice updates.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs a {@link PracticeService}.
     *
     * @param rules the practice rules
     */
    public PracticeService(IArchetypeService service, PracticeRules rules) {
        this.service = service;
        practice = rules.getPractice();
        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                if (object.isActive() || practice == null || practice.getId() == object.getId()) {
                    practice = (Party) object;
                }
            }
        };
        service.addListener(PracticeArchetypes.PRACTICE, listener);
    }

    /**
     * Returns the practice.
     *
     * @return the practice, or {@code null} if there is no practice
     */
    public Party getPractice() {
        return practice;
    }

    /**
     * Disposes of the service.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
    }

}
