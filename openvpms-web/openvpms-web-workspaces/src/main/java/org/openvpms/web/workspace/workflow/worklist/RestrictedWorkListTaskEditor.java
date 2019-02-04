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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * A task editor that restricts the available work lists using a {@link ParticipationEditor}
 * provided by subclasses.
 *
 * @author Tim Anderson
 */
public abstract class RestrictedWorkListTaskEditor extends AbstractTaskActEditor {

    /**
     * The work list participation editor.
     */
    private SingleParticipationCollectionEditor workListEditor;

    /**
     * Constructs a {@link RestrictedWorkListTaskEditor}.
     *
     * @param act     the task
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    protected RestrictedWorkListTaskEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Initialises the work list editor.
     * <p/>
     * Subclasses must invoke this before using the editor.
     */
    protected void initWorkListEditor() {
        workListEditor = createWorkListCollectionEditor();
        addEditor(workListEditor);
        if (getWorkList() == null) {
            Entity defaultWorkList = getDefaultWorkList();
            if (defaultWorkList != null) {
                setWorkList(defaultWorkList);
            }
        }
        workListEditor.addModifiableListener(modifiable -> onWorkListChanged());
    }

    /**
     * Returns a default work list.
     *
     * @return a default work list, or {@code null} if there is no default
     */
    protected Entity getDefaultWorkList() {
        return null;
    }

    /**
     * Creates an editor to edit a work list participation.
     *
     * @param participation the participation to edit
     * @return a new editor
     */
    protected abstract ParticipationEditor<Entity> createWorkListEditor(Participation participation);


    /**
     * Invoked to update the task type when the work list changes.
     */
    protected void onWorkListChanged() {
        getTaskTypeEditor().setWorkList(getWorkList());
    }

    /**
     * Returns the work list editor.
     *
     * @return the work list editor
     */
    protected SingleParticipationCollectionEditor getWorkListCollectionEditor() {
        return workListEditor;
    }

    /**
     * Creates the work list editor.
     *
     * @return a new work-list editor
     */
    protected SingleParticipationCollectionEditor createWorkListCollectionEditor() {
        CollectionProperty property = getCollectionProperty("worklist");
        return new SingleParticipationCollectionEditor(property, getObject(), getLayoutContext()) {
            @Override
            protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
                return createWorkListEditor((Participation) object);
            }
        };
    }
}
