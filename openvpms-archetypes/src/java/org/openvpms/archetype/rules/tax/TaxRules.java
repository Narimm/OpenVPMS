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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.tax;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

/**
 * Tax Rules
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class TaxRules {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxRules.class);
    
    /**
     * Calculate the amount of tax for the passed financial act utilising tax type information
     * for the products, product type, organisation and customer associated with the act and any related
     * parentchild acts.
     * The tax amount will be calculated and stored in the tax node for the Act and related parentchild acts.  
     * 
     * @param service
     *            the archetype service
     * @param act
     *            the financial act to calculate tax for
     * @throws RuleEngineException            
     */
    
    public static void calculateTax(IArchetypeService service, FinancialAct act) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing TaxRules.calculateTax");
        }
        
        // TODO Tax Calculation algorithm
        
        act.setTaxAmount(new Money("0.0"));
    }

}
