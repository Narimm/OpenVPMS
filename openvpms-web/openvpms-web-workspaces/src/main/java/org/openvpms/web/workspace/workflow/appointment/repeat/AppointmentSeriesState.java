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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
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

import static org.openvpms.archetype.rules.workflow.ScheduleArchetypes.APPOINTMENT;

/**
 * Helper for working with appointment series without loading the entire series.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesState {

    /**
     * The appointment.
     */
    private final Act appointment;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The first act, if a series is present.
     */
    private final Act first;

    /**
     * The appointment series, or {@code null} if the appointment isn't associated with a series
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
     * Constructs an {@link AppointmentSeriesState}.
     *
     * @param appointment the appointment
     * @param service     the archetype service
     */
    public AppointmentSeriesState(Act appointment, IArchetypeService service) {
        ActBean bean = new ActBean(appointment);
        this.appointment = appointment;
        this.service = service;
        series = (Act) bean.getNodeSourceObject("repeat");
        if (series != null) {
            ActBean seriesBean = new ActBean(series);
            List<IMObjectReference> refs = seriesBean.getNodeTargetObjectRefs("items");
            ArchetypeQuery query = new ArchetypeQuery(APPOINTMENT);
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
                first = appointment;
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
    }

    /**
     * Determines if the appointment is associated with a series.
     * +
     *
     * @return {@code true} if the appointment is associated with a series
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
     * Returns the series repeat condition, for the nth appointment in the series
     *
     * @param index the index of the nth appointment (0-based)
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
     * Deletes the appointment.
     * <p/>
     * If it is the only appointment in the series, the series will be deleted, otherwise the series will remain.
     *
     * @return {@code true} if the appointment was deleted
     */
    public boolean delete() {
        TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (series != null) {
                    ActBean bean = new ActBean(series, service);
                    bean.removeNodeRelationships("items", appointment);
                    service.save(Arrays.asList(series, appointment));
                    service.remove(appointment);
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
     * Returns the index of the appointment in the series.
     *
     * @return the index, or {@code -1} if not found
     */
    public int getIndex() {
        for (int i = 0; i < items.size(); ++i) {
            ObjectSet set = items.get(i);
            if (getId(set) == appointment.getId()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Deletes all appointments from the specified index.
     * <p/>
     * If there are no appointments left, the series will also be deleted
     *
     * @param index the appointment index
     */
    private boolean delete(final int index) {
        TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ActBean bean = new ActBean(series, service);
                List<Act> acts = new ArrayList<Act>();
                for (int i = index; i < items.size(); ++i) {
                    IMObjectReference reference = getReference(i);
                    Act act = (Act) ServiceHelper.getArchetypeService().get(reference);
                    if (act != null) {
                        acts.add(act);
                        bean.removeNodeRelationships("items", act);
                    }
                }
                List<Act> toSave = new ArrayList<Act>(acts);
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
     * Determines if an appointment can be edited.
     *
     * @param set the appointment
     * @param now the current time
     * @return {@code true} if the appointment can be edited
     */
    private boolean canEdit(ObjectSet set, Date now) {
        String status = set.getString("act.status");
        Date startTime = set.getDate("act.startTime");
        return WorkflowStatus.PENDING.equals(status) && DateRules.compareTo(startTime, now) > 0;
    }

    /**
     * Returns an appointment reference for the specified index.
     *
     * @param index the appointment index
     * @return the corresponding appointment reference
     */
    private IMObjectReference getReference(int index) {
        long id = getId(items.get(index));
        return new IMObjectReference(APPOINTMENT, id);
    }

    /**
     * Returns the appointment id from a set.
     *
     * @param set the set
     * @return the appointment id
     */
    private long getId(ObjectSet set) {
        return set.getLong("act.id");
    }

}
