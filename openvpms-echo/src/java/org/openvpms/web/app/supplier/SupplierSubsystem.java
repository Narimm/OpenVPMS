package org.openvpms.web.app.supplier;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Supplier subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class SupplierSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>SupplierSubsystem</code>.
     */
    public SupplierSubsystem() {
        super("supplier");
        addWorkspace(new DummyWorkspace("supplier.info"));
        addWorkspace(new DummyWorkspace("supplier.document"));
        addWorkspace(new DummyWorkspace("supplier.order"));
        addWorkspace(new DummyWorkspace("supplier.charge"));
        addWorkspace(new DummyWorkspace("supplier.payment"));
        addWorkspace(new DummyWorkspace("supplier.account"));
    }
}
