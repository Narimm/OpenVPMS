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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.query.ResultSet;

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

    /**
     * Creates a new result set.
     *
     * @param property the collection property
     * @return a new result set
     */
    @Override
    public ResultSet<IMObject> createResultSet(CollectionPropertyEditor property) {
        ResultSet<IMObject> set = super.createResultSet(property);
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
    }
}
