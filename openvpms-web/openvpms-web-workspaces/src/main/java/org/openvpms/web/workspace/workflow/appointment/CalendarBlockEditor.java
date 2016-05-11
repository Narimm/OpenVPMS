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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarBlockSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;

import java.util.Date;

/**
 * An editor for <em>act.calendarBlock</em> acts.
 *
 * @author Tim Anderson
 */
public class CalendarBlockEditor extends CalendarEventEditor {

    /**
     * Constructs a {@link CalendarBlockEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public CalendarBlockEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, false, context);
    }

    /**
     * Constructs a {@link CalendarBlockEditor}.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be {@code null}
     * @param editSeries if {@code true}, edit the series
     * @param context    the layout context
     */
    public CalendarBlockEditor(Act act, IMObject parent, boolean editSeries, LayoutContext context) {
        super(act, parent, editSeries, context);
        addStartEndTimeListeners();
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        boolean editSeries = getSeriesEditor() != null;
        return new CalendarBlockEditor(reload(getObject()), getParent(), editSeries, getLayoutContext());
    }

    /**
     * Creates a new event series.
     *
     * @return a new event series
     */
    @Override
    protected CalendarEventSeries createSeries() {
        return new CalendarBlockSeries(getObject(), ServiceHelper.getArchetypeService());
    }

    /**
     * Calculates the end time.
     */
    @Override
    protected void calculateEndTime() {
        Date start = getStartTime();
        int slotSize = getSlotSize();
        if (start != null && slotSize != 0) {
            Date end = DateRules.getDate(start, slotSize, DateUnits.MINUTES);
            setEndTime(end);
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

}
