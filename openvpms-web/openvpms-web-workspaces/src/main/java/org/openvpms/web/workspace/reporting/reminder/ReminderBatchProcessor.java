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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.processor.BatchProcessorComponent;


/**
 * Reminder batch processor.
 *
 * @author Tim Anderson
 */
public interface ReminderBatchProcessor extends BatchProcessorComponent {

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the reminder item archetype
     */
    String getArchetype();

    /**
     * Indicates if reminders are being resent.
     * <p>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being resent
     */
    void setResend(boolean resend);

    /**
     * Registers the statistics.
     *
     * @param statistics the statistics
     */
    void setStatistics(Statistics statistics);

    /**
     * Determines if there are more reminders available on completion of processing.
     *
     * @return {@code true} if there are more reminders available
     */
    boolean hasMoreReminders();

    /**
     * Returns the component.
     *
     * @return the component, or {@code null} if this doesn't render one
     */
    @Override
    Component getComponent();

}
