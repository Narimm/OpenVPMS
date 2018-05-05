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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Charge context, used to defer manipulation of relationships to patient history until the charge items are saved.
 * <p>
 * At save, the approach is:
 * <ol>
 * <li>save charge</li>
 * <li>save charge items</li>
 * <li>save charge item relationships to events. This may have updated the charge items again, so these are re
 * -saved</li>
 * <li>delete any investigations, reminders, documents</li>
 * </ol>
 *
 * @author Tim Anderson
 */
public class ChargeSaveContext implements CollectionPropertyEditor.RemoveHandler {

    /**
     * The patient history changes. These exist only as long as a save is in progress.
     */
    private PatientHistoryChanges changes;

    /**
     * The objects to remove.
     */
    private List<IMObject> toRemove = new ArrayList<>();

    /**
     * The editors to remove.
     */
    private List<IMObjectEditor> toRemoveEditors = new ArrayList<>();

    /**
     * Default constructor.
     */
    public ChargeSaveContext() {
        super();
    }

    /**
     * Registers the patient history changes for the current save.
     *
     * @param changes the changes. May be {@code null}
     */
    public void setHistoryChanges(PatientHistoryChanges changes) {
        this.changes = changes;
    }

    /**
     * Returns the patient history changes for the current save.
     *
     * @return the changes. May be {@code null}
     */
    public PatientHistoryChanges getHistoryChanges() {
        return changes;
    }

    /**
     * Invoked to remove an object.
     * <p>
     * Removal is deferred until {@link #save()} is invoked.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        toRemove.add(object);
    }

    /**
     * Invoked to remove an object.
     * <p>
     * Removal is deferred until {@link #save()} is invoked.
     *
     * @param editor the object editor
     */
    @Override
    public void remove(IMObjectEditor editor) {
        toRemoveEditors.add(editor);
    }

    /**
     * Saves changes.
     *
     * @throws OpenVPMSException if the save fails
     */
    public void save() {
        changes.save();
        for (IMObject object : toRemove.toArray(new IMObject[toRemove.size()])) {
            IArchetypeRuleService service = ServiceHelper.getArchetypeService();
            service.remove(object);
            toRemove.remove(object);
        }
        for (IMObjectEditor editor : toRemoveEditors.toArray(new IMObjectEditor[toRemoveEditors.size()])) {
            editor.delete();
            toRemoveEditors.remove(editor);
        }
    }
}
