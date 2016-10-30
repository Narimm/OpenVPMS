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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.practice;

import org.joda.time.Period;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.event.AsyncListeners;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.component.system.common.event.Listeners;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
     * Used to notify registered listeners of updates to the practice.
     */
    public static class Update {

        private final Party practice;
        private final String user;

        private Update(Party practice) {
            this.practice = practice;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            this.user = (authentication != null) ? authentication.getName() : null;
        }

        /**
         * Returns the practice.
         *
         * @return the practice
         */
        public Party getPractice() {
            return practice;
        }

        /**
         * Returns the login name of the user that updated the practice.
         *
         * @return the login name. May be {@code null}
         */
        public String getUser() {
            return user;
        }
    }

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
     * Listeners to notify when the practice updates.
     */
    private final Listeners<Update> listeners;

    /**
     * Constructs a {@link PracticeService}.
     *
     * @param service  the archetype service
     * @param rules    the practice rules
     * @param executor the executor to perform asynchronous update notification. If {@code null} no update notification
     *                 will take place
     */
    public PracticeService(IArchetypeService service, PracticeRules rules, ThreadPoolTaskExecutor executor) {
        this.service = service;
        this.rules = rules;
        practice = rules.getPractice();
        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                update((Party) object);
            }

        };
        if (executor != null) {
            listeners = new AsyncListeners<>(executor.getThreadPoolExecutor());
        } else {
            listeners = null;
        }
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
     * Returns the currency associated with a practice.
     *
     * @return the currency associated with a practice, or {@code null} if no practice is defined
     */
    public Currency getCurrency() {
        Party practice = getPractice();
        return (practice != null) ? rules.getCurrency(practice) : null;
    }

    /**
     * Returns the SMS configuration.
     *
     * @return the SMS configuration
     */
    public Entity getSMS() {
        EntityBean bean = getBean();
        return (bean != null) ? bean.getNodeTargetEntity("sms") : null;
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
     * Determines the period after which patient medical records are locked.
     *
     * @return the period, or {@code null} if no period is defined
     */
    public Period getRecordLockPeriod() {
        Party current = getPractice();
        return (current != null) ? rules.getRecordLockPeriod(current) : null;
    }

    /**
     * Returns the default field separator to use when exporting files.
     *
     * @return the field separator
     */
    public char getExportFileFieldSeparator() {
        Party current = getPractice();
        return (current != null) ? rules.getExportFileFieldSeparator(current) : ',';
    }

    /**
     * Disposes of the service.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
        if (listeners != null) {
            listeners.clear();
        }
    }

    /**
     * Adds a listener to be notified when the practice updates.
     *
     * @param listener the listener
     */
    public void addListener(Listener<Update> listener) {
        if (listeners != null) {
            listeners.addListener(listener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Listener<Update> listener) {
        if (listeners != null) {
            listeners.removeListener(listener);
        }
    }

    /**
     * Updates the practice.
     *
     * @param object the new practice
     */
    protected void update(Party object) {
        boolean updated = false;
        synchronized (this) {
            if (object.isActive() || practice == null || practice.getId() == object.getId()) {
                updated = true;
                practice = object;
            }
        }
        if (updated && listeners != null) {
            listeners.onEvent(new Update(object));
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
