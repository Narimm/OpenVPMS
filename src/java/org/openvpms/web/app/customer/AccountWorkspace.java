package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.resource.util.Messages;


/**
 * Invoice workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class AccountWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>InvoiceWorkspace</code>.
     */
    public AccountWorkspace() {
        super("customer", "account", "party", "party", "customer*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("customer.account.createtype");
        return new AccountCRUDWindow(type, "common", "act", "customerAccount*");
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        return new ActQuery(customer, "act", "customerAccountCharges*",
                            "Posted");
    }

}
