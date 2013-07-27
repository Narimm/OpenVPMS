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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.text.TitledTextArea;

import java.math.BigDecimal;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientMedicationActEditor extends PatientActEditor {

    /**
     * Dispensing units label.
     */
    private Label dispensingUnits;

    /**
     * Usage notes text.
     */
    private TitledTextArea usageNotes;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly = false;

    /**
     * Determines if the medication was dispensed from a prescription. If so, the quantity and label nodes should be
     * displayed read-only.
     */
    private boolean prescription = false;

    /**
     * Constructs a {@link PatientMedicationActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientMedicationActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }

        dispensingUnits = LabelFactory.create();
        String displayName = DescriptorHelper.getDisplayName(ProductArchetypes.MEDICATION, "usageNotes");
        usageNotes = new TitledTextArea(displayName);
        usageNotes.setEnabled(false);

        ActBean medBean = new ActBean(act);
        prescription = medBean.hasRelationship(PatientArchetypes.PRESCRIPTION_MEDICATION);

        if (parent != null) {
            ActBean bean = new ActBean(parent);
            if (bean.hasNode("product")) {
                // update the product from the parent
                Product product = (Product) getObject(bean.getNodeParticipantRef("product"));
                if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                    setProduct(product);
                    if (bean.hasNode("quantity")) {
                        setQuantity(bean.getBigDecimal("quantity"));
                    }
                } else {
                    setProduct(null);
                }
            }
        } else {
            Product product = getProduct();
            updateDispensingUnits(product);
            updateUsageNotes(product);
        }
    }

    /**
     * Updates the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        if (setParticipant("product", product)) {
            if (getProductEditor() == null) {
                productModified(product); // only invoke if the product participation changed
            }
        }
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        return (Product) getParticipant("product");
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if {@code true} display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProductReadOnly = readOnly;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("quantity").setValue(quantity);
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return getProperty("quantity").getBigDecimal();
    }

    /**
     * Determines if the medication was dispensed from a prescription.
     * If {@code true}, then the quantity and label should be displayed read-only.
     *
     * @param prescription if {@code true} display the quantity and label read-only
     */
    public void setDispensedFromPrescription(boolean prescription) {
        this.prescription = prescription;
    }


    /**
     * Sets the dispensing instructions label.
     *
     * @param instructions the dispensing instructions
     */
    public void setLabel(String instructions) {
        Property label = getProperty("label");
        label.setValue(instructions);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        PatientMedicationActLayoutStrategy strategy = new PatientMedicationActLayoutStrategy() {
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                ComponentState state = super.createComponent(property, parent, context);
                if ("quantity".equals(property.getName())) {
                    Component component = RowFactory.create("CellSpacing", state.getComponent(), dispensingUnits);
                    state = new ComponentState(component, property);
                }
                return state;
            }
        };
        strategy.setProductReadOnly(showProductReadOnly);
        strategy.setDispensedFromPrescription(prescription);
        strategy.setUsageNotes(usageNotes);
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        final ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            product.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    productModified(product.getEntity());
                }
            });
        }
        super.onLayoutCompleted();
    }

    /**
     * Invoked when the product is modified.
     *
     * @param product the product. May be {@code null}
     */
    protected void productModified(Product product) {
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            if (bean.hasNode("dispInstructions")) {
                String dispInstructions = bean.getString("dispInstructions");
                setLabel(dispInstructions);
            }
        }
        updateDispensingUnits(product);
        updateUsageNotes(product);
    }

    /**
     * Updates the dispensing units label.
     *
     * @param product the product. May be {@code null}
     */
    private void updateDispensingUnits(Product product) {
        String units = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            units = LookupNameHelper.getName(product, "dispensingUnits");
        }
        dispensingUnits.setText(units);
    }

    /**
     * Updates the usage notes.
     *
     * @param product the product. May be {@code null}
     */
    private void updateUsageNotes(Product product) {
        String notes = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product);
            notes = bean.getString("usageNotes");
        }
        usageNotes.setText(notes);
        usageNotes.setVisible(!StringUtils.isEmpty(notes));
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor, or {@code null} if none exists
     */
    private ProductParticipationEditor getProductEditor() {
        ParticipationEditor<Product> editor = getParticipationEditor("product", false);
        return (ProductParticipationEditor) editor;
    }

}
