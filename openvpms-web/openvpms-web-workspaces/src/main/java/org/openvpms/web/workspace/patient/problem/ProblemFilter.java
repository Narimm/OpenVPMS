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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.act.ActHierarchyFilter;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Filters patient problems.
 * <p/>
 * This enables specific problems items to by included by archetype.
 *
 * @author Tim Anderson
 */
public class ProblemFilter extends ActHierarchyFilter<Act> {

    /**
     * The comparator to sort items.
     */
    private final Comparator<Act> comparator;

    /**
     * Act event nodes.
     */
    private static final String[] EVENT_NODES = new String[]{"event", "events"};

    /**
     * Constructs an {@link ProblemFilter}.
     *
     * @param shortNames the act short names
     * @param ascending  if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public ProblemFilter(String[] shortNames, boolean ascending) {
        super(shortNames, true);
        comparator = getComparator(ascending);
    }

    /**
     * Filters child acts.
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return the filtered acts
     */
    @Override
    protected List<Act> filter(Act parent, List<Act> children) {
        List<Act> result = new ArrayList<Act>();
        Map<Act, List<Act>> actsByEvent = new TreeMap<Act, List<Act>>(comparator);
        List<Act> actsWithoutEvents = new ArrayList<Act>();
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();  // cache of events

        if (!children.isEmpty()) {
            for (Act act : children) {
                ActBean bean = new ActBean(act);
                List<Act> actEvents = getEvents(bean, events);
                if (!actEvents.isEmpty()) {
                    for (Act event : actEvents) {
                        List<Act> list = actsByEvent.get(event);
                        if (list == null) {
                            list = new ArrayList<Act>();
                            actsByEvent.put(event, list);
                        }
                        list.add(act);
                    }
                } else {
                    actsWithoutEvents.add(act);
                }
            }
        }
        addActs(result, actsWithoutEvents);
        if (actsByEvent.isEmpty()) {
            // no events for child items, so add these from the problem
            ActBean bean = new ActBean(parent);
            addActs(result, getEvents(bean, events));
        } else {
            addActsByEvent(result, actsByEvent);
        }
        return result;
    }

    /**
     * Adds acts to the list, sorting them first.
     *
     * @param list the list to add to
     * @param acts the acts to add
     */
    private void addActs(List<Act> list, List<Act> acts) {
        if (acts.size() > 1) {
            Collections.sort(acts, comparator);
        }
        list.addAll(acts);
    }

    /**
     * Adds acts to a list, grouped by their events.
     *
     * @param list        the list to add to
     * @param actsByEvent a map of the events to their corresponding items
     */
    private void addActsByEvent(List<Act> list, Map<Act, List<Act>> actsByEvent) {
        for (Map.Entry<Act, List<Act>> entry : actsByEvent.entrySet()) {
            list.add(entry.getKey());
            List<Act> acts = entry.getValue();
            addActs(list, acts);
        }
    }


    /**
     * Returns the events associated with an act.
     *
     * @param bean   the act bean
     * @param events the event cache
     * @return the corresponding event, or {@code null} if none is found
     */
    private List<Act> getEvents(ActBean bean, Map<IMObjectReference, Act> events) {
        List<Act> result = new ArrayList<Act>();
        List<IMObjectReference> refs = Collections.emptyList();
        for (String node : EVENT_NODES) {
            if (bean.hasNode(node)) {
                refs = bean.getNodeSourceObjectRefs(node);
                break;
            }
        }
        for (IMObjectReference ref : refs) {
            Act event = events.get(ref);
            if (event == null) {
                event = (Act) IMObjectHelper.getObject(ref, null);
                if (event != null) {
                    events.put(ref, event);
                }
            }
            if (event != null) {
                result.add(event);
            }
        }
        return result;
    }

}
