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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.property.Property;

/**
 * Edit layout strategy for <em>act.patientInvestigationVersion</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientInvestigationVersionActLayoutStrategy extends PatientDocumentVersionActLayoutStrategy {

    /**
     * Constructs an {@link PatientInvestigationVersionActLayoutStrategy}.
     *
     * @param editor the editor. May be {@code null}
     * @param locked determines if the record is locked
     */
    public PatientInvestigationVersionActLayoutStrategy(DocumentEditor editor, boolean locked) {
        super(editor, locked);
    }

    /**
     * Determines if a property should be made read-only when the act is locked.
     *
     * @param property the property
     * @return {@code true} if the property should be made read-only
     */
    @Override
    protected boolean makeReadOnly(Property property) {
        return !property.isReadOnly() && !property.getName().equals("reviewed");
    }

}
