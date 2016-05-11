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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Helper for working with calendar event series without loading the entire series.
 *
 * @author Tim Anderson
 */
public class CalendarEventSeriesState {

    /**
     * The event.
     */
    private final Act event;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The first act, if a series is present.
     */
    private final Act first;

    /**
     * The series, or {@code null} if the event isn't associated with a series.
     */
    private final Act series;

    /**
     * The repeat expression. May be {@code null}
     */
    private final RepeatExpression expression;

    /**
     * The series items.
     */
    private final List<ObjectSet> items;

    /**
     * The event display name.
     */
    private final String displayName;

    /**
     * Constructs an {@link CalendarEventSeriesState}.
     *
     * @param event   the event
     * @param service the archetype service
     */
    public CalendarEventSeriesState(Act event, IArchetypeService service) {
        ActBean bean = new ActBean(event);
        this.event = event;
        this.service = service;
        series = (Act) bean.getNodeSourceObject("repeat");
        if (series != null) {
            ActBean seriesBean = new ActBean(series);
            List<IMObjectReference> refs = seriesBean.getNodeTargetObjectRefs("items");
            ArchetypeQuery query = new ArchetypeQuery(event.getArchetypeId());
            query.getArchetypeConstraint().setAlias("act");
            query.add(new NodeSelectConstraint("id"));
            query.add(new NodeSelectConstraint("startTime"));
            query.add(new NodeSelectConstraint("status"));
            query.add(Constraints.sort("startTime"));
            query.add(Constraints.in("id", (Object[]) ActHelper.getIds(refs)));
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            IPage<ObjectSet> objects = service.getObjects(query);
            items = objects.getResults();
            int index = getIndex();
            if (index == 0) {
                first = event;
            } else if (!items.isEmpty()) {
                IMObjectReference reference = getReference(0);
                first = (Act) service.get(reference);
            } else {
                first = null;
            }
            expression = RepeatHelper.getExpression(seriesBean);
        } else {
            items = Collections.emptyList();
            first = null;
            expression = null;
        }
        displayName = DescriptorHelper.getDisplayName(event);
    }

    /**
     * Returns the event display name.
     *
     * @return the event display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determines if the event is associated with a series.
     * +
     *
     * @return {@code true} if the event is associated with a series
     */
    public boolean hasSeries() {
        return series != null;
    }

    /**
     * Returns the first act in the series.
     *
     * @return the first act in the series, or {@code null} if none exists
     */
    public Act getFirst() {
        return first;
    }

    /**
     * Returns the series repeat expression.
     *
     * @return the series repeat expression. May be {@code null}
     */
    public RepeatExpression getExpression() {
        return expression;
    }

    /**
     * Returns the series repeat condition, for the nth event in the series
     *
     * @param index the index of the nth event (0-based)
     * @return the series repeat condition. May be {@code null}
     */
    public RepeatCondition getCondition(int index) {
        RepeatCondition condition = null;
        if (series != null) {
            ActBean bean = new ActBean(series, service);
            condition = RepeatHelper.getCondition(bean, index);
        }
        return condition;
    }

    /**
     * Determines if the entire series can be edited.
     *
     * @return {@code true} if the entire series can be edited
     */
    public boolean canEditSeries() {
        Date now = new Date();
        return first != null && canEditFrom(0, now);
    }

    /**
     * Determines if the current and future acts can be edited.
     *
     * @return {@code true} if the current and future acts can be edited
     */
    public boolean canEditFuture() {
        Date now = new Date();
        int index = getIndex();
        return (index >= 0) && canEditFrom(index, now);
    }

    /**
     * Returns all non-pending appointment statuses after the first appointment in the series.
     *
     * @return the statuses
     */
    public List<String> getNonPendingStatuses() {
        return getNonPendingStatuses(0);
    }

    /**
     * Returns all non-pending appointment statuses after the current appointment in the series.
     *
     * @return the statuses
     */
    public List<String> getFutureNonPendingStatuses() {
        return getNonPendingStatuses(getIndex());
    }

