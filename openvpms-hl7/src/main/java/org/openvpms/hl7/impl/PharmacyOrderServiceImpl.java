package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.model.Message;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.PharmacyOrderService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderServiceImpl implements PharmacyOrderService {

    private final Connectors connectors;

    private final MessageDispatcher manager;
    private final RDEMessageFactory factory;

    public PharmacyOrderServiceImpl(IArchetypeService service, ILookupService lookups, Connectors connectors,
                                    MessageDispatcherImpl manager) {
        factory = new RDEMessageFactory(manager.getMessageContext(), service, lookups);
        this.connectors = connectors;
        this.manager = manager;
    }

    @Override
    public void createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date) {
        List<Connector> senders = connectors.getSenders(context.getLocation());
        if (!senders.isEmpty()) {
            Message message = factory.createOrder(context, product, quantity, placerOrderNumber, date);
            manager.queue(message, senders);
        }
    }

    @Override
    public void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date) {
        List<Connector> senders = connectors.getSenders(context.getLocation());
        if (!senders.isEmpty()) {
            Message message = factory.updateOrder(context, product, quantity, placerOrderNumber, date);
            manager.queue(message, senders);
        }
    }

    @Override
    public void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date) {
        List<Connector> senders = connectors.getSenders(context.getLocation());
        if (!senders.isEmpty()) {
            Message message = factory.cancelOrder(context, product, quantity, placerOrderNumber, date);
            manager.queue(message, senders);
        }
    }
}
