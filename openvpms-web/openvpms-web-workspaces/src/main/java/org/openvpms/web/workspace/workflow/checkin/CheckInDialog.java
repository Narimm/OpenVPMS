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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Date;

import static org.openvpms.web.component.property.ValidationHelper.showError;

/**
 * Patient Check-In dialog.
 *
 * @author Tim Anderson
 */
public class CheckInDialog extends ModalDialog {

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The editor.
     */
    private CheckInEditor editor;

    /**
     * Constructs a {@link CheckInDialog}.
     *
     * @param customer    the customer
     * @param patient     the patient. May be {@code null}
     * @param schedule    the appointment schedule. May be {@code null}
     * @param clinician   the clinician to assign to new events. May be {@code null}
     * @param location    the practice location
     * @param arrivalTime the arrival time
     * @param appointment the appointment. May be {@code null}
     * @param user        the author for acts
     * @param help        the help context
     */
    public CheckInDialog(Party customer, Party patient, Entity schedule, User clinician, Party location,
                         Date arrivalTime, Act appointment, User user, HelpContext help) {
        super(Messages.get("patient.checkin.title"), "CheckInDialog", OK_CANCEL, help);
        transactionManager = ServiceHelper.getBean(PlatformTransactionManager.class);
        editor = new CheckInEditor(customer, patient, schedule, clinician, location, arrivalTime, appointment, user,
                                   help);
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        editor.showAlerts();
    }

    /**
     * Returns the patient.
     *
     * @return the patient. Non-null if the dialog has saved
     */
    public Party getPatient() {
        return editor.getPatient();
    }

    /**
     * Returns the event.
     *
     * @return the event. Non-null if the dialog has saved
     */
    public Act getEvent() {
        return editor.getVisit();
    }

    /**
     * Returns the clinician.
     *
     * @return the clinician. May be {@code null}
     */
    public User getClinician() {
        return editor.getClinician();
    }

    /**
     * Returns flow sheet details, if a flow sheet is being created.
     *
     * @return the flow sheet detail, or {@code null} if no flow sheet is being created
     */
    public FlowSheetInfo getFlowSheetInfo() {
        return editor.getFlowSheetInfo();
    }

    /**
     * Returns the templates to print.
     *
     * @return the templates to print
     */
    public Collection<Entity> getTemplates() {
        return editor.getTemplates();
    }

    /**
     * Returns the weight act created by Check-In.
     *
     * @return the weight, or {@code null} if none was saved
     */
    public Act getWeight() {
        return editor.getWeight();
    }

    /**
     * Returns the task created by Check-In.
     *
     * @return the task, or {@code null} if none was saved
     */
    public Act getTask() {
        return editor.getTask();
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    CheckInEditor getEditor() {
        return editor;
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        Validator validator = new DefaultValidator();
        if (editor.validate(validator)) {
            if (save()) {
                super.onOK();
            }
        } else {
            showError(validator);
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        FocusGroup focus = getFocusGroup();
        getLayout().add(ColumnFactory.create(Styles.INSET, editor.getComponent()));
        focus.add(editor.getFocusGroup());
    }

    /**
     * Saves the editor.
     * <p/>
     * If the save fails, the OK button will be disabled
     */
    private boolean save() {
        boolean saved = false;
        try {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    editor.save();
                }
            });
            saved = true;
        } catch (Throwable exception) {
            getButtons().setEnabled(OK_ID, false);
            ErrorHelper.show(exception);
        }
        return saved;
    }

}
