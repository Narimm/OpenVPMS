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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.order;

/**
 * Customer order archetypes.
 *
 * @author Tim Anderson
 */
public class OrderArchetypes {

    /**
     * Pharmacy order archetype short name.
     */
    public static final String PHARMACY_ORDER = "act.customerOrderPharmacy";

    /**
     * Pharmacy order item archetype short name.
     */
    public static final String PHARMACY_ORDER_ITEM = "act.customerOrderItemPharmacy";

    /**
     * Pharmacy return archetype short name.
     */
    public static final String PHARMACY_RETURN = "act.customerReturnPharmacy";

    /**
     * Pharmacy return item archetype short name.
     */
    public static final String PHARMACY_RETURN_ITEM = "act.customerReturnItemPharmacy";

    /**
     * Investigation return archetype short name.
     */
    public static final String INVESTIGATION_RETURN = "act.customerReturnInvestigation";

    /**
     * Investigation return item archetype short name.
     */
    public static final String INVESTIGATION_RETURN_ITEM = "act.customerReturnItemInvestigation";

    /**
     * Order archetype short names.
     */
    public static final String ORDERS = "act.customerOrder*";

    /**
     * Return archetype short names.
     */
    public static final String RETURNS = "act.customerReturn*";

}
