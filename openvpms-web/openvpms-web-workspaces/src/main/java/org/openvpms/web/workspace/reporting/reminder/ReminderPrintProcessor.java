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
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;

import java.util.ArrayList;
import java.util.List;


/**
 * Prints reminders.
 *
 * @author Tim Anderson
 */
class ReminderPrintProcessor extends AbstractReminderProcessor {

    /**
     * Determines if a print dialog is being displayed.
     */
    private boolean interactive;

    /**
     * Determines if the print dialog should always be displayed.
     */
    private boolean alwaysInteractive;

    /**
     * The printer to fallback to, if none is specified by the document templates. This is selected once.
     */
    private String fallbackPrinter;

    /**
     * The listener for printer events.
     */
    private final PrinterListener listener;

    /**
     * The mail context, used when printing interactively. May be {@code null}
     */
    private final MailContext mailContext;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@code ReminderPrintProcessor}.
     *
     * @param groupTemplate the grouped reminder document template
     * @param listener      the listener for printer events
     * @param context       the context
     * @param mailContext   the mail context, used when printing interactively. May be {@code null}
     * @param help          the help context
     */
    public ReminderPrintProcessor(DocumentTemplate groupTemplate, PrinterListener listener, Context context,
                                  MailContext mailContext, HelpContext help) {
        super(groupTemplate, context);
        this.listener = listener;
        this.mailContext = mailContext;
        this.help = help;
    }

    /**
     * Determines if reminders are being printed interactively, or in the background.
     *
     * @return {@code true} if reminders are being printed interactively, or {@code false} if they are being
     *         printed in the background
     */
    public boolean isInteractive() {
        return alwaysInteractive || interactive;
    }

    /**
     * Determines if reminders should always be printed interactively.
     *
     * @param interactive if {@code true}, reminders should always be printed interactively. If {@code false},
     *                    reminders will only be printed interactively if a printer needs to be selected
     */
    public void setInteractiveAlways(boolean interactive) {
        alwaysInteractive = interactive;
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events    the events
     * @param shortName the report archetype short name, used to select the document template if none specified
     * @param template  the document template to use. May be {@code null}
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate template) {
        Context context = getContext();
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(template, shortName, context);
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(sets, locator, context);
            print(printer);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(acts, locator, context);
            print(printer);
        }
    }

    /**
     * Performs a print.
     * <p/>
     * If a printer is configured, the print will occur in the background, otherwise a print dialog will be popped up.
     *
     * @param printer the printer
     */
    private <T> void print(IMPrinter<T> printer) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer, getContext(), help);
        String printerName = printer.getDefaultPrinter();
        if (printerName == null) {
            printerName = fallbackPrinter;
        }
        interactive = alwaysInteractive || printerName == null;
        iPrinter.setInteractive(interactive);
        iPrinter.setMailContext(mailContext);

        if (interactive) {
            // register a listener to grab the selected printer, to avoid popping up a print dialog each time
            PrinterListener l = new DelegatingPrinterListener(listener) {
                public void printed(String printer) {
                    fallbackPrinter = printer;
                    super.printed(printer);
                }
            };
            iPrinter.setListener(l);
        } else {
            iPrinter.setListener(listener);
        }
        iPrinter.print(printerName);
    }

    private static class DelegatingPrinterListener implements PrinterListener {

        /**
         * The listener to delegate to.
         */
        private final PrinterListener listener;

        /**
         * Creates a new {@code DelegatingPrinterListener}.
         *
         * @param listener the listener to delegate to
         */
        public DelegatingPrinterListener(PrinterListener listener) {
            this.listener = listener;
        }

        /**
         * Notifies of a successful print.
         *
         * @param printer the printer that was used. May be {@code null}
         */
        public void printed(String printer) {
            listener.printed(printer);
        }

        /**
         * Notifies that the print was cancelled.
         */
        public void cancelled() {
            listener.cancelled();
        }

        /**
         * Notifies that the print was skipped.
         */
        public void skipped() {
            listener.skipped();
        }

        /**
         * Invoked when a print fails.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            listener.failed(cause);
        }
    }
}
