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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentQueryHelper {


    public static NamedQuery create(Party schedule,
                                    Date from, Date to) {
        Collection<String> names = Arrays.asList("act.objectReference",
                                                 "act.startTime", "act.endTime",
                                                 "act.status", "act.reason",
                                                 "act.description",
                                                 "entity.objectReference",
                                                 "entity.name");
        NamedQuery query = new NamedQuery("act.customerAppointment", names);
        query.setParameter("scheduleId", schedule.getLinkId());
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query;
    }

    public static IPage<ObjectSet> query(Party schedule, Date from, Date to) {
        IArchetypeQuery query = create(schedule, from, to);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IPage<ObjectSet> page = service.getObjects(query);
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        IMObjectReference currentAct = null;
        ObjectSet current = null;
        for (ObjectSet set : page.getResults()) {
            IMObjectReference actRef = (IMObjectReference) set.get(
                    "act.objectReference");
            if (currentAct == null || !currentAct.equals(actRef)) {
                if (current != null) {
                    result.add(current);
                }
                currentAct = actRef;
                current = new ObjectSet();
                current.add("act.objectReference", actRef);
                current.add("act.startTime", set.get("act.startTime"));
                current.add("act.endTime", set.get("act.endTime"));
                current.add("act.status", set.get("act.status"));
                current.add("act.reason", set.get("act.reason"));
                current.add("act.description", set.get("act.description"));
            }
            IMObjectReference entityRef
                    = (IMObjectReference) set.get("entity.objectReference");
            String entityName = (String) set.get("entity.name");
            if (TypeHelper.isA(entityRef, "party.customer*")) {
                current.add("customer.objectReference", entityRef);
                current.add("customer.name", entityName);
            } else if (TypeHelper.isA(entityRef, "party.patient*")) {
                current.add("patient.objectReference", entityRef);
                current.add("patient.name", entityName);
            } else if (TypeHelper.isA(entityRef, "entity.appointmentType")) {
                current.add("appointmentType.objectReference", entityRef);
                current.add("appointmentType.name", entityName);
            } else if (TypeHelper.isA(entityRef, "security.user")) {
                current.add("clinician.objectReference", entityRef);
                current.add("clinician.name", entityName);
            }
        }
        if (current != null) {
            result.add(current);
        }
        return new Page<ObjectSet>(result, 0, result.size(), result.size());
    }
}
