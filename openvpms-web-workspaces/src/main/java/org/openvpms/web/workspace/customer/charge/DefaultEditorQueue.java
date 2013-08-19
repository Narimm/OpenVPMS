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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.mr.PatientMedicationActEditor;

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
     * Constructs a {@link DefaultEditorQueue}.
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
     * @param cancel   if {@code true}, indicates that the edit can be cancelled
     * @param listener the listener to notify on completion
     */
    public void queue(IMObjectEditor editor, boolean skip, boolean cancel, Listener listener) {
        queue(new EditorState(editor, listener, skip, cancel));
    }

    /**
     * Queues a dialog.
     *
     * @param dialog the dialog to queue
     */
    @Override
    public void queue(PopupDialog dialog) {
        queue(new DialogState(dialog));
    }

    /**
     * Creates and edits the next act, if any.
     */
    protected void editNext() {
        editing = false;
        if (queue.isEmpty()) {
            completed();
            return;
        }
        State state = queue.removeFirst();
        state.show();
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
        show(dialog);
    }

    /**
     * Displays a dialog.
     *
     * @param dialog the dialog
     */
    protected void prompt(PopupDialog dialog) {
        show(dialog);
    }

    /**
     * Displays a dialog.
     *
     * @param dialog the dialog to display
     */
    protected void show(PopupDialog dialog) {
        editing = true;
        dialog.show();
    }

    /**
     * Invoked when an edit is skipped. Performs the next edit, if any.
     */
    protected void skipped() {
        editNext();
    }

    /**
     * Invoked when the edit is completed.
     */
    protected void completed() {
        editing = false;
    }

    /**
     * Invoked when an edit is cancelled. Skips all subsequent edits.
     */
    protected void cancelled() {
        while (!queue.isEmpty()) {
            State state = queue.removeFirst();
            if (state instanceof EditorState) {
                ((EditorState) state).skip();
            }
        }
        editing = false;
    }

    /**
     * Queues a state.
     *
     * @param state the state
     */
    private void queue(State state) {
        queue.addLast(state);
        if (!editing) {
            editNext();
        }
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
                PatientRules rules = ServiceHelper.getBean(PatientRules.class);
                String name = patient.getName();
                String weight = rules.getPatientWeight(patient);
                if (weight == null) {
                    weight = Messages.get("patient.noweight");
                }
                title = Messages.format("patient.medication.dialog.title", title, name, weight);
            }
        }
        return title;
    }

    private interface State {

        void show();
    }

    private class EditorState implements State {

        final IMObjectEditor editor;
        final Listener listener;
        final boolean skip;
        final boolean cancel;

        public EditorState(IMObjectEditor editor, Listener listener, boolean skip, boolean cancel) {
            this.editor = editor;
            this.listener = listener;
            this.skip = skip;
            this.cancel = cancel;
        }

        @Override
        public void show() {
            EditDialog dialog = new EditDialog(editor, false, false, cancel, skip, context);
            dialog.setTitle(getTitle(editor));
            dialog.setStyleName("ChildEditDialog");
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    doNext(false, false);
                }

                @Override
                public void onCancel() {
                    doNext(false, true);
                }

                @Override
                public void onSkip() {
                    doNext(true, false);
                }

                private void doNext(boolean skipped, boolean cancelled) {
                    editing = false;
                    if (listener != null) {
                        listener.completed(skipped, cancelled);
                    }
                    if (skipped) {
                        skipped();
                    } else if (cancelled) {
                        cancelled();
                    } else {
                        editNext();
                    }
                }
            });
            edit(dialog);
        }

        public void skip() {
            listener.completed(true, false);
        }
    }

    private class DialogState implements State {

        private final PopupDialog dialog;

        private WindowPaneListener listener;

        public DialogState(PopupDialog dialog) {
            this.dialog = dialog;
            listener = new WindowPaneListener() {
                @Override
                public void windowPaneClosing(WindowPaneEvent e) {
                    onClose();
                }
            };
        }

        @Override
        public void show() {
            dialog.addWindowPaneListener(listener);
            prompt(dialog);
        }

        private void onClose() {
            dialog.removeWindowPaneListener(listener);
            editNext();
        }
    }


}
