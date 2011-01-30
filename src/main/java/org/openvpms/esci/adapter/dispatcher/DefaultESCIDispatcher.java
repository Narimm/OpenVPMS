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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
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
 * Connects to each configured InboxService, and dispatches documents to the registered
 * {@link DocumentProcessor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DefaultESCIDispatcher implements ESCIDispatcher {

    /**
     * The document processors.
     */
    private List<DocumentProcessor> processors;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * The service locator.
     */
    private SupplierServiceLocator locator;

    /**
     * Determines if dispatching should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DefaultESCIDispatcher.class);


    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Registers the supplier service locator.
     *
     * @param locator the locator
     */
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
     * Dispatch documents.
     * <p/>
     * This will dispatch documents until there is either:
     * <ul>
     * <li>no more documents available</li>
     * <li>the {@link #stop} method is invoked, from another thread</li>
     * </ul>
     * If {@link #stop} is called, only the executing dispatch terminates.
     */
    public synchronized void dispatch() {
        dispatch(false);
    }

    /**
     * Dispatch documents.
     * <p/>
     * This will dispatch documents until there is either:
     * <ul>
     * <li>no more documents available</li>
     * <li>the {@link #stop} method is invoked, from another thread</li>
     * <li>an error occurs and <tt>terminateOnError</tt> is <tt>true</tt>
     * </ul>
     * If {@link #stop} is called, only the executing dispatch terminates.
     *
     * @param terminateOnError if <tt>true</tt> terminate on the first error
     */
    public synchronized void dispatch(boolean terminateOnError) {
        // NOTE: synchronized as individual inboxes need to be processed synchronously
        List<Party> suppliers = getSuppliers();
        Iterator<Party> iter = suppliers.iterator();
        while (!stop && iter.hasNext()) {
            dispatch(iter.next(), terminateOnError);
        }
        stop = false;
    }

    /**
     * Flags the current dispatch to stop.
     * <p/>
     * This does not block waiting for the dispatch to complete.
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * Dispatch documents from the supplier.
     *
     * @param supplier         the supplier
     * @param terminateOnError if <tt>true</tt> terminate on the first error
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void dispatch(Party supplier, boolean terminateOnError) {
        Collection<EntityRelationship> relationships = getESCIRelationships(supplier);
        Iterator<EntityRelationship> iter = relationships.iterator();
        while (!stop && iter.hasNext()) {
            EntityRelationship rel = iter.next();
            Inbox inbox = getInbox(supplier, rel, terminateOnError);
            if (inbox != null) {
                dispatch(inbox, terminateOnError);
            }
        }
    }

    /**
     * Dispatch documents from the supplied inbox.
     *
     * @param inbox            the inbox to read
     * @param terminateOnError if <tt>true</tt> terminate on the first error
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void dispatch(Inbox inbox, boolean terminateOnError) {
        try {
            InboxDispatcher dispatcher = new InboxDispatcher(inbox, processors);
            while (!stop && dispatcher.hasNext()) {
                dispatcher.dispatch();
            }
        } catch (RuntimeException exception) {
            if (terminateOnError) {
                throw exception;
            }
            Party supplier = inbox.getSupplier();
            log.error("Failed to process inbox for supplier: " + supplier.getName()
                      + " (" + supplier.getId() + "): " + exception.getMessage(), exception);
        }
    }

    /**
     * Returns the inbox for the given supplier and ESCI configuration.
     *
     * @param supplier         the supplier
     * @param configuration    the ESCI configuration
     * @param terminateOnError if <tt>true</tt>, rethrow any exception instead of returning null if an inbox cannot be
     *                         obtained
     * @return the inbox, or <tt>null</tt> if no inbox can be obtained
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Inbox getInbox(Party supplier, EntityRelationship configuration, boolean terminateOnError) {
        Inbox result = null;
        if (configuration.getTarget() != null) {
            IMObjectBean bean = factory.createBean(configuration);
            String accountId = bean.getString("accountId");
            Party stockLocation = (Party) service.get(configuration.getTarget());
            if (stockLocation != null) {
                try {
                    InboxService service = locator.getInboxService(supplier, stockLocation);
                    result = new Inbox(supplier, stockLocation, accountId, service);
                } catch (RuntimeException exception) {
                    if (terminateOnError) {
                        throw exception;
                    }
                    log.error("Failed to process inbox for supplier: " + supplier.getName()
                              + " (" + supplier.getId() + "): " + exception.getMessage(), exception);
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
