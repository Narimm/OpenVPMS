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

import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

/**
 * Helper to edit an identity and associate it with a patient.
 *
 * @author Tim Anderson
 */
public class PatientIdentityEditor {

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The identity.
     */
    private final EntityIdentity identity;

    /**
     * The context.
     */
    private Context context;

    /**
     * The help context.
     */
    private HelpContext help;

    /**
     * Constructs a {@link PatientIdentityEditor}.
     *
     * @param patient  the patient
     * @param identity the identity to edit
     * @param context  the context
     * @param help     the help context
     */
    public PatientIdentityEditor(Party patient, EntityIdentity identity, Context context, HelpContext help) {
        this.patient = patient;
        this.identity = identity;
        this.context = context;
        this.help = help;
    }

    /**
     * Creates a new editor.
     *
     * @param patient   the patient
     * @param shortName the entity identity archetype short name
     * @param context   the context
     * @param help      the help context
     * @return a new editor, or {@code null} if {@code shortName} is invalid
     */
    public static PatientIdentityEditor create(Party patient, String shortName, Context context, HelpContext help) {
        IMObject object = IMObjectCreator.create(shortName);
        if (object instanceof EntityIdentity) {
            return new PatientIdentityEditor(patient, (EntityIdentity) object, context, help);
        }
        return null;
    }

    /**
     * Returns an edit dialog, containing an editor for the identity.
     * <p/>
     * On save, the identity will be added to the patient.
     *
     * @param skip if {@code true}, display a skip button to skip editing
     * @return a new dialog
     */
    public EditDialog edit(boolean skip) {
        DefaultLayoutContext layout = new DefaultLayoutContext(context, help.topic(identity, "edit"));

        IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(identity, layout);
        EditDialog dialog = new EditDialog(editor, true, false, true, skip, context) {
            /**
             * Saves the current object.
             * <p/>
             * This implementation adds the identity to the patient, and saves it.
             *
             * @param editor the editor
             * @throws OpenVPMSException if the save fails
             */
            @Override
            protected void doSave(IMObjectEditor editor) {
                patient = IMObjectHelper.reload(patient); // make sure we have the latest instance
                if (patient == null) {
                    throw new IllegalStateException("Patient has been removed");
                }
                patient.addIdentity(identity);
                ServiceHelper.getArchetypeService().deriveValues(patient); // update description
                super.doSave(editor);
            }
        };
        dialog.setStyleName("ChildEditDialog");
        return dialog;
    }

    /**
     * Returns the patient.
     * <p/>
     * This will have the new identity, if the editor was saved.
     *
     * @return the patient
     */
    public Party getPatient() {
        return patient;
    }
}
