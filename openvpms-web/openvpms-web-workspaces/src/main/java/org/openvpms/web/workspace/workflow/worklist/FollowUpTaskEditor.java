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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.ComponentState;
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
public class FollowUpTaskEditor extends RestrictedWorkListTaskEditor {

    /**
     * The available work lists, ordered on name.
     */
    private final List<Entity> workLists;

    /**
     * The default work list. May be {@code null}
     */
    private final Entity defaultWorkList;

    /**
     * Constructs a {@link FollowUpTaskEditor}.
     *
     * @param act       the act to edit
     * @param workLists the work lists for follow up tasks, in order of preference
     * @param context   the layout context
     */
    public FollowUpTaskEditor(Act act, List<Entity> workLists, LayoutContext context) {
        super(act, null, context);
        defaultWorkList = (!workLists.isEmpty()) ? workLists.get(0) : null;
        this.workLists = new ArrayList<>(workLists);
        IMObjectSorter.sort(workLists, "name");
        setStartTime(new Date());
        initWorkListEditor();
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
     * Returns a default work list.
     *
     * @return a default work list, or {@code null} if there is no default
     */
    @Override
    protected Entity getDefaultWorkList() {
        return defaultWorkList;
    }

    /**
     * Creates an editor to edit a work list participation.
     *
     * @param participation the participation to edit
     * @return a new editor
     */
    @Override
    protected ParticipationEditor<Entity> createWorkListEditor(Participation participation) {
        return new RestrictedWorkListParticipationEditor(participation, getObject(), getLayoutContext()) {
            @Override
            protected Query<Entity> createWorkListQuery(String name) {
                RestrictedWorkListQuery query = new RestrictedWorkListQuery(workLists);
                if (name != null) {
                    Entity entity = getEntity();
                    if (entity == null || !StringUtils.equals(entity.getName(), name)) {
                        query.setValue(name);
                    }
                }
                return query;
            }
        };
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        ArchetypeNodes nodes = new ArchetypeNodes(true, true).simple("worklist").order("worklist", "startTime")
                .order("taskType", "startTime").exclude("patient").hidden(true);
        DefaultLayoutStrategy strategy = new DefaultLayoutStrategy(nodes);
        strategy.addComponent(new ComponentState(getWorkListCollectionEditor()));
        strategy.addComponent(new ComponentState(getStartTimeEditor()));
        strategy.addComponent(new ComponentState(getEndTimeEditor()));
        return strategy;
    }

}
