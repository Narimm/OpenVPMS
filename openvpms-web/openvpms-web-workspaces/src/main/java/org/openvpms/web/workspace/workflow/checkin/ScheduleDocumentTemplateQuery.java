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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ExistsConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.focus.FocusHelper;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.exists;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * A query for <em>entity.documentTemplate</em> instances, optionally linked to a <em>party.organisationSchedule</em>
 * or <em>party.organisationWorkList</em>.
 *
 * @author Tim Anderson
 */
class ScheduleDocumentTemplateQuery extends DocumentTemplateQuery {

    /**
     * The schedule. May be {@code null}
     */
    private final Entity schedule;

    /**
     * The work list. May be {@code null}
     */
    private final Entity workList;


    /**
     * Constructs a {@link ScheduleDocumentTemplateQuery}.
     *
     * @param schedule the schedule. May be {@code null}
     * @param workList the work list. May be {@code null}
     */
    public ScheduleDocumentTemplateQuery(Entity schedule, Entity workList) {
        if (useAllTemplates(schedule) || useAllTemplates(workList)) {
            // don't constrain to either a schedule or work list.
            this.schedule = null;
            this.workList = null;
        } else {
            this.schedule = schedule;
            this.workList = workList;
        }
        setTypes(PatientArchetypes.DOCUMENT_FORM, PatientArchetypes.DOCUMENT_LETTER);
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically;
     * otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return true;
    }

    /**
     * Returns the schedule.
     *
     * @return the schedule, or {@code null} if  all templates are being used
     */
    public Entity getSchedule() {
        return schedule;
    }

    /**
     * Returns the work list.
     *
     * @return the work list, or {@code null} if  all templates are being used
     */
    public Entity getWorkList() {
        return workList;
    }

    /**
     * Determines if a schedule uses all patient forms and letters, or those directly associated with it via
     * its templates node.
     * <p/>
     * If the useAllTemplates node is {@code null}, then all templates will be selected if the the templates node is
     * empty. This is to support existing sites that won't have a value for useAllTemplates populated.
     *
     * @param schedule the schedule. An <em>party.organisationSchedule</em> or <em>party.organisationWorkList</em>.
     * @return {@code true} if the schedule uses all patient forms and letters, {@code false} if it uses those linked
     * via its "templates" node
     */
    public static boolean useAllTemplates(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule);
        if (bean.getValue("useAllTemplates") != null) {
            return bean.getBoolean("useAllTemplates");
        } else {
            return bean.getValues("templates").isEmpty();
        }
    }

    /**
     * Determines if a schedule/work list has any active templates linked to it.
     *
     * @param schedule the schedule/work list. May be {@code null}
     * @return {@code true} if there are any active templates, otherwise {@code false}
     */
    public static boolean hasTemplates(Entity schedule) {
        boolean result = false;
        if (schedule != null) {
            if (useAllTemplates(schedule)) {
                result = true;
            } else {
                ArchetypeQuery query = new ArchetypeQuery(schedule.getObjectReference());
                query.add(new NodeSelectConstraint("id"));
                query.add(join("templates").add(join("target").add(eq("active", true))));
                query.setMaxResults(1);
                ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
                result = iterator.hasNext();
            }
        }
        return result;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addSearchField(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Entity> createResultSet(SortConstraint[] sort) {
        return new EntityResultSet<Entity>(getArchetypeConstraint(), getValue(), false, null, sort, getMaxResults(),
                                           isDistinct()) {
            /**
             * Creates a new archetype query.
             *
             * @return a new archetype query
             */
            @Override
            protected ArchetypeQuery createQuery() {
                getArchetypes().setAlias("t");
                ArchetypeQuery query = super.createQuery();
                ExistsConstraint scheduleExists = schedule != null ? createExists(schedule, "s") : null;
                ExistsConstraint worklistExists = workList != null ? createExists(workList, "w") : null;
                if (scheduleExists != null && worklistExists != null) {
                    query.add(or(scheduleExists, worklistExists));
                } else if (scheduleExists != null) {
                    query.add(scheduleExists);
                } else if (worklistExists != null) {
                    query.add(worklistExists);
                }
                return query;
            }

            private ExistsConstraint createExists(Entity entity, String alias) {
                String relAlias = alias + "r";
                return exists(subQuery(entity, alias).add(join("templates", relAlias)
                                                                  .add(idEq("t", relAlias + ".target"))));
            }
        };
    }

}
