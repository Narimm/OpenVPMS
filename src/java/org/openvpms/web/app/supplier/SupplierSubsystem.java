package org.openvpms.web.app.supplier;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Supplier subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class SupplierSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>SupplierSubsystem</code>.
     */
    public SupplierSubsystem() {
        super("supplier");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("supplier", "document"));
        addWorkspace(new DummyWorkspace("supplier", "order"));
        addWorkspace(new DummyWorkspace("supplier", "charge"));
        addWorkspace(new DummyWorkspace("supplier", "payment"));
        addWorkspace(new DummyWorkspace("supplier", "account"));
    }
}
