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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemRecordCRUDWindow;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link VisitEditor}.
 *
 * @author Tim Anderson
 */
public class VisitEditorTestCase extends AbstractAppTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The invoice.
     */
    private FinancialAct invoice;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext(ContextApplicationInstance.getInstance().getContext());
        Party practice = context.getPractice();
        assertNotNull(practice);
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("showProblemsInVisit", true);

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        context.setPatient(patient);

        invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        ActBean invoiceBean = new ActBean(invoice);
        invoiceBean.addNodeParticipation("customer", customer);
        invoiceBean.save();
    }

    /**
     * Tests initialisation of the editor.
     */
    @Test
    public void testInit() {
        PatientTestHelper.createEvent(TestHelper.getDate("2014-07-01"), patient); // create an older event
        Act event = PatientTestHelper.createEvent(TestHelper.getDate("2014-07-20"), patient);

        VisitEditor editor = new VisitEditor(customer, patient, event, invoice, context,
                                             new HelpContext("dummy", null));
        editor.getComponent();

        assertEquals(patient, editor.getPatient());

        assertEquals(event, editor.getHistoryBrowser().getSelected());
        assertEquals(event, editor.getHistoryWindow().getObject());
        assertEquals(event, editor.getHistoryWindow().getEvent());
    }

    /**
     * Verifies that the {@link PatientHistoryCRUDWindow} is updated with the selected object and event when it
     * is selected in the browser.
     */
    @Test
    public void testSelectHistory() {
        Date date1 = TestHelper.getDate("2014-07-01");
        Act note1 = PatientTestHelper.createNote(date1, patient);
        Act event1 = PatientTestHelper.createEvent(date1, patient, note1);

        Date date2 = TestHelper.getDate("2014-07-02");
        Act note2 = PatientTestHelper.createNote(date2, patient);
        Act event2 = PatientTestHelper.createEvent(date2, patient, note2);

        VisitEditor editor = new VisitEditor(customer, patient, event2, invoice, context,
                                             new HelpContext("dummy", null));
        editor.getComponent();

        PatientHistoryBrowser history = editor.getHistoryBrowser();

        history.setSelected(event1);
        checkHistorySelection(event1, event1, editor);

        history.setSelected(note1);
        checkHistorySelection(note1, event1, editor);

        history.setSelected(event2);
        checkHistorySelection(event2, event2, editor);

        history.setSelected(note2);
        checkHistorySelection(note2, event2, editor);
    }

    /**
     * Verifies that the {@link ProblemRecordCRUDWindow} is updated with the selected object, problem and event when it
     * is selected in the browser.
     */
    @Test
    public void testSelectProblems() {
        Date date1 = TestHelper.getDate("2014-07-01");
        Act note1 = PatientTestHelper.createNote(date1, patient);
        Act problem1 = PatientTestHelper.createProblem(date1, patient, note1);
        Act event1 = PatientTestHelper.createEvent(date1, patient, problem1, note1);

        Date date2 = TestHelper.getDate("2014-07-02");
        Act note2 = PatientTestHelper.createNote(date2, patient);
        Act problem2 = PatientTestHelper.createProblem(date2, patient, note2);
        Act event2 = PatientTestHelper.createEvent(date2, patient, problem2, note2);

        VisitEditor editor = new VisitEditor(customer, patient, event2, invoice, context,
                                             new HelpContext("dummy", null));
        editor.getComponent();

        editor.selectTab(VisitEditor.PROBLEM_TAB);
        checkProblemSelection(null, null, null, editor); // NOTE - different to PatientRecordWorkspace

        ProblemBrowser browser = editor.getProblemBrowser();

        browser.setSelected(problem1);
        checkProblemSelection(problem1, problem1, event1, editor);

        browser.setSelected(note1);
        checkProblemSelection(note1, problem1, event1, editor);

        browser.setSelected(event1);
        checkProblemSelection(event1, problem1, event1, editor);

        browser.setSelected(event2);
        checkProblemSelection(event2, problem2, event2, editor);
    }

    /**
     * Verifies that selecting a problem hyperlink switches to it on the Problems tab, and that selecting a visit
     * hyperlink switches to it on the Summary tab.
     */
    @Test
    public void testHyperlinks() {
        Date date1 = TestHelper.getDate("2014-07-01");
        Act note1 = PatientTestHelper.createNote(date1, patient);
        Act problem1 = PatientTestHelper.createProblem(date1, patient, note1);
        Act event1 = PatientTestHelper.createEvent(date1, patient, problem1);

        Date date2 = TestHelper.getDate("2014-07-02");
        Act note2 = PatientTestHelper.createNote(date2, patient);
        Act problem2 = PatientTestHelper.createProblem(date2, patient, note2);
        Act event2 = PatientTestHelper.createEvent(date2, patient, problem2);

        VisitEditor editor = new VisitEditor(customer, patient, event2, invoice, context,
                                             new HelpContext("dummy", null));
        editor.getComponent();

        editor.followHyperlink(problem1);

        checkProblemSelection(problem1, problem1, event1, editor);

        editor.followHyperlink(event2);
        checkHistorySelection(event2, event2, editor);

        editor.followHyperlink(problem2);
        checkProblemSelection(problem2, problem2, event2, editor);
    }

    /**
     * Verifies that the expected object is selected in the history browser and CRUD window.
     *
     * @param object the expected object
     * @param event  the expected event
     * @param editor the editor
     */
    private void checkHistorySelection(Act object, Act event, VisitEditor editor) {
        Browser<Act> selectedBrowser = editor.getHistoryBrowser();
        assertEquals(object, selectedBrowser.getSelected());
        assertEquals(event, ((PatientHistoryBrowser) selectedBrowser).getSelectedParent());
        CRUDWindow<Act> window = editor.getHistoryWindow();
        assertTrue(window instanceof PatientHistoryCRUDWindow);
        assertEquals(object, window.getObject());
        assertEquals(event, ((PatientHistoryCRUDWindow) window).getEvent());
    }

    /**
     * Verifies that the expected object is selected in the problem browser and CRUD window.
     *
     * @param object  the expected object
     * @param problem the expected problem
     * @param event   the expected event
     * @param editor  the visit editor
     */
    private void checkProblemSelection(Act object, Act problem, Act event, VisitEditor editor) {
        ProblemBrowser browser = editor.getProblemBrowser();
        assertNotNull(browser);
        assertEquals(object, browser.getSelected());
        assertEquals(problem, browser.getSelectedParent());

        ProblemRecordCRUDWindow window = editor.getProblemWindow();
        assertNotNull(window);
        assertEquals(object, window.getObject());
        assertEquals(problem, window.getProblem());
        assertEquals(event, window.getEvent());
    }

}
