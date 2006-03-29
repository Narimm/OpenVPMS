package org.openvpms.web.app.customer;

import java.util.List;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Invoice workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InvoiceWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>InvoiceWorkspace</code>.
     */
    public InvoiceWorkspace() {
        super("customer", "invoice", "party", "party", "customer*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("customer.invoice.createtype");
        return new InvoiceCRUDWindow(type, "common", "act", "customer*");
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor("act.customerInvoice");
        NodeDescriptor descriptor = archetype.getNodeDescriptor("status");
        ILookupService lookup = ServiceHelper.getLookupService();
        List<Lookup> lookups = lookup.get(descriptor);
        return new ActQuery(customer, "act", "customer*", lookups, "Posted");
    }

}
