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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map.invoice;

import org.oasis.ubl.common.aggregate.TaxCategoryType;
import org.oasis.ubl.common.aggregate.TaxSchemeType;
import org.oasis.ubl.common.basic.PercentType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.adapter.map.UBLType;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import java.math.BigDecimal;

/**
 * Wrapper for an UBL <tt>TaxCategoryType</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLTaxCategory extends UBLFinancialType {

    /**
     * The tax category.
     */
    private final TaxCategoryType category;

    /**
     * Constructs an <tt>UBLTaxCategory</tt>.
     *
     * @param category the tax category
     * @param parent   the parent
     * @param currency the expected currency for all amounts
     * @param service  the archetype service
     */
    public UBLTaxCategory(TaxCategoryType category, UBLType parent, String currency, IArchetypeService service) {
        super(parent, currency, service);
        this.category = category;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "TaxCategory";
    }

    /**
     * Returns the type's identifier.
     *
     * @return the identifier
     * @throws ESCIAdapterException if the identifier is mandatory but not set, or the identifier is incorrectly
     *                              specified
     */
    public String getID() {
        return getId(category.getID());
    }

    /**
     * Returns the tax rate, expressed as a percentage.
     *
     * @return the tax rate, or <tt>0.0</tt> if none is provided
     * @throws ESCIAdapterException if the tax rate is incorrectly specified
     */
    public BigDecimal getTaxRate() {
        PercentType percent = getRequired(category.getPercent(), "Percent");
        return getRequired(percent.getValue(), "Percent");
    }

    /**
     * Returns the tax scheme identifier.
     *
     * @return the tax scheme identifier, or <tt>null</tt> if no tax category is provided
     * @throws ESCIAdapterException if more than one tax category is specified, or the tax category doesn't provide a
     *                              tax scheme
     */
    public String getTaxSchemeID() {
        TaxSchemeType scheme = getRequired(category.getTaxScheme(), "TaxScheme");
        return getId(scheme.getID(), "TaxScheme/ID");
    }

}
