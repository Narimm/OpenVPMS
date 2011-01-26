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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.charge;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.app.patient.mr.PatientMedicationActEditor;
import org.openvpms.web.resource.util.Messages;

import java.util.LinkedList;


/**
 * Helper to queue editing of patient medication, investigation and reminder popups, only showing one
 * dialog at a time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
class PatientActEditorManager {

    /**
     * Listener to notify completion of the edit.
     */
    interface Listener {

        /**
         * Invoked when the edit is complete.
         */
        void completed();
    }

    /**
     * The queue of editors.
     */
    private LinkedList<Pair> queue = new LinkedList<Pair>();

    /**
     * Determines if an edit is in progress.
     */
    private boolean editing;


    /**
     * Queue an edit.
     *
     * @param editor   the medication act editor
     * @param listener the listener to notify on completion
     */
    public void queue(IMObjectEditor editor, Listener listener) {
        queue.addLast(new Pair(editor, listener));
        if (!editing) {
            editNext();
        }
    }

    /**
     * Creates and edits the next act, if any.
     */
    private void editNext() {
        if (queue.isEmpty()) {
            return;
        }
        Pair pair = queue.removeFirst();
        IMObjectEditor editor = pair.editor;
        final Listener listener = pair.listener;
        // create an edit dialog with OK and Skip buttons 
        EditDialog dialog = new EditDialog(editor, false, false, false, true);
        dialog.setTitle(getTitle(editor));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                editing = false;
                listener.completed();
                editNext();
            }

            @Override
            public void onSkip() {
                editing = false;
                listener.completed();
                cancel();
            }

        });
        editing = true;
        dialog.show();
    }

    /**
     * Returns a title for the edit dialog.
     *
     * @param editor the editor
     * @return a title for the edit dialog
     */
    private String getTitle(IMObjectEditor editor) {
        String title = editor.getTitle();
        if (editor instanceof PatientMedicationActEditor) {
            PatientMedicationActEditor meditor = (PatientMedicationActEditor) editor;
            Party patient = meditor.getPatient();
            if (patient != null) {
                PatientRules rules = new PatientRules();
                String name = patient.getName();
                String weight = rules.getPatientWeight(patient);
                if (weight == null) {
                    weight = Messages.get("patient.noweight");
                }
                title = Messages.get("patient.medication.dialog.title", title, name, weight);
            }
        }
        return title;
    }

    /**
     * Cancel all remaining edits, notifying the listeners.
     */
    private void cancel() {
        for (Pair pair : queue) {
            pair.listener.completed();
        }
        queue.clear();
    }

    /**
     * Helper to associate an {@link IMObjectEditor} and {@link Listener}.
     */
    private static class Pair {
        final IMObjectEditor editor;
        final Listener listener;

        public Pair(IMObjectEditor editor, Listener listener) {
            this.editor = editor;
            this.listener = listener;
        }
    }

}