    /**
     * Deletes the event.
     * <p/>
     * If it is the only event in the series, the series will be deleted, otherwise the series will remain.
     *
     * @return {@code true} if the event was deleted
     */
    public boolean delete() {
        TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (series != null) {
                    ActBean bean = new ActBean(series, service);
                    bean.removeNodeRelationships("items", event);
                    service.save(Arrays.asList(series, event));
                    service.remove(event);
                    if (bean.getNodeTargetObjectRefs("items").isEmpty()) {
                        service.remove(series);
                    }
                }
            }
        };
        return execute(callback);
    }

    /**
     * Deletes the current and future acts.
     *
     * @return {@code true} if the series was deleted
     */
    public boolean deleteFuture() {
        int index = getIndex();
        return (index >= 0) && delete(index);
    }

    /**
     * Deletes the series.
     *
     * @return {@code true} if the series was deleted
     */
    public boolean deleteSeries() {
        return series != null && delete(0);
    }

    /**
     * Returns the index of the event in the series.
     *
     * @return the index, or {@code -1} if not found
     */
    public int getIndex() {
        for (int i = 0; i < items.size(); ++i) {
            ObjectSet set = items.get(i);
            if (getId(set) == event.getId()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Deletes all events from the specified index.
     * <p/>
     * If there are no events left, the series will also be deleted
     *
     * @param index the events index
     */
    private boolean delete(final int index) {
        TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ActBean bean = new ActBean(series, service);
                List<Act> acts = new ArrayList<>();
                for (int i = index; i < items.size(); ++i) {
                    IMObjectReference reference = getReference(i);
                    Act act = (Act) ServiceHelper.getArchetypeService().get(reference);
                    if (act != null) {
                        acts.add(act);
                        bean.removeNodeRelationships("items", act);
                    }
                }
                List<Act> toSave = new ArrayList<>(acts);
                toSave.add(series);
                service.save(toSave);
                if (index == 0) {
                    acts.add(0, series);
                }
                for (Act act : acts) {
                    service.remove(act);
                }
            }
        };
        return execute(callback);
    }

    /**
     * Executes a callback in a transaction, logging any error.
     *
     * @param callback the callback to execute
     * @return {@code true} if the execution was successful
     */
    private boolean execute(TransactionCallbackWithoutResult callback) {
        boolean result = false;
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        try {
            template.execute(callback);
            result = true;
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Determines if the series can be edited from the specified index.
     * <p/>
     * The element at the index can always be edited.
     *
     * @param index the index
     * @param now   the current time
     * @return {@code true} if the series can be edited from the specified index
     */
    private boolean canEditFrom(int index, Date now) {
        boolean edit = true;
        for (int i = index + 1; i < items.size(); ++i) {
            if (!canEdit(items.get(i), now)) {
                edit = false;
                break;
            }
        }
        return edit;
    }

    /**
     * Determines if an event can be edited.
     *
     * @param event the event
     * @param now   the current time
     * @return {@code true} if the event can be edited
     */
    private boolean canEdit(ObjectSet event, Date now) {
        Date startTime = event.getDate("act.startTime");
        return DateRules.compareTo(startTime, now) > 0;
    }

    /**
     * Returns any appointment statuses after the specified index that are not PENDING.
     *
     * @param index the appointment index
     * @return the statuses
     */
    private List<String> getNonPendingStatuses(int index) {
        List<String> result = new ArrayList<>();
        if (TypeHelper.isA(event, ScheduleArchetypes.APPOINTMENT)) {
            for (int i = index + 1; i < items.size(); ++i) {
                String status = items.get(i).getString("act.status");
                if (!WorkflowStatus.PENDING.equals(status) && !result.contains(status)) {
                    result.add(status);
                }
            }
        }
        return result;
    }

    /**
     * Returns an event reference for the specified index.
     *
     * @param index the event index
     * @return the corresponding event reference
     */
    private IMObjectReference getReference(int index) {
        long id = getId(items.get(index));
        return new IMObjectReference(event.getArchetypeId(), id);
    }

    /**
     * Returns the event id from a set.
     *
     * @param set the set
     * @return the event id
     */
    private long getId(ObjectSet set) {
        return set.getLong("act.id");
    }

}
