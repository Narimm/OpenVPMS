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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.component.im.query.QueryTestHelper.checkSelects;

/**
 * Tests the {@link ScheduleDocumentTemplateQuery} class.
 *
 * @author Tim Anderson
 */
public class ScheduleDocumentTemplateQueryTestCase extends AbstractAppTest {

    /**
     * Tests querying for schedules and work lists with {@code useAllTemplates=false} and no templates.
     */
    @Test
    public void testUseAllTemplatesFalseWithNoTemplates() {
        Party location = TestHelper.createLocation();
        Entity schedule1 = createSchedule(location, false);
        Entity workList1 = createWorkList(false);
        checkQuery(schedule1, workList1);
    }

    /**
     * Tests querying for schedules and work lists with {@code useAllTemplates=true} and no templates linked to the
     * schedule or work list. All available templates should be returned.
     */
    @Test
    public void testUseAllTemplatesTrueWithNoTemplates() {
        // create at least one template
        Entity template1 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);

        Party location = TestHelper.createLocation();
        Entity schedule1 = createSchedule(location, true);
        Entity workList1 = createWorkList(true);
        ScheduleDocumentTemplateQuery query = new ScheduleDocumentTemplateQuery(schedule1, workList1);
        checkSelects(true, query, template1);
    }

    /**
     * Tests querying for schedules and work lists with {@code useAllTemplates=true} that have templates,
     * All available templates should be returned.
     */
    @Test
    public void testUseAllTemplatesTrueWithTemplates() {
        Entity template1 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template2 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template3 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);

        Party location = TestHelper.createLocation();
        Entity schedule1 = createSchedule(location, true, template1);
        Entity workList1 = createWorkList(true, template2);
        ScheduleDocumentTemplateQuery query = new ScheduleDocumentTemplateQuery(schedule1, workList1);
        checkSelects(true, query, template1);
        checkSelects(true, query, template2);
        checkSelects(true, query, template3);
    }

    /**
     * Tests querying for schedules and work lists with no {@code useAllTemplates=false}.
     */
    @Test
    public void testUseAllTemplatesFalseWithTemplates() {
        Party location = TestHelper.createLocation();
        Entity template1 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template2 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template3 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template4 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity schedule1 = createSchedule(location, false, template1);
        Entity schedule2 = createSchedule(location, false, template2);
        Entity workList1 = createWorkList(false, template3);
        Entity workList2 = createWorkList(false, template4);
        checkQuery(schedule1, workList1, template1, template3);
        checkQuery(schedule2, workList2, template2, template4);
    }

    /**
     * Tests querying for schedules and work lists with no {@code useAllTemplates=false} with duplicate templates.
     * <p>
     * These should only appear once in the results.
     */
    @Test
    public void testUseAllTemplatesFalseWithDuplicateTemplates() {
        Party location = TestHelper.createLocation();
        Entity template1 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity template2 = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Entity schedule1 = createSchedule(location, false, template1, template2);
        Entity workList1 = createWorkList(false, template1, template2);
        checkQuery(schedule1, workList1, template1, template2);
    }

    /**
     * Verifies that the templates returned by the query match those expected.
     *
     * @param schedule  the schedule
     * @param workList  the work list
     * @param templates the expected templates
     */
    private void checkQuery(Entity schedule, Entity workList, Entity... templates) {
        ScheduleDocumentTemplateQuery query = new ScheduleDocumentTemplateQuery(schedule, workList);
        List<Entity> matches = QueryHelper.query(query);
        assertEquals(templates.length, matches.size());
        for (Entity template : templates) {
            assertTrue(matches.contains(template));
        }
    }

    /**
     * Creates a schedule.
     *
     * @param location        the practice location
     * @param useAllTemplates if {@code true}, use all templates rather than those linked
     * @param templates       the templates
     * @return a new schedule
     */
    private Entity createSchedule(Party location, boolean useAllTemplates, Entity... templates) {
        Entity schedule = ScheduleTestHelper.createSchedule(location);
        addTemplates(schedule, useAllTemplates, templates);
        return schedule;
    }

    /**
     * Creates a work list.
     *
     * @param useAllTemplates if {@code true}, use all templates rather than those linked
     * @param templates       the templates
     * @return a new work list
     */
    private Entity createWorkList(boolean useAllTemplates, Entity... templates) {
        Entity workList = ScheduleTestHelper.createWorkList();
        addTemplates(workList, useAllTemplates, templates);
        return workList;
    }

    /**
     * Adds templates to a schedule or work list.
     *
     * @param schedule        the schedule/work list
     * @param useAllTemplates if {@code true}, use all templates rather than those linked
     * @param templates       the templates
     */
    private void addTemplates(Entity schedule, boolean useAllTemplates, Entity[] templates) {
        IMObjectBean bean = new IMObjectBean(schedule);
        for (Entity template : templates) {
            bean.addNodeTarget("templates", template);
        }
        bean.setValue("useAllTemplates", useAllTemplates);
        bean.save();
    }

}
