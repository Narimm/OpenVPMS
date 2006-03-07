package org.openvpms.web.app.patient;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Patient sybsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PatientSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>PatientSubsystem</code>.
     */
    public PatientSubsystem() {
        super("patient");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("patient", "document"));
        addWorkspace(new DummyWorkspace("patient", "dispensing"));
        addWorkspace(new DummyWorkspace("patient", "record"));
        addWorkspace(new DummyWorkspace("patient", "reminder"));
        addWorkspace(new DummyWorkspace("patient", "investigation"));
    }
}
