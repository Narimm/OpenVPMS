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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import org.openvpms.web.echo.factory.LabelFactory;

/**
 * Appointment series viewer.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesViewer {

    /**
     * The appointment series.
     */
    private final AppointmentSeries series;

    /**
     * Constructs an {@link AppointmentSeriesViewer}.
     *
     * @param series the series to view
     */
    public AppointmentSeriesViewer(AppointmentSeries series) {
        this.series = series;
    }

    /**
     * Returns a component to display the series
     *
     * @return a new component
     */
    public Component getComponent() {
        RepeatExpression expression = series.getExpression();
        if (expression == null) {
            return LabelFactory.create("workflow.scheduling.appointment.norepeat");
        } else if (expression instanceof CalendarRepeatExpression) {
            return new CalendarRepeatExpressionViewer((CalendarRepeatExpression) expression).getComponent();
        } else if (expression instanceof CronRepeatExpression) {
            CronRepeatExpression cron = (CronRepeatExpression) expression;
            if (RepeatOnDaysEditor.supports(cron)) {
                return new RepeatOnDaysViewer(cron).getComponent();
            }
        }
        return LabelFactory.create();
    }
}
