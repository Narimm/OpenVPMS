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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.customer.charge.PopupEditorManager;
import org.openvpms.web.app.patient.charge.VisitChargeEditor;
import org.openvpms.web.app.patient.charge.VisitChargeItemRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
* A test {@link VisitChargeEditor}.
*
* @author Tim Anderson
*/
public class TestVisitChargeEditor extends VisitChargeEditor {

    /**
     * The visit editing task.
     */
    private TestEditVisitTask testEditVisitTask;

    /**
     * Constructs a {@code TestVisitChargeEditor}.
     *
     * @param testEditVisitTask the task
     * @param charge the charge to edit
     * @param event the visit to edit
     * @param context the layout context
     */
    public TestVisitChargeEditor(TestEditVisitTask testEditVisitTask, FinancialAct charge, Act event,
                                 LayoutContext context) {
        super(charge, event, context, false); // don't add a default item...
        this.testEditVisitTask = testEditVisitTask;
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public VisitChargeItemRelationshipCollectionEditor getItems() {
        return super.getItems();
    }

    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act,
                                                                CollectionProperty items) {
        VisitChargeItemRelationshipCollectionEditor result
                = new VisitChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
        result.setPopupEditorManager(new DelegatingPopupEditorManager());
        return result;
    }

    private class DelegatingPopupEditorManager implements PopupEditorManager {

        /**
         * Queues an editor for display.
         *
         * @param editor   the editor to queue
         * @param skip     if {@code true}, indicates that the edit can be skipped
         * @param listener the listener to notify on completion
         */
        public void queue(IMObjectEditor editor, boolean skip, Listener listener) {
            testEditVisitTask.getEditorManager().queue(editor, skip, listener);
        }

        /**
         * Determines if editing is complete.
         *
         * @return {@code true} if there are no more editors
         */
        public boolean isComplete() {
            return testEditVisitTask.getEditorManager().isComplete();
        }
    }
}
