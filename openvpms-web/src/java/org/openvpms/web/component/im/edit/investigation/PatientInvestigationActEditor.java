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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.edit.investigation;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.app.patient.document.PatientDocumentActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * An editor for <em>act.patientInvestigation</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientInvestigationActEditor extends PatientDocumentActEditor {

    /**
     * Determines if the button to print the form should be enabled.
     */
    private boolean enableButton;

    /**
     * Creates a new <tt>PatientInvestigationActEditor</tt>.
     *
     * @param act     the act
     * @param parent  the parent act. May be <tt>null</tt>
     * @param context the layout context
     */
    public PatientInvestigationActEditor(DocumentAct act, Act parent, LayoutContext context) {
        super(act, parent, context);
        enableButton = !act.isNew();
    }

    /**
     * Updates the investigation type, if it is not the same as the existing one.
     * On update, the description will be set to the description of the investigation type.
     *
     * @param investigationType the investigation type. May be <tt>null</tt>
     */
    public void setInvestigationType(Entity investigationType) {
        IMObjectReference current = getParticipantRef("investigationType");
        if (!ObjectUtils.equals(current, investigationType)) {
            setParticipant("investigationType", investigationType);
            if (investigationType != null) {
                Property description = getProperty("description");
                description.setValue(investigationType.getDescription());
            }
        }
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    public boolean save() {
        boolean isNew = getObject().isNew();
        boolean saved = super.save();
        if (saved && isNew) {
            // only enable printing of the form if the act has been saved
            enableButton = true;
            onLayout();
        }
        return saved;
    }

    /**
     * Returns the investigation type.
     *
     * @return the investigation type. May be <tt>null</tt>
     */
    public Entity getInvestigationType() {
        return (Entity) getParticipant("investigationType");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        PatientInvestigationActLayoutStrategy strategy = new PatientInvestigationActLayoutStrategy(getDocumentEditor(),
                                                                                                   getVersionsEditor());
        strategy.setEnableButton(enableButton);
        return strategy;
    }

}
