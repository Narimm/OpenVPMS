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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;
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
     * Constructs an {@link ClaimEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ClaimEditor(FinancialAct act, IMObject parent, LayoutContext layoutContext) {
        super(act, parent, "amount", layoutContext);

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

        Context context = layoutContext.getContext();
        if (act.isNew()) {
            initParticipant("patient", context.getPatient());
            initParticipant("location", context.getLocation());
            initParticipant("clinician", context.getClinician());
            initParticipant("user", context.getUser());
        }
        Party patient = getPatient();
        if (patient == null) {
            throw new IllegalStateException("Claim has no patient");
        }

        Editors editors = getEditors();
        attachments = new AttachmentCollectionEditor(getCollectionProperty("attachments"), act, layoutContext);
        Charges charges = new Charges();
        items = new ClaimItemCollectionEditor(getCollectionProperty("items"), act, customer, patient,
                                              charges, attachments, layoutContext);
        editors.add(attachments);
        editors.add(items);

        generator = new AttachmentGenerator(customer, patient, charges, context);
        items.addModifiableListener(modifiable -> onItemsChanged());

        // The following forces all of the invoice items to be added to charges.
        for (Act item : items.getCurrentActs()) {
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
     * Returns the claim amount.
     *
     * @return the claim amount
     */
    public BigDecimal getAmount() {
        return getProperty("amount").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance of the editor, or {@code null} if the object no longer has {@code PENDING} status.
     * @throws IllegalStateException if the object no longer exists
     */
    @Override
    public IMObjectEditor newInstance() {
        ClaimEditor editor = null;
        FinancialAct act = (FinancialAct) reload(getObject());
        if (Claim.Status.PENDING.isA(act.getStatus())) {
            editor = new ClaimEditor(act, getParent(), getLayoutContext());
        }
        return editor;
    }

    /**
     * Generates attachments.
     *
     * @return {@code true} if all attachments were successfully generated
     */
    public boolean generateAttachments() {
        return generator.generate(getObject(), attachments);
    }

    /**
     * Displays the attachments tabs.
     */
    public void showAttachments() {
        ClaimLayoutStrategy strategy = (ClaimLayoutStrategy) getView().getLayout();
        strategy.selectAttachments();
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
     * Returns the claim items editor
     *
     * @return the claim items editor
     */
    protected ClaimItemCollectionEditor getItems() {
        return items;
    }

    /**
     * Returns the attachments editor.
     *
     * @return the attachments editor
     */
    protected AttachmentCollectionEditor getAttachments() {
        return attachments;
    }

    /**
     * Save any edits.
     * <p>
     * This uses {@link #saveObject()} to save the object prior to saving any children with {@link #saveChildren()}.
     * <p>
     * This is necessary to avoid stale object exceptions when related acts are deleted.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        saveObject();
        saveChildren();
    }

    /**
     * Invoked when layout has completed.
     * <p>
     * This can be used to perform processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        attachments.getComponent();  // force rendering of the component so deletion works
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ClaimLayoutStrategy(getPatient(), items, attachments);
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

}
