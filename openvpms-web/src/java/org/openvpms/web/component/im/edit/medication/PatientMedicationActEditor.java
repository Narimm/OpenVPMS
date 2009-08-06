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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.PatientActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMedicationActEditor extends PatientActEditor {

    /**
     * Construct a new <tt>PatientMedicationActEditor</tt>.
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

        if (parent != null) {
            ActBean bean = new ActBean(parent);
            if (bean.hasNode("product")) {
                // update the product from the parent
                IMObjectReference product = bean.getParticipantRef(ProductArchetypes.PRODUCT_PARTICIPATION);
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
     * @param product the product reference. May be <tt>null</tt>
     */
    public void setProduct(IMObjectReference product) {
        IMObjectReference current = getParticipantRef("product");
        if (!ObjectUtils.equals(current, product)) {
            setParticipant("product", product);
            IMObject prod = IMObjectHelper.getObject(product);
            if (prod != null) {
                IMObjectBean bean = new IMObjectBean(prod);
                if (bean.hasNode("dispInstructions")) {
                    Property label = getProperty("label");
                    label.setValue(bean.getValue("dispInstructions"));
                }
            }
        }
    }

    /**
     * Returns the product.
     *
     * @return the product refereence. May be <tt>null</tt>
     */
    public IMObjectReference getProduct() {
        return getParticipantRef("product");
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("quantity").setValue(quantity);
    }

}
