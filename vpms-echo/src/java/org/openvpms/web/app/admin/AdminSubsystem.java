package org.openvpms.web.app.admin;

import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Administration subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class AdminSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>AdminSubsystem</code>.
     */
    public AdminSubsystem() {
        super("admin");
        addWorkspace(new OrganisationWorkspace());
        addWorkspace(new UserWorkspace());
        addWorkspace(new LookupWorkspace());
        addWorkspace(new ClassificationWorkspace());
        addWorkspace(new ArchetypeWorkspace());
    }
}
