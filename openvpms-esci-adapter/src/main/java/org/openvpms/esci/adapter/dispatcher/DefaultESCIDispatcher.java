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
 */

package org.openvpms.esci.adapter.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.InboxService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Connects to each configured InboxService, and dispatches documents to the registered
 * {@link DocumentProcessor}s.
 *
 * @author Tim Anderson
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
    public void dispatch() {
        dispatch(new ErrorHandler() {
            @Override
            public boolean terminateOnError() {
                return false;
            }

            @Override
            public void error(Throwable exception) {
                log.error(exception.getMessage(), exception);
            }
        });
    }

    /**
     * Dispatch documents.
     * <p/>
     * This will dispatch documents until there is either:
     * <ul>
     * <li>no more documents available</li>
     * <li>the {@link #stop} method is invoked, from another thread</li>
     * <li>an error occurs, and the supplied handler's {@link ErrorHandler#terminateOnError} method returns
     * {@code true}</li>
     * </ul>
     * If {@link #stop} is called, only the executing dispatch terminates.
     */
    @Override
    public synchronized void dispatch(ErrorHandler handler) {
        // NOTE: synchronized as individual inboxes need to be processed synchronously
        try {
            ESCISuppliers helper = new ESCISuppliers(service);
            List<Party> suppliers = helper.getSuppliers();
            Iterator<Party> iter = suppliers.iterator();
            while (!stop && iter.hasNext()) {
                Party supplier = iter.next();
                Collection<EntityRelationship> relationships = helper.getESCIRelationships(supplier);
                dispatch(supplier, relationships, handler);
            }
        } finally {
            stop = false;
        }
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
     * @param supplier      the supplier
     * @param relationships the <em>entityRelationship.supplierStockLocationESCI</em> relationships
     * @param handler       the error handler
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void dispatch(Party supplier, Collection<EntityRelationship> relationships, ErrorHandler handler) {
        Iterator<EntityRelationship> iter = relationships.iterator();
        while (!stop && iter.hasNext()) {
            EntityRelationship rel = iter.next();
            Inbox inbox = getInbox(supplier, rel, handler);
            if (inbox != null) {
                dispatch(inbox, handler);
            }
        }
    }

    /**
     * Dispatch documents from the supplied inbox.
     *
     * @param inbox   the inbox to read
     * @param handler the error handler
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void dispatch(Inbox inbox, ErrorHandler handler) {
        try {
            InboxDispatcher dispatcher = new InboxDispatcher(inbox, processors);
            while (!stop && dispatcher.hasNext()) {
                dispatcher.dispatch();
            }
        } catch (ESCIAdapterException exception) {
            handler.error(exception);
            if (handler.terminateOnError()) {
                throw exception;
            }
        } catch (Throwable cause) {
            ESCIAdapterException exception = new ESCIAdapterException(ESCIAdapterMessages.failedToProcessInbox(
                    inbox.getSupplier(), inbox.getStockLocation(), cause.getMessage()), cause);
            handler.error(exception);
            if (handler.terminateOnError()) {
                throw exception;
            }
        }
    }

    /**
     * Returns the inbox for the given supplier and ESCI configuration.
     *
     * @param supplier      the supplier
     * @param configuration the ESCI configuration
     * @param handler       the error handler
     * @return the inbox, or {@code null} if no inbox can be obtained
     * @throws ESCIAdapterException      for any ESCI adapter error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Inbox getInbox(Party supplier, EntityRelationship configuration, ErrorHandler handler) {
        Inbox result = null;
        if (configuration.getTarget() != null) {
            IMObjectBean bean = factory.createBean(configuration);
            String accountId = bean.getString("accountId");
            Party stockLocation = (Party) service.get(configuration.getTarget());
            if (stockLocation != null) {
                try {
                    InboxService service = locator.getInboxService(supplier, stockLocation);
                    result = new Inbox(supplier, stockLocation, accountId, service);
                } catch (ESCIAdapterException exception) {
                    handler.error(exception);
                    if (handler.terminateOnError()) {
                        throw exception;
                    }
                } catch (Throwable cause) {
                    ESCIAdapterException exception = new ESCIAdapterException(ESCIAdapterMessages.failedToProcessInbox(
                            supplier, stockLocation, cause.getMessage()), cause);
                    handler.error(exception);
                    if (handler.terminateOnError()) {
                        throw exception;
                    }
                }
            }
        }
        return result;
    }

}
