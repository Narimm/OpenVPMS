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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * A {@link TimeRange} selector.
 *
 * @author Tim Anderson
 */
public class TimeRangeSelector extends SelectField {

    /**
     * Constructs a {@link TimeRangeSelector}.
     */
    public TimeRangeSelector() {
        setStyleName(Styles.DEFAULT);
        // the order of the items must correspond to the order of TimeRange values.
        String[] items = {
                Messages.get("workflow.scheduling.time.all"),
                Messages.get("workflow.scheduling.time.morning"),
                Messages.get("workflow.scheduling.time.afternoon"),
                Messages.get("workflow.scheduling.time.evening"),
                Messages.get("workflow.scheduling.time.AM"),
                Messages.get("workflow.scheduling.time.PM")};
        setModel(new DefaultListModel(items));
        setSelected(TimeRange.ALL);
    }

    /**
     * Sets the selected time range.
     *
     * @param range the range
     */
    public void setSelected(TimeRange range) {
        int index = range.ordinal();
        if (index > getModel().size()) {
            index = 0;
        }
        setSelectedIndex(index);
    }

    /**
     * Returns the selected time range.
     *
     * @return the selected time range
     */
    public TimeRange getSelected() {
        int index = getSelectedIndex();
        return index >= 0 && index < TimeRange.values().length ? TimeRange.values()[index] : TimeRange.ALL;
    }
}
