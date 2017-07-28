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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

/**
 * An iterator over items returned queries created by {@link ReminderItemQueryFactory}.
 *
 * @author Tim Anderson
 */
public class PagedReminderItemIterator extends UpdatableQueryIterator<ObjectSet> {

    /**
     * Constructs a {@link PagedReminderItemIterator}.
     *
     * @param factory  the reminder item query factory
     * @param pageSize the page size
     * @param service  the archetype service
     */
    public PagedReminderItemIterator(ReminderItemQueryFactory factory, int pageSize, IArchetypeService service) {
        super(factory.createQuery(), pageSize, service);
    }

    /**
     * Returns the next page.
     *
     * @param query   the query
     * @param service the archetype service
     * @return the next page
     */
    @Override
    protected IPage<ObjectSet> getNext(ArchetypeQuery query, IArchetypeService service) {
        return service.getObjects(query);
    }

    /**
     * Returns a unique identifier for the object.
     *
     * @param object the object
     * @return a unique identifier for the object
     */
    @Override
    protected long getId(ObjectSet object) {
        Act item = (Act) object.get("item");
        return item.getId();
    }

}
