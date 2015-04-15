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

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.DemographicUpdater;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Collections;
import java.util.List;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DemographicUpdateHelper {

    private final IArchetypeService service;
    private final ActBean itemBean;
    private EntityBean productBean;

    public DemographicUpdateHelper(Act item, IArchetypeService service) {
        itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getParticipant(
                "participation.product");
        if (product != null) {
            productBean = new EntityBean(product, service);
        }
        this.service = service;
    }

    public DemographicUpdateHelper(ActBean itemBean, EntityBean productBean,
                                   IArchetypeService service) {
        this.itemBean = itemBean;
        this.productBean = productBean;
        this.service = service;
    }

    /**
     * Processes an demographic updates associated with a product, if the parent
     * invoice is <em>POSTED</em>.
     *
     * @param invoice the parent invoice
     */
    public void processDemographicUpdates(Act invoice) {
        if (ActStatus.POSTED.equals(invoice.getStatus())) {
            List<Lookup> updates = getDemographicUpdates();
            if (!updates.isEmpty()) {
                processDemographicUpdates(updates);
            }
        }
    }

    public void processDemographicUpdates() {
        List<Lookup> updates = getDemographicUpdates();
        if (!updates.isEmpty() && invoicePosted()) {
            processDemographicUpdates(updates);
        }
    }

    private List<Lookup> getDemographicUpdates() {
        List<Lookup> updates = Collections.emptyList();
        if (productBean != null && productBean.hasNode("updates")) {
            updates = productBean.getValues("updates", Lookup.class);
        }
        return updates;
    }

    /**
     * Determines if the parent invoice is posted.
     *
     * @return <tt>true</tt> if the parent invoice is posted
     */
    private boolean invoicePosted() {
        List<ActRelationship> relationships = itemBean.getRelationships(
                "actRelationship.customerAccountInvoiceItem");
        if (!relationships.isEmpty()) {
            ActRelationship relationship = relationships.get(0);
            ArchetypeQuery query = new ArchetypeQuery(
                    new ObjectRefConstraint("act", relationship.getSource()));
            query.add(new NodeSelectConstraint("act.status"));
            query.add(new NodeConstraint("status", ActStatus.POSTED));
            query.setMaxResults(1);
            ObjectSetQueryIterator iter = new ObjectSetQueryIterator(service,
                                                                     query);
            return iter.hasNext();
        }
        return false;
    }

    private void processDemographicUpdates(List<Lookup> updates) {
        DemographicUpdater updater = new DemographicUpdater(
                service);
        updater.evaluate(itemBean.getAct(), updates);
    }

}
