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

package org.openvpms.web.app.customer.charge;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.mr.PatientMedicationActEditor;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.LinkedList;


/**
 * Helper to queue editing of patient medication, investigation and reminder popups, only showing one dialog at a time.
 *
 * @author Tim Anderson
 */
public class DefaultEditorQueue implements EditorQueue {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The queue of editors.
     */
    private LinkedList<State> queue = new LinkedList<State>();

    /**
     * Determines if an edit is in progress.
     */
    private boolean editing;

    /**
     * Constructs a {@code DefaultEditorQueue}.
     *
     * @param context the context
     */
    public DefaultEditorQueue(Context context) {
        this.context = context;
    }

    /**
     * Queue an edit.
     *
     * @param editor   the editor to queue
     * @param skip     if {@code true}, indicates that the edit can be skipped
     * @param listener the listener to notify on completion
     */
    public void queue(IMObjectEditor editor, boolean skip, Listener listener) {
        queue.addLast(new State(editor, listener, skip));
        if (!editing) {
            editNext();
        }
    }

    /**
     * Creates and edits the next act, if any.
     */
    protected void editNext() {
        if (queue.isEmpty()) {
            editCompleted();
            return;
        }
        State state = queue.removeFirst();
        final IMObjectEditor editor = state.editor;
        final Listener listener = state.listener;
        // create an edit dialog with OK and (for non-medication acts) Skip buttons
        EditDialog dialog = new EditDialog(editor, false, false, false, state.skip, context);
        dialog.setTitle(getTitle(editor));
        dialog.setStyleName("ChildEditDialog");
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doNext(false);
            }

            @Override
            public void onSkip() {
                doNext(true);
            }

            private void doNext(boolean skipped) {
                editing = false;
                listener.completed(skipped);
                editNext();
            }

        });
        editing = true;
        edit(dialog);
    }

    /**
     * Determines if editing is complete.
     *
     * @return {@code true} if there are no more popups
     */
    public boolean isComplete() {
        return !editing && queue.isEmpty();
    }

    /**
     * Displays an edit dialog.
     *
     * @param dialog the dialog
     */
    protected void edit(EditDialog dialog) {
        dialog.show();
    }

    /**
     * Invoked when the edit is completed.
     */
    protected void editCompleted() {
        editing = false;
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
                PatientRules rules = new PatientRules(ServiceHelper.getArchetypeService(),
                                                      ServiceHelper.getLookupService());
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
     * Helper to associate an {@link IMObjectEditor} and {@link Listener}.
     */
    private static class State {
        final IMObjectEditor editor;
        final Listener listener;
        final boolean skip;

        public State(IMObjectEditor editor, Listener listener, boolean skip) {
            this.editor = editor;
            this.listener = listener;
            this.skip = skip;
        }
    }

}
