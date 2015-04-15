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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.stock;


/**
 * Stock archetype short names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockArchetypes {

    /**
     * Stock participation short name.
     */
    public static final String STOCK_PARTICIPATION = "participation.stock";

    /**
     * Stock location short name.
     */
    public static final String STOCK_LOCATION
            = "party.organisationStockLocation";

    /**
     * Stock location participation short name.
     */
    public static final String STOCK_LOCATION_PARTICIPATION
            = "participation.stockLocation";

    /**
     * Stock transfer location participation short name.
     */
    public static final String STOCK_XFER_LOCATION_PARTICIPATION
            = "participation.stockTransferLocation";

    /**
     * Stock transfer act short name.
     */
    public static final String STOCK_TRANSFER = "act.stockTransfer";

    /**
     * Stock transfer act item short name.
     */
    public static final String STOCK_TRANSFER_ITEM = "act.stockTransferItem";

    /**
     * Stock transfer item relationship short name.
     */
    public static final String STOCK_TRANSFER_ITEM_RELATIONSHIP
            = "actRelationship.stockTransferItem";

    /**
     * Stock adjust act short name.
     */
    public static final String STOCK_ADJUST = "act.stockAdjust";

    /**
     * Stock adjust act item short name.
     */
    public static final String STOCK_ADJUST_ITEM = "act.stockAdjustItem";

    /**
     * Stock adjust item relationship short name.
     */
    public static final String STOCK_ADJUST_ITEM_RELATIONSHIP
            = "actRelationship.stockAdjustItem";

    /**
     * Product stock location relationship short name.
     */
    public static final String PRODUCT_STOCK_LOCATION_RELATIONSHIP
            = "entityRelationship.productStockLocation";
}
