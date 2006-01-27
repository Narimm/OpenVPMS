package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Administration subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class AdminSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>AdminSubsystem</code>.
     */
    public AdminSubsystem() {
        super("admin");
        addWorkspace(new DummyWorkspace("admin.organisation"));
        addWorkspace(new UserWorkspace());
        addWorkspace(new RoleWorkspace());
        addWorkspace(new LookupWorkspace());
        addWorkspace(new ClassificationWorkspace());
        addWorkspace(new ArchetypeWorkspace());
    }
}
