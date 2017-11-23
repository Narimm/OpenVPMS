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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.identity.SingleIdentityCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractClaimEditor {

    /**
     * The attachments.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * The claim items.
     */
    private final ClaimItemCollectionEditor items;

    /**
     * The attachment generator.
     */
    private final AttachmentGenerator generator;

    /**
     * The insurerId node editor, for non-eClaims.
     */
    private SingleIdentityCollectionEditor insurerId;

    /**
     * Constructs an {@link ClaimEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public ClaimEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, "amount", context);

        ActBean claimBean = new ActBean(act);
        Act policy = (Act) claimBean.getNodeTargetObject("policy");
        if (policy == null) {
            throw new IllegalStateException("Claim has no policy");
        }
        ActBean policyBean = new ActBean(policy);
        Party customer = (Party) policyBean.getNodeParticipant("customer");
        if (customer == null) {
            throw new IllegalStateException("Policy has no customer");
        }

        if (act.isNew()) {
            initParticipant("patient", context.getContext().getPatient());
            initParticipant("location", context.getContext().getLocation());
            initParticipant("clinician", context.getContext().getClinician());
        }
        Party patient = getPatient();
        if (patient == null) {
            throw new IllegalStateException("Claim has no patient");
        }

        Editors editors = getEditors();
        if (!canSubmitClaim(act)) {
            // users can't submit the claim via an InsuranceService, so allow the insurerId to be edited, and make it
            // mandatory.
            CollectionProperty property = getCollectionProperty("insurerId");
            if (property.getValues().isEmpty()) {
                IMObject identity = IMObjectCreator.create(InsuranceArchetypes.CLAIM_IDENTITY);
                property.add(identity);
            }
            property = new DelegatingProperty(property) {
                @Override
                public int getMinCardinality() {
                    return 1;
                }
            };
            insurerId = new SingleIdentityCollectionEditor(property, act, context);
            editors.add(insurerId);
        }
        attachments = new AttachmentCollectionEditor(getCollectionProperty("attachments"), act, context);
        Charges charges = new Charges();
        items = new ClaimItemCollectionEditor(getCollectionProperty("items"), act, customer, patient,
                                              charges, attachments, context);
        editors.add(attachments);
        editors.add(items);

        generator = new AttachmentGenerator(customer, patient, charges, context.getContext());
        items.addModifiableListener(modifiable -> onItemsChanged());

        // The following forces all of the invoice items to be added to charges.
        for (Act item :items.getCurrentActs()) {
            items.getEditor(item);
        }
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return (Party) getParticipant("patient");
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new ClaimEditor((FinancialAct) reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Generates attachments.
     *
     * @return {@code true} if all attachments were successfully generated
     */
    public boolean generateAttachments() {
        boolean generate = generator.generate(getObject(), attachments);
        if (!generate) {
            ClaimLayoutStrategy strategy = (ClaimLayoutStrategy) getView().getLayout();
            strategy.selectAttachments();
        }
        return generate;
    }

    /**
     * Verifies attachments aren't in error.
     * <p>
     * On error, displays an error dialog of the attachment that is in error.
     *
     * @return if {@code true} if all attachments are valid
     */
    public boolean checkAttachments() {
        boolean result = true;
        for (Act attachment : attachments.getCurrentActs()) {
            if (Attachment.Status.ERROR.isA(attachment.getStatus())) {
                ErrorDialog.show(Messages.get("patient.insurance.attachments.title"),
                                 Messages.format("patient.insurance.attachments.message", attachment.getName(),
                                                 new ActBean(attachment).getString("error")));
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ClaimLayoutStrategy(getPatient(), insurerId, items, attachments);
    }

    /**
     * Returns the item acts to sum.
     *
     * @return the acts
     */
    @Override
    protected List<Act> getItemActs() {
        return items.getActs();
    }

    /**
     * Determines if a claim can be submitted via an {@link InsuranceService}.
     *
     * @param act the claim act
     * @return {@code true} if the claim can be submitted
     */
    private boolean canSubmitClaim(Act act) {
        Claim claim = ServiceHelper.getBean(InsuranceFactory.class).createClaim(act);
        return ServiceHelper.getBean(InsuranceServices.class).canSubmit(claim.getPolicy().getInsurer());
    }

}
