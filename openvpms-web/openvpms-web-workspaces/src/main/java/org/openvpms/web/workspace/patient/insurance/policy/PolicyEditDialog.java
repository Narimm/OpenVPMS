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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.policy.Policy;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.insurance.service.PolicyValidationStatus;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Edit dialog for <em>act.patientInsurancePolicy</em>.
 *
 * @author Tim Anderson
 */
public class PolicyEditDialog extends EditDialog {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PolicyEditDialog.class);

    /**
     * Check policy identifier.
     */
    private static final String CHECK_ID = "button.checkpolicy";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {APPLY_ID, OK_ID, CANCEL_ID, CHECK_ID};

    /**
     * Constructs a {@link PolicyEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public PolicyEditDialog(IMObjectEditor editor, Context context) {
        super(editor, BUTTONS, true, context);
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public PolicyEditor getEditor() {
        return (PolicyEditor) super.getEditor();
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (CHECK_ID.equals(button)) {
            onCheck();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Checks the policy against the insurer, if possible.
     */
    protected void onCheck() {
        if (save()) {
            PolicyEditor editor = getEditor();
            Act act = editor.getObject();
            InsuranceFactory factory = ServiceHelper.getBean(InsuranceFactory.class);
            Policy policy = factory.createPolicy(act);
            InsuranceServices services = ServiceHelper.getBean(InsuranceServices.class);
            Party insurer = policy.getInsurer();
            InsuranceService service = services.getService(insurer);
            String title = Messages.get("patient.insurance.policy.check.title");
            if (service != null) {
                try {
                    Context context = getContext();
                    Location location = factory.getLocation(context.getLocation());
                    PolicyValidationStatus status = service.validate(policy, location);
                    switch (status.getStatus()) {
                        case VALID:
                            InformationDialog.show(title, Messages.format("patient.insurance.policy.check.valid"));
                            break;
                        case INVALID:
                            ErrorDialog.show(title, Messages.format("patient.insurance.policy.check.invalid",
                                                                    status.getMessage()));
                            break;
                        case NOT_FOUND:
                            ErrorDialog.show(title, Messages.format("patient.insurance.policy.check.notfound",
                                                                    insurer.getName()));
                            break;
                        case EXPIRED:
                            // back date the policy to expire it.
                            editor.setEndTime(DateRules.getYesterday());
                            ErrorDialog.show(title, Messages.format("patient.insurance.policy.check.expired",
                                                                    insurer.getName()));
                            break;
                        case CHANGE_POLICY_NUMBER:
                            if (ServiceHelper.getBean(InsuranceRules.class).canChangePolicyNumber(act)) {
                                editor.setPolicyNumber(status.getPolicyNumber());
                                String message = Messages.format("patient.insurance.policy.check.numberupdated",
                                                                 insurer.getName());
                                InformationDialog.show(title, message);
                            } else {
                                // can't update the policy as it has claims against it. Need to create a new policy
                                String message = Messages.format("patient.insurance.policy.check.cannotupdate",
                                                                 insurer.getName(), status.getPolicyNumber());
                                InformationDialog.show(title, message);
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unsupported policy status: " + status.getStatus());
                    }
                } catch (Throwable exception) {
                    log.error("Failed to validate policy", exception);
                    ErrorDialog.show(title, Messages.format("patient.insurance.policy.check.error", getName(service),
                                                            exception.getMessage()));
                }
            } else {
                InformationDialog.show(title, Messages.format("patient.insurance.policy.check.unsupported",
                                                              insurer.getName()));
            }
        }
    }

    /**
     * Returns the name of an insurance service.
     *
     * @param service the service
     * @return the name
     */
    private String getName(InsuranceService service) {
        try {
            // the service can be stopped at any moment
            return service.getName();
        } catch (Throwable exception) {
            log.error("Failed to get insurance service name", exception);
            return "unknown";
        }
    }

}
