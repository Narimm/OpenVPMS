package org.openvpms.web.app;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;


/**
 * Application context information.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Context {

    /**
     * The current customer.
     */
    private Party _customer;

    /**
     * The current product.
     */
    private Product _product;


    /**
     * Restrict construction.
     */
    Context() {
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer
     */
    public void setCustomer(Party customer) {
        _customer = customer;
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer
     */
    public Party getCustomer() {
        return _customer;
    }

    /**
     * Sets the current product.
     *
     * @param product the current product.
     */
    public void setProduct(Product product) {
        _product = product;
    }

    /**
     * Returns the current product.
     *
     * @return the current product
     */
    public Product getProduct() {
        return _product;
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static Context getInstance() {
        return OpenVPMSApp.getInstance().getContext();
    }

}
