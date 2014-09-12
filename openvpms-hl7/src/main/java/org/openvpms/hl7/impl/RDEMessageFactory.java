package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.RXO;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.PatientContext;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Factory for RDE (pharmacy order) messages.
 *
 * @author Tim Anderson
 */
public class RDEMessageFactory extends AbstractMessageFactory {


    /**
     * Dispensing units node.
     */
    private static final String DISPENSING_UNITS = "dispensingUnits";

    /**
     * Selling units node.
     */
    private static final String SELLING_UNITS = "sellingUnits";


    /**
     * Constructs an {@link RDEMessageFactory}.
     *
     * @param messageContext the message context
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public RDEMessageFactory(HapiContext messageContext, IArchetypeService service, ILookupService lookups) {
        super(messageContext, service, lookups);
    }

    public Message createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               Date date) {
        return createOrder(context, "NW", product, quantity, placerOrderNumber, date);
    }

    public Message updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               Date date) {
        return createOrder(context, "RP", product, quantity, placerOrderNumber, date);
    }


    public Message cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               Date date) {
        return createOrder(context, "CA", product, quantity, placerOrderNumber, date);
    }

    private RDE_O11 createOrder(PatientContext context, String orderControl, Product product, BigDecimal quantity,
                                long placerOrderNumber, Date date) {
        RDE_O11 rde;
        try {
            rde = new RDE_O11(getModelClassFactory());
            init(rde, "RDE", "O11");
            populate(rde.getPATIENT().getPID(), context);
            populate(rde.getPATIENT().getPATIENT_VISIT().getPV1(), context);
            ORC orc = rde.getORDER().getORC();
            orc.getOrderControl().setValue(orderControl);
            orc.getPlacerOrderNumber().getEntityIdentifier().setValue(Long.toString(placerOrderNumber));
            orc.getDateTimeOfTransaction().getTime().setValue(date);
            if (context.getClinicianId() != -1) {
                PopulateHelper.populateClinicianSegment(orc.getEnteredBy(0), context);
            }
            RXO rxo = rde.getORDER().getORDER_DETAIL().getRXO();
            // RXE rxe = rde.getORDER().getRXE();
            PopulateHelper.populateProduct(rxo.getRequestedGiveCode(), product);
            // populateCE(rxe.getGiveCode(), product.getId(), product.getName());
            IMObjectBean bean = new IMObjectBean(product, getArchetypeService());
            String dispensingCode = bean.getString(DISPENSING_UNITS);
            if (dispensingCode != null) {
                String dispensingName = getLookupService().getName(product, DISPENSING_UNITS);
                PopulateHelper.populateCE(rxo.getRequestedGiveUnits(), dispensingCode, dispensingName);
                // populateCE(rxe.getGiveUnits(), dispensingCode, dispensingName);
            }
            String sellingCode = bean.getString(SELLING_UNITS);
            String dispensingInstructions = bean.getString("dispInstructions");
            if (dispensingInstructions != null) {
                rxo.getProviderSAdministrationInstructions(0).getText().setValue(dispensingInstructions);
            }
            rxo.getRequestedDispenseAmount().setValue(quantity.toString());
            if (sellingCode != null) {
                String sellingName = getLookupService().getName(product, SELLING_UNITS);
                PopulateHelper.populateCE(rxo.getRequestedDispenseUnits(), sellingCode, sellingName);
            }
            populateAllergies(rde.getPATIENT(), context);
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
        return rde;
    }


}
