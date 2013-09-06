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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ExistsConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.ResultSet;

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
     * Determines if a schedule uses all patient forms and letters, or those directly associated with it via
     * its templates node.
     *
     * @param schedule the schedule. An <em>party.organisationSchedule</em> or <em>party.organisationWorkList</em>.
     * @return {@code true} if the schedule uses all patient forms and letters, {@code false} if it uses those linked
     *         via its "templates" node
     */
    public static boolean useAllTemplates(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule);
        if (bean.getValues("useAllTemplates") != null) {
            return bean.getBoolean("useAllTemplates");
        } else {
            return bean.getValues("templates").isEmpty();
        }
    }

    /**
     * Constructs a {@link ScheduleDocumentTemplateQuery}.
     *
     * @param schedule the schedule. May be {@code null}
     * @param workList the work list. May be {@code null}
     */
    public ScheduleDocumentTemplateQuery(Entity schedule, Entity workList) {
        if ((schedule != null && useAllTemplates(schedule)) || (workList != null && useAllTemplates(workList))) {
            this.schedule = null;
            this.workList = null;
        } else {
            this.schedule = schedule;
            this.workList = workList;
        }
        setTypes(PatientArchetypes.DOCUMENT_FORM, PatientArchetypes.DOCUMENT_LETTER);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
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
                return exists(subQuery(entity.getArchetypeId().getShortName(), alias)
                                      .add(join("templates", relAlias).add(idEq("t", relAlias + ".target"))));
            }
        };
    }

}
