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
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ubl.InvoiceType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.esci.service.InvoiceService;

import javax.annotation.Resource;
import java.util.Arrays;


/**
 * Implementation of the {@link InvoiceService} that maps invoices to <em>act.supplierDelivery</em> acts using
 * {@link InvoiceMapper}.
 * <p/>
 * UBL invoices are mapped to deliveries rather than <em>act.supplierAccountChargesInvoice</em> to reflect the fact
 * that practices may not use suplier invoices. An invoice can be created from the delivery if required.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceServiceAdapter implements InvoiceService {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The invoice mapper.
     */
    private InvoiceMapper mapper;

    /**
     * The listener to notify when an invoice is received. May be <tt>null</tt>
     */
    private InvoiceListener listener;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InvoiceServiceAdapter.class);


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
     * @param listener the listener to notify. May be <tt>null</tt>
     */
    @Resource
    public void setInvoiceListener(InvoiceListener listener) {
        this.listener = listener;
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
     * Submits an invoice.
     *
     * @param invoice the invoice to submit.
     * @throws ESCIException for any error
     */
    public void submitInvoice(InvoiceType invoice) throws ESCIException {
        Delivery delivery = mapper.map(invoice);
        service.save(delivery.getActs());
        linkDeliveryToOrder(delivery);
        notifyListener(delivery.getDelivery());
    }

    /**
     * Links delivery items to their corresponding order items.
     *
     * @param delivery the delivery
     */
    private void linkDeliveryToOrder(Delivery delivery) {
        for (FinancialAct item : delivery.getDeliveryItems()) {
            IMObjectReference orderItemRef = delivery.getOrderItem(item);
            if (orderItemRef != null) {
                linkItem(item, orderItemRef);
            }
        }
    }

    /**
     * Links an <em>act.supplierDeliveryItem</em> to an <em>act.supplierOrderItem</em>, if it exists
     *
     * @param delivery the delivery item
     * @param orderRef the order item referemce
     */
    private void linkItem(FinancialAct delivery, IMObjectReference orderRef) {
        FinancialAct order = (FinancialAct) service.get(orderRef);
        if (order != null) {
            ActBean bean = factory.createActBean(delivery);
            bean.addNodeRelationship("order", order);
            service.save(Arrays.asList(delivery, order));
        } else {
            log.warn("Cannot link " + delivery.getObjectReference().getArchetypeId() + ":" + delivery.getId()
                     + " to " + orderRef.getArchetypeId() + ":" + orderRef.getId()
                     + ": order doesn't exist");
        }
    }

    /**
     * Notifies the registered listener that an invoice has been received and processed.
     *
     * @param delivery the delivery that the invoice was mapped to
     */
    private void notifyListener(Act delivery) {
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
