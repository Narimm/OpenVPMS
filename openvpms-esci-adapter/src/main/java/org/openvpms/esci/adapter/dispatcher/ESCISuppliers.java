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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper for locating ESCI suppliers, and accessing their configuration.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class ESCISuppliers {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs an <tt>ESCISuppliers</tt>.
     *
     * @param service the archetype service
     */
    public ESCISuppliers(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns all active suppliers that have ESCI configurations.
     *
     * @return a list of suppliers with ESCI configurations
     */
    public List<Party> getSuppliers() {
        List<Party> result = new ArrayList<Party>();
        ArchetypeQuery query = new ArchetypeQuery("party.supplier*", true);
        IMObjectQueryIterator<Party> iter = new IMObjectQueryIterator<Party>(service, query);
        while (iter.hasNext()) {
            Party supplier = iter.next();
            Collection<EntityRelationship> relationships = getESCIRelationships(supplier);
            if (!relationships.isEmpty()) {
                result.add(supplier);
            }
        }
        return result;
    }

    /**
     * Returns any active <em>entityRelationship.supplierStockLocationESCI</em> relationships that a supplier may have,
     * filtering duplicate services.
     *
     * @param supplier the supplier
     * @return <em>entityRelationship.supplierStockLocationESCI</em>
     */
    public Collection<EntityRelationship> getESCIRelationships(Party supplier) {
        EntityBean bean = new EntityBean(supplier, service);
        List<EntityRelationship> relationships = bean.getRelationships(
                SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI);
        Map<String, EntityRelationship> filtered = new TreeMap<String, EntityRelationship>();
        for (EntityRelationship relationship : relationships) {
            if (relationship.isActive()) {
                IMObjectBean relBean = new IMObjectBean(relationship, service);
                String key = relBean.getString("serviceURL") + ":" + relBean.getString("accountId") + ":"
                             + relBean.getString("username") + ":" + relBean.getString("password");
                filtered.put(key, relationship);
            }
        }

        return filtered.values();
    }


}
