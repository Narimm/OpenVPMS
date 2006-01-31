package org.openvpms.web.app;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
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
     * The current supplier.
     */
    private Party _supplier;

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
     * Sets the current supplier.
     *
     * @param supplier the current supplier
     */
    public void setSupplier(Party supplier) {
        _supplier = supplier;
    }

    /**
     * Returns the current suppller.
     *
     * @return the current supplier
     */
    public Party getSupplier() {
        return _supplier;
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
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return the first context object with an archetype id in
     *         <code>range</code>, or <code>null</code> if there is no match
     */
    public IMObject getObject(String[] range) {
        IMObject result = null;
        IMObject[] objects = new IMObject[]{_customer, _supplier, _product};
        for (String shortName : range) {
            for (IMObject object : objects) {
                if (object != null) {
                    ArchetypeId id = object.getArchetypeId();
                    if (id.getShortName().equals(shortName)) {
                        result = object;
                        break;
                    }
                }
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches <code>reference</code>,
     *         or <code>null</code> if there is no match
     */
    public IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        IMObject[] objects = new IMObject[]{_customer, _supplier, _product};
        for (IMObject object : objects) {
            if (object != null) {
                ArchetypeId id = object.getArchetypeId();
                if (id.equals(reference.getArchetypeId())
                        && reference.getUid() == object.getUid()) {
                    result = object;
                    break;
                }
            }
        }
        return result;
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
