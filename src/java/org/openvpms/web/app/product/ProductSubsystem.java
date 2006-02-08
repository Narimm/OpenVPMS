package org.openvpms.web.app.product;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Product subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class ProductSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>ProductSubsystem</code>.
     */
    public ProductSubsystem() {
        super("product");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("product", "pricing"));
        addWorkspace(new DummyWorkspace("product", "inventory"));
    }
}
