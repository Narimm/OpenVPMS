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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceArchetypes;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.internal.service.ClaimStatus;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * CRUD window for patient insurance policies.
 *
 * @author Tim Anderson
 */
public class InsuranceCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * Claim button identifier.
     */
    private static final String CLAIM_ID = "button.claim";

    /**
     * Submit button identifier.
     */
    private static final String SUBMIT_ID = "button.submit";

    /**
     * Constructs an {@link InsuranceCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public InsuranceCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(InsuranceArchetypes.POLICY, Act.class), InsuranceActions.INSTANCE, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(ButtonFactory.create(CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClaim();
            }
        }));
        buttons.add(ButtonFactory.create(SUBMIT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSubmit();
            }
        }));
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        Act object = getObject();
        buttons.setEnabled(CLAIM_ID, enable && TypeHelper.isA(object, InsuranceArchetypes.POLICY));
        buttons.setEnabled(SUBMIT_ID, enable && getActions().canSubmit(object));
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected InsuranceActions getActions() {
        return (InsuranceActions) super.getActions();
    }

    /**
     * Invoked when the 'Claim' button is pressed.
     */
    private void onClaim() {
        Act object = IMObjectHelper.reload(getObject());
        if (TypeHelper.isA(object, InsuranceArchetypes.POLICY)) {
            Act act = (Act) IMObjectCreator.create(InsuranceArchetypes.CLAIM);
            if (act != null) {
                ActBean bean = new ActBean(act);
                Act policy = getObject();
                bean.addNodeRelationship("policy", policy);
                edit(act, null);
            }
        }
    }

    /**
     * Invoked when the 'Submit' button is pressed.
     */
    private void onSubmit() {
        Act object = IMObjectHelper.reload(getObject());
        if (getActions().canSubmit(object)) {
            InsuranceFactory factory = ServiceHelper.getBean(InsuranceFactory.class);
            final Claim claim = factory.createClaim(object);
            InsuranceServices insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
            Party insurer = claim.getPolicy().getInsurer();
            final InsuranceService service = insuranceServices.getService(insurer);
            String title = Messages.get("patient.insurance.submit.title");
            if (service != null) {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.submit.online", service.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                submit(claim, service);
                            }
                        });
            } else {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.submit.offline", insurer.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                            }
                        });
            }
        }
    }

    private void submit(Claim claim, InsuranceService service) {
        service.submit(claim);
    }

    private static class InsuranceActions extends ActActions<Act> {

        public static final InsuranceActions INSTANCE = new InsuranceActions();

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code true} if the act isn't locked
         */
        @Override
        public boolean canDelete(Act act) {
            boolean result = super.canDelete(act);
            if (result) {
                if (TypeHelper.isA(act, InsuranceArchetypes.POLICY)) {
                    result = new ActBean(act).getValues("claims").isEmpty();
                } else if (TypeHelper.isA(act, InsuranceArchetypes.CLAIM)) {
                    result = ClaimStatus.PENDING.equals(act.getStatus());
                }
            }
            return result;
        }

        /**
         * Determines if an act is a claim that can be submitted.
         *
         * @param act the act
         * @return {@code true} if the act is a claim that can be submitted
         */
        public boolean canSubmit(Act act) {
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM) && ClaimStatus.POSTED.equals(act.getStatus());
        }
    }
}
