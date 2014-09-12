package org.openvpms.hl7;

import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public interface PharmacyOrderService {

    void createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date);

    void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date);

    void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date);

}