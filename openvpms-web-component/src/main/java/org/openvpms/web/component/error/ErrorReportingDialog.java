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
 */
package org.openvpms.web.component.error;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.context.ApplicationContext;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.LARGE_INSET;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;


/**
 * An {@link ErrorDialog} that provides the option to report errors back to OpenVPMS.
 *
 * @author Tim Anderson
 */
public class ErrorReportingDialog extends ErrorDialog {

    /**
     * The error reporter.
     */
    private ErrorReporter reporter;

    /**
     * The error report, or {@code null} if the exception isn't reportable.
     */
    private ErrorReport report;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ErrorReportingDialog.class);


    /**
     * Constructs an {@link ErrorReportingDialog}.
     *
     * @param message   the error message
     * @param exception the exception to display
     */
    public ErrorReportingDialog(String message, Throwable exception) {
        this(Messages.get("errordialog.title"), message, exception);
    }

    /**
     * Constructs an {@code ErrorReportingDialog}.
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
        final SendReportDialog dialog = new SendReportDialog();
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doReport(dialog.getEmail());
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
     *
     * @param email the reply-to email address. May be {@code null}
     */
    private void doReport(String email) {
        reporter.report(report, email);
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
         * Email field.
         */
        private final TextField email;

        /**
         * Determines if the email address should be submitted.
         */
        private final CheckBox includeEmail;

        /**
         * Constructs a {@link SendReportDialog}.
         */
        public SendReportDialog() {
            super(Messages.get("errorreportdialog.title"),
                  Messages.get("errorreportdialog.message"), new String[0]);
            email = TextComponentFactory.create(40);
            includeEmail = CheckBoxFactory.create("errorreportdialog.email", true);
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
         * Returns the email address.
         *
         * @return the email address. May be {@code null}
         */
        public String getEmail() {
            String result = null;
            if (includeEmail.isSelected()) {
                result = StringUtils.trimToNull(email.getText());
            }
            return result;
        }

        /**
         * Invoked when the OK button is pressed.
         * <p/>
         * Validates the email address, if any. If valid, closes the dialog.
         */
        @Override
        protected void onOK() {
            String email = getEmail();
            boolean valid = true;
            if (email != null) {
                try {
                    new InternetAddress(email, true);
                } catch (AddressException exception) {
                    ErrorDialog dialog = new ErrorDialog(Messages.format("errorreportdialog.invalidemail", email));
                    dialog.show();
                    valid = false;
                }
            }
            if (valid) {
                super.onOK();
            }
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create(true);
            message.setText(getMessage());
            Button show = ButtonFactory.create("errorreportdialog.showlink", "hyperlink", new ActionListener() {
                public void onAction(ActionEvent e) {
                    showReport();
                }
            });
            email.setStyleName(Styles.EDIT);
            ContextApplicationInstance app = ContextApplicationInstance.getInstance();
            if (app != null) {
                Party practice = app.getContext().getPractice();
                if (practice != null) {
                    PartyRules rules = ServiceHelper.getBean(CustomerRules.class);
                    email.setText(rules.getEmailAddress(practice));
                }
            }
            includeEmail.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    email.setEnabled(includeEmail.isSelected());
                }
            });

            Label content = LabelFactory.create("errorreportdialog.show");
            Column column = ColumnFactory.create(WIDE_CELL_SPACING, message,
                                                 RowFactory.create(CELL_SPACING, includeEmail, email),
                                                 RowFactory.create(CELL_SPACING, content, show));
            getLayout().add(ColumnFactory.create(LARGE_INSET, column));
        }

    }

}
