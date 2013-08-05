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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.patientClinicalProblem</em>.
 * This prevents the editing of items nodes in 'visits view'.
 *
 * @author Tim Anderson
 */
public class PatientClinicalProblemActEditor extends ActEditor {

    /**
     * Constructs a {@link PatientClinicalProblemActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context. May be {@code null}.
     */
    public PatientClinicalProblemActEditor(Act act, Act parent, LayoutContext context) {
        this(act, parent, (parent == null), context);
        // disable editing of the items node if there is a parent act.
    }

    /**
     * Constructs a new {@code PatientClinicalProblemActEditor}.
     *
     * @param act       the act to edit
     * @param parent    the parent act. May be {@code null}
     * @param editItems if {@code true} create an editor for any items node
     * @param context   the layout context. May be {@code null}.
     */
    public PatientClinicalProblemActEditor(Act act, Act parent, boolean editItems, LayoutContext context) {
        super(act, parent, editItems, context);
        // disable editing of the items node if there is a parent act.

        initParticipant("patient", context.getContext().getPatient());

        addStartEndTimeListeners();

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
    }

    /**
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        return new Items(items, act);
    }

    /**
     * Invoked when the status changes. Sets the end time to today if the
     * status is 'RESOLVED', otherwise {@code null}.
     */
    private void onStatusChanged() {
        Property status = getProperty("status");
        String value = (String) status.getValue();
        Date time = "RESOLVED".equals(value) ? new Date() : null;
        setEndTime(time, false);
    }

    private class Items extends ActRelationshipCollectionEditor {
        public Items(CollectionProperty items, Act act) {
            super(items, act, PatientClinicalProblemActEditor.this.getLayoutContext());
        }

        /**
         * Invoked when the "New" button is pressed. This implementation prompts for confirmation if the object to
         * be created is a Medication.
         */
        @Override
        protected void onNew() {
            if (PatientArchetypes.PATIENT_MEDICATION.equals(getShortName())) {
                ConfirmationDialog dialog = new ConfirmationDialog(
                        Messages.get("patient.record.create.medication.title"),
                        Messages.get("patient.record.create.medication.message"),
                        getHelpContext().subtopic("newMedication"));
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        Items.super.onNew();
                    }
                });
                dialog.show();
            } else {
                super.onNew();
            }

        }
    }
}
