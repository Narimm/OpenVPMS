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
public class ReminderPrintProcessor extends AbstractReminderProcessor {

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
    private PrinterListener listener;

    /**
     * The mail context, used when printing interactively. May be {@code null}
     */
    private final MailContext mailContext;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@link ReminderPrintProcessor}.
     *
     * @param groupTemplate the grouped reminder document template
     * @param context       the context
     * @param mailContext   the mail context, used when printing interactively. May be {@code null}
     * @param help          the help context
     */
    public ReminderPrintProcessor(DocumentTemplate groupTemplate, Context context, MailContext mailContext,
                                  HelpContext help) {
        super(groupTemplate, context);
        this.mailContext = mailContext;
        this.help = help;
    }

    /**
     * Registers a listener for printer events.
     * <p/>
     * This must be registered prior to processing any reminders.
     *
     * @param listener the listener
     */
    public void setListener(PrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Determines if reminders are being printed interactively, or in the background.
     *
     * @return {@code true} if reminders are being printed interactively, or {@code false} if they are being
     * printed in the background
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
            print(printer, events);
        } else {
            List<Act> acts = new ArrayList<>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMPrinter<Act> printer = new IMObjectReportPrinter<>(acts, locator, context);
            print(printer, events);
        }
    }

    /**
     * Invoked when reminders are printed.
     *
     * @param printer   the printer
     * @param reminders the printed reminders
     */
    protected void onPrinted(String printer, List<ReminderEvent> reminders) {
        if (fallbackPrinter == null) {
            fallbackPrinter = printer;
        }
        if (listener != null) {
            listener.printed(printer);
        }
    }

    /**
     * Invoked when printing is cancelled.
     *
     * @param reminders the cancelled reminders
     */
    protected void onPrintCancelled(List<ReminderEvent> reminders) {
        if (listener != null) {
            listener.cancelled();
        }
    }

    /**
     * Invoked when printing is skipped.
     *
     * @param reminders the skipped reminders
     */
    protected void onPrintSkipped(List<ReminderEvent> reminders) {
        if (listener != null) {
            listener.skipped();
        }
    }

    /**
     * Invoked when printing fails.
     *
     * @param reminders the reminders that failed to print
     */
    protected void onPrintFailed(List<ReminderEvent> reminders, Throwable cause) {
        if (listener != null) {
            listener.failed(cause);
        }
    }

    /**
     * Performs a print.
     * <p/>
     * If a printer is configured, the print will occur in the background, otherwise a print dialog will be popped up.
     *
     * @param printer the printer
     * @param events  the reminder events
     */
    private <T> void print(IMPrinter<T> printer, final List<ReminderEvent> events) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<>(printer, getContext(), help);
        String printerName = printer.getDefaultPrinter();
        if (printerName == null) {
            printerName = fallbackPrinter;
        }
        interactive = alwaysInteractive || printerName == null;
        iPrinter.setInteractive(interactive);
        iPrinter.setMailContext(mailContext);

        iPrinter.setListener(new PrinterListener() {
            @Override
            public void printed(String printer) {
                onPrinted(printer, events);
            }

            @Override
            public void cancelled() {
                onPrintCancelled(events);
            }

            @Override
            public void skipped() {
                onPrintSkipped(events);
            }

            @Override
            public void failed(Throwable cause) {
                onPrintFailed(events, cause);
            }
        });
        iPrinter.print(printerName);
    }

}
