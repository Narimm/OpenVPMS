package org.openvpms.web.component.app;

import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * Application context information.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Context {

    /**
     * The object being viewed/edited.
     */
    private IMObject _current;

    /**
     * The current customer.
     */
    private Party _customer;

    /**
     * The current patient.
     */
    private Party _patient;

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
    protected Context() {
    }

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               <code>null</code>
     */
    public void setCurrent(IMObject object) {
        _current = object;
    }

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or <code>null</code> if there is
     *         no current object
     */
    public IMObject getCurrent() {
        return _current;
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be <code>null</code>
     */
    public void setCustomer(Party customer) {
        _customer = customer;
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer, or <code>null</code> if there is no current
     *         customer
     */
    public Party getCustomer() {
        return _customer;
    }

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be <code>null</code>
     */
    public void setPatient(Party patient) {
        _patient = patient;
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient, or <code>null</code> if there is no current
     *         patient
     */
    public Party getPatient() {
        return _patient;
    }

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be <code>null</code>
     */
    public void setSupplier(Party supplier) {
        _supplier = supplier;
    }

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or <code>null</code> if there is no current
     *         supplier
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
     * @return the current product, or <code>null</code> if there is no current
     *         product
     */
    public Product getProduct() {
        return _product;
    }

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in <code>range</code> or
     *         <code>null</code> if none exists
     */
    public IMObject getObject(String[] range) {
        IMObject result = null;
        for (IMObject object : getObjects()) {
            if (object != null) {
                for (String shortName : range) {
                    ArchetypeId id = object.getArchetypeId();
                    if (DescriptorHelper.matches(id, shortName)) {
                        result = object;
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches <code>reference</code>,
     *         or <code>null</code> if there is no matches
     */
    public IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        for (IMObject object : getObjects()) {
            if (object != null) {
                ArchetypeId id = object.getArchetypeId();
                if (id.equals(reference.getArchetypeId())
                    && StringUtils.equals(reference.getLinkId(), object.getLinkId())) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the context associated with the current thread.
     *
     * @return the context associated with the current thread, or
     *         <code>null</code>
     */
    public static Context getInstance() {
        return ContextApplicationInstance.getInstance().getContext();
    }

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    protected IMObject[] getObjects() {
        return new IMObject[]{_current, _customer, _patient, _supplier,
                              _product};
    }

}
