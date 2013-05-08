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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.context.ApplicationContext;


/**
 * An {@link ErrorDialog} that provides the option to report errors back to OpenVPMS.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ErrorReportingDialog extends ErrorDialog {

    /**
     * The error reporter.
     */
    private ErrorReporter reporter;

    /**
     * The error report, or <tt>null</tt> if the exception isn't reportable.
     */
    private ErrorReport report;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ErrorReportingDialog.class);


    /**
     * Constructs an <tt>ErrorReportingDialog</tt>.
     *
     * @param message   the error message
     * @param exception the exception to display
     */
    public ErrorReportingDialog(String message, Throwable exception) {
        this(Messages.get("errordialog.title"), message, exception);
    }

    /**
     * Constructs an <tt>ErrorReportingDialog</tt>.
     *
     * @param title     the dialog title
     * @param message   the error message
     * @param exception the exception to display
     */
    public ErrorReportingDialog(String title, String message, Throwable exception) {
        super(title, message, OK);
        ApplicationContext context = ServiceHelper.getContext();
        if (context.containsBean("errorReporter")) {
            reporter = (ErrorReporter) context.getBean("errorReporter");
            if (reporter.isReportable(exception)) {
                report = new ErrorReport(message, exception);
                addButton("errorreportdialog.report", new ActionListener() {
                    public void onAction(ActionEvent e) {
                        reportError();
                    }
                });
            }
        }
    }

    /**
     * Helper to show a new error reporting dialog.
     *
     * @param title     the dialog title
     * @param message   dialog message
     * @param exception the cause
     */
    public static void show(String title, String message, Throwable exception) {
        ErrorDialog dialog = new ErrorReportingDialog(title, message, exception);
        dialog.show();
    }

    /**
     * Helper to show a new error reporting dialog.
     *
     * @param message   dialog message
     * @param exception the cause
     */
    public static void show(String message, Throwable exception) {
        ErrorDialog dialog = new ErrorReportingDialog(message, exception);
        dialog.show();
    }

    /**
     * Pops up a dialog to report the error.
     */
    private void reportError() {
        SendReportDialog dialog = new SendReportDialog();
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doReport();
            }

            @Override
            public void onCancel() {
                ErrorReportingDialog.this.onCancel();
            }
        });
        dialog.show();
    }

    /**
     * Sends the report and closes the dialog.
     */
    private void doReport() {
        reporter.report(report);
        onOK();
    }

    /**
     * Shows the report in a new dialog.
     */
    private void showReport() {
        try {
            String xml = report.toXML();
            InformationDialog.show(Messages.get("errorreportdialog.showtitle"), xml);
        } catch (Throwable exception) {
            log.error(exception, exception);
            ErrorDialog.show(exception);
        }
    }

    /**
     * Confirmation dialog that prompts to send the report.
     */
    private class SendReportDialog extends ConfirmationDialog {

        /**
         * Constructs a new <tt>SendReportDialog</tt>.
         */
        public SendReportDialog() {
            super(Messages.get("errorreportdialog.title"),
                  Messages.get("errorreportdialog.message"), new String[0]);
            addButton("errorreportdialog.send", new ActionListener() {
                public void onAction(ActionEvent e) {
                    onOK();
                }
            });
            addButton("errorreportdialog.nosend", new ActionListener() {
                public void onAction(ActionEvent e) {
                    onCancel();
                }
            });
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create();
            message.setText(getMessage());
            Button show = ButtonFactory.create("errorreportdialog.showlink", "hyperlink", new ActionListener() {
                public void onAction(ActionEvent e) {
                    showReport();
                }
            });
            Label content = LabelFactory.create("errorreportdialog.show");
            Column column = ColumnFactory.create(
                "Inset", ColumnFactory.create("CellSpacing", message,
                                              RowFactory.create("CellSpacing", content, show)));
            getLayout().add(column);
        }

    }

}
