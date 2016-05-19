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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

/**
 * Calendar block series.
 *
 * @author Tim Anderson
 */
public class CalendarBlockSeries extends CalendarEventSeries {

    /**
     * Constructs an {@link CalendarBlockSeries}.
     *
     * @param event   the event. An appointment or calendar block
     * @param service the archetype service
     */
    public CalendarBlockSeries(Act event, IArchetypeService service) {
        super(event, service);
    }

    /**
     * Determines if the series can be calculated.
     *
     * @param state the current event state
     * @return {@code true} if the series can be calculated
     */
    @Override
    protected boolean canCalculateSeries(State state) {
        return super.canCalculateSeries(state) && ((BlockState) state).getBlockType() != null;
    }

    /**
     * Creates state from an act.
     *
     * @param bean the act bean
     * @return a new state
     */
    @Override
    protected State createState(ActBean bean) {
        return new BlockState(bean);
    }

    /**
     * Copies state.
     *
     * @param state the state to copy
     * @return a copy of {@code state}
     */
    @Override
    protected State copy(State state) {
        return new BlockState((BlockState) state);
    }

    /**
     * Populates an event from state.
     *
     * @param bean  the event bean
     * @param state the state
     */
    @Override
    protected void populate(ActBean bean, State state) {
        super.populate(bean, state);
        BlockState block = (BlockState) state;
        bean.setNodeParticipant("type", block.getBlockType());
    }

    private static class BlockState extends State {

        /**
         * The block type reference.
         */
        private IMObjectReference blockType;

        /**
         * Initialises the state from an event.
         *
         * @param event the event
         */
        public BlockState(ActBean event) {
            super(event);
        }

        /**
         * Copy constructor.
         *
         * @param state the state to copy
         */
        public BlockState(BlockState state) {
            super(state);
            blockType = state.blockType;
        }

        /**
         * Updates the state from an event.
         *
         * @param event the event
         */
        @Override
        public void update(ActBean event) {
            super.update(event);
            blockType = event.getNodeParticipantRef("type");
        }

        /**
         * Returns the block type.
         *
         * @return the block type
         */
        public IMObjectReference getBlockType() {
            return blockType;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         */
        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if (obj instanceof BlockState && super.equals(obj)) {
                BlockState other = (BlockState) obj;
                result = ObjectUtils.equals(blockType, other.blockType);
            }
            return result;
        }

    }
}