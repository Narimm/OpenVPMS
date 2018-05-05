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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;
import java.util.List;

/**
 * Represents an <em>act.calendarBlock</em> with respect to a customer.
 *
 * @author Tim Anderson
 */
public class CalendarBlock {

    /**
     * The act.
     */
    private final Act act;

    /**
     * The block type.
     */
    private final IMObjectBean blockType;

    /**
     * Determines if the block is reserved.
     */
    private final boolean reserved;

    /**
     * Constructs a {@link CalendarBlock}.
     *
     * @param act       the block
     * @param blockType the block type
     * @param reserved  determines if the block is reserved
     * @param service   the archetype service
     */
    public CalendarBlock(Act act, Entity blockType, boolean reserved, IArchetypeService service) {
        this.act = act;
        this.blockType = new IMObjectBean(blockType, service);
        this.reserved = reserved;
    }

    /**
     * Returns the act reference.
     *
     * @return the act reference
     */
    public IMObjectReference getReference() {
        return act.getObjectReference();
    }

    /**
     * Returns the block type.
     *
     * @return the block type
     */
    public Entity getBlockType() {
        return (Entity) blockType.getObject();
    }

    /**
     * Returns the calendar block name.
     *
     * @return the calendar block name
     */
    public String getName() {
        return act.getName() != null ? act.getName() : getBlockType().getName();
    }

    /**
     * Returns the calendar block start time.
     *
     * @return the calendar block start time
     */
    public Date getStartTime() {
        return act.getActivityStartTime();
    }

    /**
     * Returns the calendar block end time.
     *
     * @return the calendar block end time
     */
    public Date getEndTime() {
        return act.getActivityEndTime();
    }

    /**
     * Returns the customer account types that the block is reserved for.
     *
     * @return the customer account types
     */
    public List<Lookup> getCustomerAccountTypes() {
        return blockType.getValues("customerAccountTypes", Lookup.class);
    }

    /**
     * Returns the customer types that the block is reserved for.
     *
     * @return the customer types
     */
    public List<Lookup> getCustomerTypes() {
        return blockType.getValues("customerTypes", Lookup.class);
    }

    /**
     * Determines if the block is reserved. If so, no appointment may be scheduled
     *
     * @return {@code true} if the block is reserved
     */
    public boolean isReserved() {
        return reserved;
    }

}
