package org.openvpms.web.app.workflow;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Workflow subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class WorkflowSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>WorkflowSubsystem</code>.
     */
    public WorkflowSubsystem() {
        super("workflow");
        addWorkspace(new DummyWorkspace("workflow", "scheduling"));
        addWorkspace(new DummyWorkspace("workflow", "worklist"));
    }
}
