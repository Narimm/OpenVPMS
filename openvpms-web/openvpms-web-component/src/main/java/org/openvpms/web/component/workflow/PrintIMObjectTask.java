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

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Prints an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class PrintIMObjectTask extends AbstractTask {

    /**
     * The print mode for documents.
     */
    public enum PrintMode {
        INTERACTIVE, // print interactively
        BACKGROUND,  // print in the background, unless user intervention is required
        DEFAULT      // use the settings from the template
    }

    /**
     * The object to print.
     */
    private IMObject object;

    /**
     * The short name of the object to print.
     */
    private String shortName;

    /**
     * The mail context. May be {@code null}
     */
    private final MailContext mailContext;

    /**
     * Determines how objects should be printed.
     */
    private final PrintMode printMode;

    /**
     * Determines if the skip button should be displayed if
     * {@link #isRequired()} is {@code false}.
     */
    private boolean enableSkip = true;


    /**
     * Constructs a {@link PrintIMObjectTask}.
     *
     * @param object    the object to print
     * @param context   the mail context. May be {@code null}
     * @param printMode the print mode
     */
    public PrintIMObjectTask(IMObject object, MailContext context, PrintMode printMode) {
        this.object = object;
        this.mailContext = context;
        this.printMode = printMode;
    }

    /**
     * Constructs a {@link PrintIMObjectTask}.
     *
     * @param shortName the short name of the object to print
     * @param context   the mail context. May be {@code null}
     */
    public PrintIMObjectTask(String shortName, MailContext context) {
        this(shortName, context, true);
    }

    /**
     * Constructs a {@link PrintIMObjectTask}.
     *
     * @param shortName   the short name of the object to print
     * @param context     the mail context. May be {@code null}
     * @param interactive if {@code true} print interactively, otherwise attempt to print in the background
     */
    public PrintIMObjectTask(String shortName, MailContext context, boolean interactive) {
        this(shortName, context, interactive ? PrintMode.INTERACTIVE : PrintMode.BACKGROUND);
    }

    /**
     * Constructs a {@link PrintIMObjectTask}.
     *
     * @param shortName the short name of the object to print
     * @param context   the mail context. May be {@code null}
     * @param printMode the print mode
     */
    public PrintIMObjectTask(String shortName, MailContext context, PrintMode printMode) {
        this.shortName = shortName;
        this.mailContext = context;
        this.printMode = printMode;
    }

    /**
     * Determines if printing may be skipped. This only applies when the task
     * is not required. Defaults to {@code true}.
     *
     * @param skip if {@code true} and the task is not required, displays a
     *             skip button to skip printing.
     */
    public void setEnableSkip(boolean skip) {
        enableSkip = skip;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        IMObject object = getObject(context);
        if (object == null) {
            notifyCancelled();
        } else {
            print(object, context);
        }
    }

    /**
     * Prints an object.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param object  the object to print
     * @param context the task context
     */
    protected void print(final IMObject object, final TaskContext context) {
        try {
            ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
            IMPrinter<IMObject> printer = IMPrinterFactory.create(object, locator, context);
            boolean skip = !isRequired() && enableSkip;
            HelpContext help = context.getHelpContext().topic(object, "print");
            InteractiveIMPrinter<IMObject> iPrinter = createPrinter(printer, skip, context, help);
            if (printMode == PrintMode.INTERACTIVE) {
                iPrinter.setInteractive(true);
            } else if (printMode == PrintMode.BACKGROUND) {
                iPrinter.setInteractive(false);
            }
            iPrinter.setMailContext(mailContext);

            iPrinter.setListener(new PrinterListener() {
                public void printed(String printer) {
                    onPrinted(object, context);
                }

                public void cancelled() {
                    notifyCancelled();
                }

                public void skipped() {
                    notifySkipped();
                }

                public void failed(Throwable cause) {
                    notifyCancelledOnError(cause);
                }
            });
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            notifyCancelledOnError(exception);
        }
    }

    /**
     * Creates an interactive printer.
     *
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    protected InteractiveIMPrinter<IMObject> createPrinter(IMPrinter<IMObject> printer, boolean skip,
                                                           TaskContext context, HelpContext help) {
        return new InteractiveIMPrinter<IMObject>(printer, skip, context, help);
    }

    /**
     * Returns the object.
     *
     * @param context the context
     * @return the object. May be {@code null}
     */
    protected IMObject getObject(TaskContext context) {
        if (object == null) {
            object = context.getObject(shortName);
        }
        return object;
    }

    /**
     * Invoked when the object is successfully printed.
     * Notifies completion of the task.
     *
     * @param object  the printed object
     * @param context the task context
     */
    protected void onPrinted(IMObject object, TaskContext context) {
        notifyCompleted();
    }

}
