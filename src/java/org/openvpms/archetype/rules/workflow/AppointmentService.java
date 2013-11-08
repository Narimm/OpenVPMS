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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import net.sf.ehcache.Ehcache;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Implementation of the {@link ScheduleService} for appointments.
 *
 * @author Tim Anderson
 */
public class AppointmentService extends AbstractScheduleService {

    /**
     * Listener for appointment reason changes.
     */
    private final IArchetypeServiceListener listener;

    /**
     * Appointment reason archetype short name.
     */
    private static final String APPOINTMENT_REASON_SHORT_NAME = "lookup.appointmentReason";


    /**
     * Constructs an {@link AppointmentService}.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param cache         the cache
     */
    public AppointmentService(IArchetypeService service, ILookupService lookupService, Ehcache cache) {
        super(ScheduleArchetypes.APPOINTMENT, service, cache, new AppointmentFactory(service, lookupService));

        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                onReasonSaved((Lookup) object);
            }

            @Override
            public void removed(IMObject object) {
                onReasonRemoved((Lookup) object);
            }
        };
        service.addListener(APPOINTMENT_REASON_SHORT_NAME, listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        getService().removeListener(APPOINTMENT_REASON_SHORT_NAME, listener);
        super.destroy();
    }

    /**
     * Returns the event factory.
     *
     * @return the event factory
     */
    @Override
    protected AppointmentFactory getEventFactory() {
        return (AppointmentFactory) super.getEventFactory();
    }

    /**
     * Invoked when an appointment reason is saved. Updates the name cache and clears the appointment cache.
     *
     * @param reason the reason lookup
     */
    private void onReasonSaved(Lookup reason) {
        boolean updated = getEventFactory().addReason(reason);
        if (updated) {
            clearCache();
        }
    }

    /**
     * Invoked when an appointment reason is removed. Updates the name cache.
     * If the name is cached, then the appointment cache will be cleared.
     * <p/>
     * Strictly speaking, no lookup will be removed by the archetype service if it is use.
     *
     * @param reason the reason lookup
     */
    private void onReasonRemoved(Lookup reason) {
        if (getEventFactory().removeReason(reason)) {
            clearCache();
        }
    }

}
