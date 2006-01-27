package org.openvpms.web.app;

import org.openvpms.component.business.domain.im.party.Party;


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
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static Context getInstance() {
        return OpenVPMSApp.getInstance().getContext();
    }
}
