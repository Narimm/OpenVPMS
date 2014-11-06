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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.esci.adapter.dispatcher.invoice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.dispatcher.DocumentProcessor;
import org.openvpms.esci.adapter.dispatcher.InboxDocument;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.invoice.Delivery;
import org.openvpms.esci.adapter.map.invoice.InvoiceMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.invoice.InvoiceType;

import javax.annotation.Resource;


/**
 * Maps invoices to <em>act.supplierDelivery</em> acts using {@link InvoiceMapper}.
 * <p/>
 * UBL invoices are mapped to deliveries rather than <em>act.supplierAccountChargesInvoice</em> to reflect the fact
 * that practices may not use supplier invoices. An invoice can be created from the delivery if required.
 *
 * @author Tim Anderson
 */
public class InvoiceProcessor implements DocumentProcessor {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The invoice mapper.
     */
    private InvoiceMapper mapper;

    /**
     * The listener to notify when an invoice is received. May be {@code null}
     */
    private InvoiceListener listener;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InvoiceProcessor.class);


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
     * Registers the invoice mapper.
     *
     * @param mapper the invoice mapper
     */
    @Resource
    public void setInvoiceMapper(InvoiceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Registers a listener to be notified when an invoice is received.
     *
     * @param listener the listener to notify. May be {@code null}
     */
    @Resource
    public void setInvoiceListener(InvoiceListener listener) {
        this.listener = listener;
    }

    /**
     * Determines if this processor can handle the supplied document.
     *
     * @param document the document
     * @return {@code true} if the processor can handle the document, otherwise {@code false}
     */
    public boolean canHandle(InboxDocument document) {
        return document.getContent() instanceof InvoiceType;
    }

    /**
     * Processes an invoice.
     *
     * @param document      the document to process
     * @param supplier      the supplier submitting the invoice
     * @param stockLocation the stock location
     * @param accountId     the supplier account identifier
     */
    public void process(InboxDocument document, Party supplier, Party stockLocation, String accountId) {
        InvoiceType invoice = (InvoiceType) document.getContent();
        try {
            Delivery delivery = mapper.map(invoice, supplier, stockLocation, accountId);
            service.save(delivery.getActs());
            notifyListener(delivery.getDelivery());
        } catch (Throwable exception) {
            String invoiceId = (invoice.getID() != null) ? invoice.getID().getValue() : null;
            Message message = ESCIAdapterMessages.failedToProcessInvoice(
                    invoiceId, supplier, stockLocation, exception.getMessage());
            throw new ESCIAdapterException(message, exception);
        }
    }

    /**
     * Notifies the registered listener that an invoice has been received and processed.
     *
     * @param delivery the delivery that the invoice was mapped to
     */
    protected void notifyListener(FinancialAct delivery) {
        InvoiceListener l = listener;
        if (l != null) {
            try {
                l.receivedInvoice(delivery);
            } catch (Throwable exception) {
                log.error("InvoiceListener threw exception", exception);
            }
        }
    }

}
