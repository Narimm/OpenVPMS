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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.resource.util.Messages;

import java.util.List;
import java.util.ArrayList;


/**
 * Prints reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPrintProcessor extends ReminderProgressBarProcessor {

    /**
     * The name of the selected printer. Once a printer has been selected,
     * printing will occur in the background.
     */
    private String printerName;


    /**
     * Constructs a new <tt>ReminderPrintProcessor</tt>.
     *
     * @param reminders     the reminders to print
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     */
    public ReminderPrintProcessor(List<List<ReminderEvent>> reminders, Entity groupTemplate,
                                  Statistics statistics) {
        super(reminders, groupTemplate, statistics, Messages.get("reporting.reminder.run.print"));
    }

    @Override
    protected void process(List<ReminderEvent> events, String shortName, Entity documentTemplate) {
        setSuspend(true);
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(sets, shortName, documentTemplate);
            print(events, printer);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(acts, shortName, documentTemplate);
            print(events, printer);
        }
    }

    private <T> void print(final List<ReminderEvent> events, IMPrinter<T> printer) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer);
        if (printerName != null) {
            iPrinter.setInteractive(false);
        }
        iPrinter.setListener(new PrinterListener() {
            public void printed(String printer) {
                try {
                    setSuspend(false);
                    processCompleted(events);
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
