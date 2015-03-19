package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;

/**
 * Product helper methods.
 *
 * @author Tim Anderson
 */
public class ProductHelper {

    /**
     * Returns the price for a product price, multiplied by the service ratio if there is one.
     *
     * @param price the price
     * @return the price
     */
    public static BigDecimal getPrice(ProductPrice price, BigDecimal serviceRatio) {
        BigDecimal result = price.getPrice();
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        if (!MathRules.equals(serviceRatio, ONE)) {
            result = MathRules.round(result.multiply(serviceRatio));
        }
        return result;
    }

}
