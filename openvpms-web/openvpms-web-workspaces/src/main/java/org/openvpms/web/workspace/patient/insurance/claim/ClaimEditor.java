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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Attachment;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.insurance.service.Times;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractClaimEditor {

    /**
     * The claim context.
     */
    private final ClaimContext claimContext;

    /**
     * The claimed charges.
     */
    private final Charges charges;

    /**
     * The insurer.
     */
    private final SimpleProperty insurer = new SimpleProperty("insurer", IMObjectReference.class);

    /**
     * The insurer listener.
     */
    private final ModifiableListener insurerListener;

    /**
     * The insurer editor.
     */
    private final IMObjectReferenceEditor<Party> insurerEditor;

    /**
     * Listener for policy number updates.
     */
    private final ModifiableListener policyNumberListener;

    /**
     * The policy number editor.
     */
    private final PolicyNumberEditor policyNumberEditor;

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
     * Determines if the gap claim node is being displayed.
     */
    private boolean showGapClaim;

    /**
     * The identifier of the last gap alert, used to cancel it.
     */
    private long gapAlert = -1;

    /**
     * Constructs an {@link ClaimEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ClaimEditor(FinancialAct act, IMObject parent, LayoutContext layoutContext) {
        super(act, parent, "amount", layoutContext);
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
        Party customer = context.getCustomer();
        if (customer == null) {
            throw new IllegalStateException("Context has no customer");
        }

        claimContext = createClaimContext(act, customer, patient, context);

        // the policy insurer editor
        insurer.setRequired(true);
        insurer.setArchetypeRange(new String[]{SupplierArchetypes.INSURER});
        insurer.setDisplayName(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurer"));
        insurerEditor = IMObjectReferenceEditorFactory.create(insurer, parent, layoutContext);
        insurerEditor.setObject(claimContext.getInsurer());

        // the policy number editor
        SimpleProperty policyNumber = new SimpleProperty("policyNumber", String.class);
        policyNumber.setDisplayName(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurerId"));
        policyNumberEditor = new PolicyNumberEditor(policyNumber, claimContext, layoutContext);

        attachments = new AttachmentCollectionEditor(getCollectionProperty("attachments"), act, layoutContext);
        charges = new Charges(claimContext);
        items = new ClaimItemCollectionEditor(getCollectionProperty("items"), act, claimContext.getCustomer(), patient,
                                              charges, attachments, layoutContext);
        addEditor(policyNumberEditor);
        addEditor(insurerEditor);
        addEditor(attachments);
        addEditor(items);

        generator = new AttachmentGenerator(claimContext.getCustomer(), patient, charges, context);
        items.addModifiableListener(modifiable -> onItemsChanged());

        // The following forces all of the invoice items to be added to charges.
        for (Act item : items.getCurrentActs()) {
            items.getEditor(item);
        }
        insurerListener = modifiable -> onInsurerChanged();
        insurer.addModifiableListener(insurerListener);
        policyNumberListener = modifiable -> onPolicyNumberChanged();
        policyNumberEditor.addModifiableListener(policyNumberListener);
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
     * Returns the insurer.
     *
     * @return the insurer. May be {@code null}, if no policy has been selected
     */
    public Party getInsurer() {
        return claimContext.getInsurer();
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
        boolean result = false;
        DefaultValidator validator = new DefaultValidator();
        if (!validate(validator)) {
            // can only generate attachments for valid claims
            ValidationHelper.showError(validator);
        } else {
            Party location = getLocation();
            if (location != null) {
                result = generator.generate(getObject(), attachments, location);
            }
        }
        return result;
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
                IArchetypeService service = ServiceHelper.getArchetypeService();
                IMObjectBean bean = service.getBean(attachment);
                String error = bean.getString("error");
                ErrorDialog.show(Messages.get("patient.insurance.attachments.title"),
                                 Messages.format("patient.insurance.attachments.message", attachment.getName(), error));
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the times when gap claims can be submitted.
     *
     * @return the gap claim submit times, or {@code null} if gap claims cannot be submitted
     */
    public Times getGapClaimSubmitTimes() {
        return claimContext.getGapClaimSubmitTimes();
    }

    /**
     * Determines if the claim is a gap claim.
     *
     * @return {@code true} if the claim is a gap claim
     */
    public boolean isGapClaim() {
        return getProperty("gapClaim").getBoolean();
    }

    /**
     * Checks the times when gap claims may be submitted.
     */
    public void checkGapClaimSubmitTimes() {
        Party insurer = getInsurer();
        AlertListener alertListener = getAlertListener();
        if (insurer != null && alertListener != null) {
            GapClaimSubmitStatus status = new GapClaimSubmitStatus();
            status.check(insurer, getGapClaimSubmitTimes(), isGapClaim());
            if (status.getMessage() != null) {
                notifyGapAlert(status.getMessage());
            }
        }
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location. May be {@code null}
     */
    protected Party getLocation() {
        return (Party) getParticipant("location");
    }

    /**
     * Creates the claim context.
     *
     * @param act      the claim
     * @param customer the customer
     * @param patient  the patient
     * @param context  the context
     * @return a new claim context
     */
    protected ClaimContext createClaimContext(FinancialAct act, Party customer, Party patient, Context context) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        InsuranceServices insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
        InsuranceRules rules = ServiceHelper.getBean(InsuranceRules.class);
        return new ClaimContext(act, customer, patient, context.getUser(), getLocation(),
                                service, rules, insuranceServices, ServiceHelper.getBean(InsuranceFactory.class));
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
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = insurer.validate(validator);
        if (valid) {
            claimContext.prepare();
            valid = super.doValidation(validator);
            if (valid) {
                boolean gapClaim = isGapClaim();
                if (gapClaim && !claimContext.supportsGapClaims()) {
                    Party insurer = claimContext.getInsurer();
                    String name = (insurer != null) ? insurer.getName() : null;
                    String message = Messages.format("patient.insurance.gap.notsupported", name);
                    validator.add(this, new ValidatorError(message));
                    valid = false;
                }
                if (valid) {
                    // verify that the associated invoices can still be claimed
                    for (FinancialAct invoice : charges.getInvoices()) {
                        if (!charges.canClaimInvoice(invoice)) {
                            valid = false;
                            String displayName = DescriptorHelper.getDisplayName(invoice);
                            String date = DateFormatter.formatDateTime(invoice.getActivityStartTime());
                            String message;
                            if (charges.isReversed(invoice)) {
                                message = Messages.format("patient.insurance.invoice.reversed", displayName, date);
                            } else if (charges.isUnpaid(invoice)) {
                                // invoice is unpaid, but must be paid for a standard claim
                                message = Messages.format("patient.insurance.invoice.unpaid", displayName, date);
                            } else {
                                // invoice is paid, but must be unpaid for a gap claim
                                message = Messages.format("patient.insurance.invoice.paid", displayName, date);
                            }
                            validator.add(this, new ValidatorError(message));
                            break;
                        }
                    }
                }
            }
        }
        return valid;
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
        claimContext.save();
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
        getParticipationEditor("location", false).addModifiableListener(modifiable -> onLocationChanged());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        showGapClaim = isGapClaim() || claimContext.supportsGapClaims();
        return new ClaimLayoutStrategy(getPatient(), insurerEditor, policyNumberEditor, items,
                                       attachments, showGapClaim);
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
     * Invoked when the location changes.
     * <br/>
     * This deletes any generated documents associated with attachments, to ensure they are regenerated with the correct
     * location.
     */
    private void onLocationChanged() {
        claimContext.setLocation(getLocation());
        onLayout();
        attachments.deleteGeneratedDocuments();
        checkGapClaimSubmitTimes();
    }

    /**
     * Notifies of a gap claim alert.
     *
     * @param message the message to display
     */
    private void notifyGapAlert(String message) {
        AlertListener alertListener = getAlertListener();
        if (gapAlert != -1) {
            alertListener.cancel(gapAlert);
        }
        gapAlert = alertListener.onAlert(message);
    }

    /**
     * Invoked when the insurer changes.
     * <p>
     * Updates the policy number.
     */
    private void onInsurerChanged() {
        claimContext.setInsurer(insurerEditor.getObject());
        try {
            policyNumberEditor.removeModifiableListener(policyNumberListener);
            policyNumberEditor.refresh();
        } finally {
            policyNumberEditor.addModifiableListener(policyNumberListener);
        }
        if ((showGapClaim && !isGapClaim() && !claimContext.supportsGapClaims())
            || (!showGapClaim && claimContext.supportsGapClaims())) {
            // . if the gap claim node is being displayed, and isn't selected, and the current insurer doesn't support
            //   gap claims, this will hide the node.
            // . if the gap claim node is not being displayed, and the current insurer supports gap claims,
            //   this will make the node visible
            onLayout();
        }
        checkGapClaimSubmitTimes();
    }

    /**
     * Updated when the policy number changes.
     * <p>
     * Updates the insurer.
     */
    private void onPolicyNumberChanged() {
        try {
            insurer.removeModifiableListener(insurerListener);
            insurerEditor.setObject(claimContext.getInsurer());
        } finally {
            insurer.addModifiableListener(insurerListener);
        }
    }
}
