package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.view.ComponentState;

import java.util.List;


/**
 * Layout strategy for <em>act.patientClinicalNote</em>.
 *
 * @author Tim Anderson
 */
public class PatientClinicalNoteLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Returns the default focus component.
     * <p/>
     * This implementation returns the note component.
     *
     * @param components the components
     * @return the note component, or <tt>null</tt> if none is found
     */
    @Override
    protected Component getDefaultFocus(List<ComponentState> components) {
        return getFocusable(components, "note");
    }
}
