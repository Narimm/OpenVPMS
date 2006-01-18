package org.openvpms.web.app.customer;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Customer subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class CustomerSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>CustomerSubsystem</code>.
     */
    public CustomerSubsystem() {
        super("customer");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("customer.document"));
        addWorkspace(new DummyWorkspace("customer.estimation"));
        addWorkspace(new DummyWorkspace("customer.charge"));
        addWorkspace(new DummyWorkspace("customer.payment"));
        addWorkspace(new DummyWorkspace("customer.account"));
    }
}
