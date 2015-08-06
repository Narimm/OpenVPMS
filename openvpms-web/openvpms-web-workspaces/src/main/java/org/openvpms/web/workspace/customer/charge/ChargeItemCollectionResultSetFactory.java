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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;

/**
 * An implementation of the {@link CollectionResultSetFactory} for charge items.
 * <p/>
 * This sorts items on descending startTime.
 *
 * @author Tim Anderson
 */
public class ChargeItemCollectionResultSetFactory extends ActCollectionResultSetFactory {

    /**
     * The singleton instance.
     */
    public static final CollectionResultSetFactory INSTANCE = new ChargeItemCollectionResultSetFactory();

    /**
     * Default constructor.
     */
    protected ChargeItemCollectionResultSetFactory() {
    }

}
