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

package org.openvpms.web.component.im.invoice;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientMedicationActEditor;
import org.openvpms.web.component.im.edit.medication.PatientMedicationActLayoutStrategy;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.layout.EditLayoutStrategyFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.LinkedList;


/**
 * Helper to queue editing of patient medication popups, only showing one
 * dialog at a time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class MedicationManager {

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
     * Layout strategy factory that returns customized instances of
     * {@link PatientMedicationActLayoutStrategy}.
     */
    private static final IMObjectLayoutStrategyFactory FACTORY
            = new MedicationLayoutStrategyFactory();


    /**
     * Queue an edit.
     *
     * @param editor   the medication act collection editor
     * @param listener the listener to notify on completion
     */
    public void queue(ActRelationshipCollectionEditor editor,
                      Listener listener) {
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
        final ActRelationshipCollectionEditor collectionEditor = pair.editor;
        final Listener listener = pair.listener;
        IMObject object = collectionEditor.create();
        if (object != null) {
            LayoutContext context = new DefaultLayoutContext(true);
            context.setLayoutStrategyFactory(FACTORY);
            final IMObjectEditor editor = collectionEditor.createEditor(
                    object, context);
            final EditDialog dialog = new EditDialog(editor, false);
            dialog.setTitle(getTitle(editor));
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    editing = false;
                    if (EditDialog.OK_ID.equals(dialog.getAction())) {
                        collectionEditor.addEdited(editor);
                        listener.completed();
                        editNext();
                    } else {
                        listener.completed();
                        cancel();
                    }
                }

            });
            editing = true;
            dialog.show();
        } else {
            listener.completed();
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
            PatientMedicationActEditor meditor
                    = (PatientMedicationActEditor) editor;
            Party patient = (Party) IMObjectHelper.getObject(
                    meditor.getPatient());
            if (patient != null) {
                PatientRules rules = new PatientRules();
                String name = patient.getName();
                String weight = rules.getPatientWeight(patient);
                if (weight == null) {
                    weight = Messages.get("patient.noweight");
                }
                title = Messages.get("patient.medication.dialog.title",
                                     title, name, weight);
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
     * Helper to associate an {@link ActRelationshipCollectionEditor} and
     * {@link Listener}.
     */
    private static class Pair {
        final ActRelationshipCollectionEditor editor;
        final Listener listener;

        public Pair(ActRelationshipCollectionEditor editor,
                    Listener listener) {
            this.editor = editor;
            this.listener = listener;
        }
    }

    /**
     * Factory that invokes <code>setProductReadOnly(true)</code> on
     * {@link PatientMedicationActLayoutStrategy} instances.
     */
    private static class MedicationLayoutStrategyFactory
            extends EditLayoutStrategyFactory {

        /**
         * Creates a new layout strategy for an object.
         *
         * @param object the object to create the layout strategy for
         * @param parent the parent object. May be <code>null</code>
         */
        @Override
        public IMObjectLayoutStrategy create(IMObject object, IMObject parent) {
            IMObjectLayoutStrategy result = super.create(object, parent);
            if (result instanceof PatientMedicationActLayoutStrategy) {
                PatientMedicationActLayoutStrategy strategy
                        = ((PatientMedicationActLayoutStrategy) result);
                strategy.setProductReadOnly(true);
            }
            return result;
        }

    }
}
