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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.pharmacy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.InvoiceItemStatus;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.query.TypedQuery;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Join;
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.customer.charge.OrderPlacer;
import org.openvpms.web.workspace.customer.charge.OrderServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Discontinues orders associated with invoices.
 *
 * @author Tim Anderson
 * @see PharmacyOrderDiscontinuationJob
 * @see OrderPlacer#discontinue
 */
public class OrderDiscontinuer {

    /**
     * The order services.
     */
    private final OrderServices orderServices;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OrderDiscontinuer.class);

    /**
     * Constructs a {@link OrderDiscontinuer}.
     *
     * @param orderServices the order services
     * @param service       the archetype service
     */
    public OrderDiscontinuer(OrderServices orderServices, IArchetypeService service) {
        if (service instanceof IArchetypeRuleService) {
            // Want to avoid triggering rules when invoice items are saved, as these are associated with POSTED invoices
            throw new IllegalArgumentException("Argument 'service' must have rules disabled");
        }
        this.orderServices = orderServices;
        this.service = service;
    }

    /**
     * Discontinues pharmacy orders associated with an invoice.
     *
     * @param invoice  the invoice
     * @param user     the user to send in HL7 messages
     * @param practice the practice
     * @param cache    object cache
     */
    public void discontinue(FinancialAct invoice, User user, Party practice, IMObjectCache cache) {
        List<Act> items = new ArrayList<>();
        IMObjectBean bean = service.getBean(invoice);
        for (Act item : bean.getTargets("items", Act.class)) {
            if (InvoiceItemStatus.ORDERED.equals(item.getStatus())) {
                items.add(item);
            }
        }
        if (!items.isEmpty()) {
            Party customer = bean.getTarget("customer", Party.class);
            Party location = bean.getTarget("location", Party.class);
            if (log.isDebugEnabled()) {
                String custName = (customer != null) ? customer.getName() + "(" + customer.getId() + ")" : null;
                Date endTime = invoice.getActivityEndTime();
                String date = (endTime != null) ? DateFormatter.formatDateTime(endTime, false) : null;
                log.debug("Discontinuing orders for invoice=" + invoice.getId() + ", POSTED at " + date
                          + " for customer=" + custName);
            }
            if (customer != null && location != null) {
                // these should never be null.
                OrderPlacer placer = new OrderPlacer(customer, location, user, practice, cache, orderServices);
                placer.initialise(items);
                placer.discontinue(Collections.emptyList());
            }
            for (Act item : items) {
                // always mark as discontinued even if no messages were sent as can't do anything else with them
                // and don't want to continually reprocess
                item.setStatus(InvoiceItemStatus.DISCONTINUED);
            }
            service.save(items);
        }
    }

    /**
     * Returns up to {@code batchSize} invoices POSTED prior to {@code time}.
     *
     * @param time      the time
     * @param batchSize the batch size
     * @return the invoices
     */
    public List<FinancialAct> getInvoices(Date time, int batchSize) {
        CriteriaBuilder cb = service.getCriteriaBuilder();
        CriteriaQuery<FinancialAct> criteriaQuery = cb.createQuery(FinancialAct.class).distinct(true);
        Root<FinancialAct> root = criteriaQuery.from(FinancialAct.class, CustomerAccountArchetypes.INVOICE)
                .alias("invoice");
        Join<IMObject, IMObject> item = root.join("items").join("target").alias("item");
        item.on(cb.equal(item.get("status"), InvoiceItemStatus.ORDERED));
        criteriaQuery.where(cb.equal(root.get("status"), ActStatus.POSTED),
                            cb.lessThanOrEqualTo(root.get("endTime"), time));
        TypedQuery<FinancialAct> query = service.createQuery(criteriaQuery);
        query.setMaxResults(batchSize);
        return query.getResultList();
    }
}
