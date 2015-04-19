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

package org.openvpms.archetype.rules.stock;

import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.party.Party;


/**
 * Base class for stock test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractStockTest extends ArchetypeServiceTest {

    /**
     * Helper to create a stock location.
     *
     * @return a new stock location
     */
    protected Party createStockLocation() {
        return ProductTestHelper.createStockLocation();
    }

}
