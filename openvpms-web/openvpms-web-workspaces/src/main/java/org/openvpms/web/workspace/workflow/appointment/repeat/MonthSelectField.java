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

import nextapp.echo2.app.list.ListModel;
import org.openvpms.web.component.bound.BoundSelectField;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.style.Styles;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Field to select a month.
 *
 * @author Tim Anderson
 */
class MonthSelectField extends BoundSelectField {

    /**
     * Constructs a {@link MonthSelectField}.
     *
     * @param property the property to bind
     */
    public MonthSelectField(Property property) {
        super(property, createListModel());
        setStyleName(Styles.DEFAULT);
        setCellRenderer(PairListModel.RENDERER);
    }

    /**
     * Creates a list model containing the months.
     *
     * @return a new list model
     */
    private static ListModel createListModel() {
        PairListModel model = new PairListModel();
        String[] months = DateFormatSymbols.getInstance().getMonths();
        for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; ++i) {
            model.add(i + 1, months[i]); // cron months go from 1-12
        }
        return model;
    }
}
