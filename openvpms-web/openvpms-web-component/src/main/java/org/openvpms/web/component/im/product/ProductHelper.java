package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.Currency;
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
     * @param price    the price
     * @param currency the currency, used to round prices
     * @return the price
     */
    public static BigDecimal getPrice(ProductPrice price, BigDecimal serviceRatio, Currency currency) {
        BigDecimal result = price.getPrice();
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        if (!MathRules.equals(serviceRatio, ONE)) {
            result = currency.round(result.multiply(serviceRatio));
        }
        return result;
    }

}
