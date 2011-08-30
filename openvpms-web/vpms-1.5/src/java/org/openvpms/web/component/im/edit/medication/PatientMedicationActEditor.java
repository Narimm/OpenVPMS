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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.medication;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Component;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.PatientActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMedicationActEditor extends PatientActEditor {

    /**
     * Dispensing units label.
     */
    private Label dispensingUnits;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly = false;


    /**
     * Constructs a <tt>PatientMedicationActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public PatientMedicationActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }

        dispensingUnits = LabelFactory.create();

        if (parent != null) {
            ActBean bean = new ActBean(parent);
            if (bean.hasNode("product")) {
                // update the product from the parent
                Product product = (Product) bean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION);
                if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                    setProduct(product);
                    if (bean.hasNode("quantity")) {
                        setQuantity(bean.getBigDecimal("quantity"));
                    }
                } else {
                    setProduct(null);
                }
            }
        }
    }

    /**
     * Updates the product, if it not the same as the existing product.
     * On update, the label will be set to the dispensing label from the
     * product's dispensing instructions, if available.
     *
     * @param product the product. May be <tt>null</tt>
     */
    public void setProduct(Product product) {
        IMObjectReference current = getParticipantRef("product");
        IMObjectReference productRef = (product != null) ? product.getObjectReference() : null;
        if (!ObjectUtils.equals(current, productRef)) {
            setParticipant("product", product);
            if (product != null) {
                IMObjectBean bean = new IMObjectBean(product);
                if (bean.hasNode("dispInstructions")) {
                    Property label = getProperty("label");
                    label.setValue(bean.getValue("dispInstructions"));
                }
            }
        }
        updateDispensingUnits(product);
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
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProductReadOnly = readOnly;
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
        return strategy;
    }

    /**
     * Updates the dispensing units label.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateDispensingUnits(Product product) {
        String units = "";
        if (product != null) {
            units = LookupNameHelper.getName(product, "dispensingUnits");
        }
        dispensingUnits.setText(units);
    }

}
