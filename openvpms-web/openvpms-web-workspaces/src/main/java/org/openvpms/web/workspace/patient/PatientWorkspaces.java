/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.component.workspace.Workspace;
import org.openvpms.web.workspace.patient.info.InformationWorkspace;
import org.openvpms.web.workspace.patient.mr.PatientRecordWorkspace;
import org.openvpms.web.workspace.patient.mr.RecordBrowser;


/**
 * Patient workspaces.
 *
 * @author Tim Anderson
 */
public class PatientWorkspaces extends AbstractWorkspaces {

    /**
     * The records workspace.
     */
    private final PatientRecordWorkspace records;

    /**
     * Constructs a {@link PatientWorkspaces}.
     *
     * @param context     the context
     * @param preferences the user preferences
     */
    public PatientWorkspaces(Context context, Preferences preferences) {
        super("patient");
        addWorkspace(new InformationWorkspace(context, preferences));
        records = new PatientRecordWorkspace(context, preferences);
        addWorkspace(records);
    }

    /**
     * Returns the first workspace that can handle a particular archetype.
     * This implementation returns the {@link PatientRecordWorkspace} in
     * preference to the {@link InformationWorkspace}.
     *
     * @param shortName the archetype's short name.
     * @return a workspace that supports the specified archetype or {@code null} if no workspace supports it
     */
    @Override
    public Workspace getWorkspaceForArchetype(String shortName) {
        Workspace result = null;
        if (InsuranceArchetypes.POLICY.equals(shortName)) {
            result = records;
            records.getComponent();
            RecordBrowser browser = records.getBrowser();
            if (browser != null) {
                browser.showInsurance();
            }
        } else {
            for (Workspace workspace : getWorkspaces()) {
                if (workspace.canUpdate(shortName)) {
                    if (workspace instanceof PatientRecordWorkspace) {
                        result = workspace;
                        break;
                    } else {
                        result = workspace;
                    }
                }
            }
        }
        return result;
    }

}
