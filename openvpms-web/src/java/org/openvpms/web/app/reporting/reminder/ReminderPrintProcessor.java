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

package org.openvpms.web.app.reporting.reminder;

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
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Prints reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPrintProcessor extends ProgressBarProcessor<ReminderEvent> {

    /**
     * The name of the selected printer. Once a printer has been selected,
     * printing will occur in the background.
     */
    private String printerName;


    /**
     * Constructs a new <tt>ReminderPrintProcessor</tt>.
     *
     * @param reminders the reminders to print
     */
    public ReminderPrintProcessor(List<ReminderEvent> reminders) {
        super(reminders, Messages.get("reporting.reminder.run.print"));
    }

    /**
     * Invoked to process a reminder.
     *
     * @param event the event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the event cannot be processed
     */
    protected void process(final ReminderEvent event) {
        setSuspend(true);
        Entity documentTemplate = event.getDocumentTemplate();
        IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(
                event.getReminder(), documentTemplate);
        final InteractiveIMPrinter<Act> iPrinter
                = new InteractiveIMPrinter<Act>(printer);
        if (printerName != null) {
            iPrinter.setInteractive(false);
        }
        iPrinter.setListener(new PrinterListener() {
            public void printed(String printer) {
                try {
                    setSuspend(false);
                    processCompleted(event);
                    printerName = printer;
                } catch (OpenVPMSException exception) {
                    notifyError(exception);
                }
            }

            public void cancelled() {
                notifyCompleted();
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                notifyError(cause);
            }
        });

        if (iPrinter.getInteractive()) {
            setSuspend(true); // suspend generation while printing
        }
        iPrinter.print(printerName);
    }

}
