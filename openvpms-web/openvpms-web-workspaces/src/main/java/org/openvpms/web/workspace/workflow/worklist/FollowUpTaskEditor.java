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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A task editor that enables the work list to be selected, for the purposes of follow-up tasks.
 *
 * @author Tim Anderson
 */
public class FollowUpTaskEditor extends AbstractTaskActEditor {

    /**
     * The work lists.
     */
    private final List<Entity> workLists;

    /**
     * The work list participation editor.
     */
    private final SingleParticipationCollectionEditor workListEditor;

    /**
     * Constructs a {@link FollowUpTaskEditor}.
     *
     * @param act       the act to edit
     * @param workLists the work lists for follow up tasks, in order of preference
     * @param context   the layout context
     */
    public FollowUpTaskEditor(Act act, List<Entity> workLists, LayoutContext context) {
        super(act, null, context);
        this.workLists = new ArrayList<>(workLists); // copy as these will be ordered alphabetically
        workListEditor = createWorkListEditor();
        getEditors().add(workListEditor);
        initWorkList();

        setStartTime(new Date());
        workListEditor.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onWorkListChanged();
            }
        });
    }

    /**
     * Returns a display name for the object being edited.
     *
     * @return a display name for the object
     */
    @Override
    public String getDisplayName() {
        return Messages.get("patient.followup.task");
    }

    /**
     * Returns the follow-up work lists.
     *
     * @param context the context
     * @return the follow-up work lists
     */
    public static List<Entity> getWorkLists(Context context) {
        Set<Entity> matches = new LinkedHashSet<>();
        UserRules userRules = ServiceHelper.getBean(UserRules.class);
        LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
        User clinician = context.getClinician();
        User user = context.getUser();
        if (clinician != null) {
            matches.addAll(userRules.getFollowupWorkLists(clinician));
        }
        if (user != null && !ObjectUtils.equals(user, clinician)) {
            matches.addAll(userRules.getFollowupWorkLists(user));
        }
        Party location = context.getLocation();
        if (location != null) {
            matches.addAll(locationRules.getFollowupWorkLists(location));
        }
        return new ArrayList<>(matches);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        ArchetypeNodes nodes = new ArchetypeNodes(true, true).simple("worklist").order("worklist", "startTime")
                .order("taskType", "startTime");
        DefaultLayoutStrategy strategy = new DefaultLayoutStrategy(nodes);
        strategy.addComponent(new ComponentState(workListEditor));
        strategy.addComponent(new ComponentState(getStartTimeEditor()));
        strategy.addComponent(new ComponentState(getEndTimeEditor()));
        return strategy;
    }

    /**
     * Invoked to update the task type when the work list changes.
     */
    protected void onWorkListChanged() {
        getTaskTypeEditor().setWorkList(getWorkList());
    }

    /**
     * Creates the work list editor.
     *
     * @return a new work-list editor
     */
    private SingleParticipationCollectionEditor createWorkListEditor() {
        CollectionProperty property = getCollectionProperty("worklist");
        return new SingleParticipationCollectionEditor(property, getObject(), getLayoutContext()) {
            @Override
            protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
                return new FollowUpWorkListParticipationEditor((Participation) object, (Act) getObject(), getContext(),
                                                               workLists);
            }
        };
    }

    /**
     * Initialises the work list, if one isn't already set.
     */
    private void initWorkList() {
        if (!workLists.isEmpty() && getWorkList() == null) {
            Entity defaultWorkList = workLists.get(0);
            IMObjectSorter.sort(workLists, "name");
            setWorkList(defaultWorkList);
        }
    }

}
