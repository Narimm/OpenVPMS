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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.invoice;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.act.ActRelationshipCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Viewer for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sorts the items on descending start time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceItemRelationshipCollectionViewer
        extends ActRelationshipCollectionViewer {

    /**
     * Constructs a new <tt>InvoiceItemRelationshipCollectionViewer</tt>.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public InvoiceItemRelationshipCollectionViewer(CollectionProperty property,
                                                   Act act,
                                                   LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        ResultSet<IMObject> set = super.createResultSet();
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime",
                                                             false)});
        return set;
    }
}
