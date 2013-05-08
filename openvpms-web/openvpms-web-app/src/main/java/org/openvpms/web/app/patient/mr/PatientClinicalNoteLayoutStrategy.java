/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentSet;


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
     * @return the note component, or {@code null} if none is found
     */
    @Override
    protected Component getDefaultFocus(ComponentSet components) {
        return components.getFocusable("note");
    }
}
