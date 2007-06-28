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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.reminder;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.patient.reminder.AbstractReminderProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Prints reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPrintProcessor extends AbstractReminderProcessorListener {

    /**
     * The generator.
     */
    private final ReminderGenerator generator;


    /**
     * Constructs a new <tt>ReminderPrintProcessor</tt>.
     *
     * @param generator the reminder generator
     */
    public ReminderPrintProcessor(ReminderGenerator generator) {
        this.generator = generator;
    }

    /**
     * Invoked to process a reminder.
     *
     * @param event the event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the event cannot be processed
     */
    public void process(final ReminderEvent event) {
        generator.setSuspend(true);
        Entity documentTemplate = event.getDocumentTemplate();
        IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(
                event.getReminder(), documentTemplate);
        InteractiveIMPrinter<Act> iPrinter
                = new InteractiveIMPrinter<Act>(printer);
        iPrinter.setListener(new PrinterListener() {
            public void printed() {
                try {
                    update(event.getReminder());
                    generator.process();
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                }
            }

            public void cancelled() {
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                ErrorHelper.show(cause, new WindowPaneListener() {
                    public void windowPaneClosing(
                            WindowPaneEvent event) {
                    }
                });
            }
        });
        iPrinter.print();
    }

}
