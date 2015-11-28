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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

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
     * The practice rules.
     */
    private final PracticeRules rules;

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
        this.rules = rules;
        practice = rules.getPractice();
        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                update((Party) object);
            }

        };
        service.addListener(PracticeArchetypes.PRACTICE, listener);
    }

    /**
     * Returns the practice.
     *
     * @return the practice, or {@code null} if there is no practice
     */
    public synchronized Party getPractice() {
        return practice;
    }

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    public List<Party> getLocations() {
        Party current = getPractice();
        return current != null ? rules.getLocations(current) : Collections.<Party>emptyList();
    }

    /**
     * Returns the practice mail server.
     *
     * @return the mail server, or {@code null} if none is configured
     */
    public MailServer getMailServer() {
        MailServer result = null;
        EntityBean bean = getBean();
        if (bean != null) {
            Entity entity = bean.getNodeTargetEntity("mailServer");
            if (entity != null) {
                result = new MailServer(entity, service);
            }
        }
        return result;
    }

    /**
     * Returns the SMS configuration.
     *
     * @return the SMS configuration
     */
    public Entity getSMS() {
        EntityBean bean = getBean();
        return (bean != null) ? bean.getNodeTargetEntity("SMS") : null;
    }

    /**
     * Returns the default user to be used by background services.
     *
     * @return the service user. May be {@code null}
     */
    public User getServiceUser() {
        Party current = getPractice();
        return (current != null) ? rules.getServiceUser(current) : null;
    }

    /**
     * Returns the SMS appointment template configured for the practice.
     *
     * @return the template or {@code null} if none is configured
     */
    public Entity getAppointmentSMSTemplate() {
        EntityBean bean = getBean();
        return (bean != null) ? bean.getNodeTargetEntity("smsAppointment") : null;
    }

    /**
     * Disposes of the service.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
    }

    /**
     * Updates the practice.
     *
     * @param object the new practice
     */
    protected synchronized void update(Party object) {
        if (object.isActive() || practice == null || practice.getId() == object.getId()) {
            practice = object;
        }
    }

    /**
     * Returns the practice wrapped in a bean.
     *
     * @return the bean, or {@code null} if there is no current practice
     */
    protected synchronized EntityBean getBean() {
        return (practice != null) ? new EntityBean(practice, service) : null;
    }

}
