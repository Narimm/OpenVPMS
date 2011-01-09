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
package org.openvpms.web.app.patient.mr;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
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
     * Flag to indicate if the Print Form button should be enabled or disabled.
     */
    Boolean enableButton;

    /**
     * Creates a new <tt>PatientInvestigationActEditor</tt>.
     *
     * @param act     the act
     * @param parent  the parent act. May be <tt>null</tt>
     * @param context the layout context
     */
    public PatientInvestigationActEditor(DocumentAct act, Act parent, LayoutContext context) {
        super(act, parent, context);
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
            // enable printing of the form if the act has been saved and was previously unsaved
            enableButton = true; // getObject().isNew() will be true until transaction commits, so need this flag
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
     * Sets the patient.
     *
     * @param patient the patient reference. May be <tt>null</tt>
     */
    public void setPatient(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician reference. May be <tt>null</tt>.
     */
    public void setClinician(IMObjectReference clinician) {
        setParticipant("clinician", clinician);
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
        strategy.setEnableButton(enablePrintForm());
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        // need to re-register editors as these are removed when the layout is performed.
        // TODO - not ideal.
        getEditors().add(getDocumentEditor());
        getEditors().add(getVersionsEditor());
    }

    /**
     * Determines if the Print Form button should be displayed.
     * <p/>
     * Notes:
     * <ul>
     * <li>enableButton cannot be set in the constructor as createLayoutStrategy() is invoked during construction,
     * by which time it is too late
     * <li>getObject().isNew() returns true until the transaction commits
     * </ul>
     *
     * @return <tt>true</tt> if it should be displayed, otherwise <tt>false</tt>
     */
    private boolean enablePrintForm() {
        return enableButton != null && enableButton || !getObject().isNew();
    }
}
