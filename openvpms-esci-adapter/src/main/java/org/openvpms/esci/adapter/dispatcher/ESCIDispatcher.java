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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.InboxService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Connects to each InboxService and processes their documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCIDispatcher {

    private List<DocumentProcessor> processors;

    private IArchetypeService service;

    private IMObjectBeanFactory factory;

    private SupplierServiceLocator locator;

    private volatile boolean stop;

    private static final Log log = LogFactory.getLog(ESCIDispatcher.class);

    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    @Resource
    public void setSupplierServiceLocator(SupplierServiceLocator locator) {
        this.locator = locator;
    }

    /**
     * Registers the document processors.
     *
     * @param processors the processors
     */
    @Resource
    public void setDocumentProcessors(List<DocumentProcessor> processors) {
        this.processors = processors;
    }

    /**
     * Starts dispatching.
     */
    public void start() {
        List<Party> suppliers = getSuppliers();
        Iterator<Party> iter = suppliers.iterator();
        while (!stop && iter.hasNext()) {
            dispatch(iter.next());
        }
    }

    public void stop() {
        this.stop = true;
    }

    protected void dispatch(Party supplier) {
        Collection<EntityRelationship> relationships = getESCIRelationships(supplier);
        Iterator<EntityRelationship> iter = relationships.iterator();
        while (!stop && iter.hasNext()) {
            EntityRelationship rel = iter.next();
            Inbox inbox = getInbox(supplier, rel);
            if (inbox != null) {
                dispatch(inbox);
            }
        }
    }

    protected void dispatch(Inbox inbox) {
        InboxDispatcher dispatcher = new InboxDispatcher(inbox, processors);
        while (!stop && dispatcher.hasNext()) {
            dispatcher.dispatch();
        }
    }

    private Inbox getInbox(Party supplier, EntityRelationship rel) {
        Inbox result = null;
        if (rel.getTarget() != null) {
            Party stockLocation = (Party) service.get(rel.getTarget());
            if (stockLocation != null) {
                try {
                    InboxService service = locator.getInboxService(supplier, stockLocation);
                    result = new Inbox(supplier, service);
                } catch (ESCIAdapterException exception) {
                    log.error(exception);
                }
            }
        }
        return result;
    }


    /**
     * Returns all suppliers that have ESCI configurations.
     *
     * @return a list of suppliers with ESCI configurations
     */
    protected List<Party> getSuppliers() {
        List<Party> result = new ArrayList<Party>();
        ArchetypeQuery query = new ArchetypeQuery("party.supplier*");
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
     * Returns any <em>entityRelationship.supplierStockLocationESCI</em> relationships that a supplier may have,
     * filtering duplicate services.
     *
     * @param supplier the supplier
     * @return <em>entityRelationship.supplierStockLocationESCI</em>
     */
    private Collection<EntityRelationship> getESCIRelationships(Party supplier) {
        EntityBean bean = factory.createEntityBean(supplier);
        List<EntityRelationship> relationships = bean.getRelationships(
                SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI);
        Map<String, EntityRelationship> filtered = new TreeMap<String, EntityRelationship>();
        for (EntityRelationship relationship : relationships) {
            IMObjectBean relBean = factory.createBean(relationship);
            String key = relBean.getString("serviceURL") + ":" + relBean.getString("accountId") + ":"
                         + relBean.getString("username") + ":" + relBean.getString("password");
            filtered.put(key, relationship);
        }

        return filtered.values();
    }

}
