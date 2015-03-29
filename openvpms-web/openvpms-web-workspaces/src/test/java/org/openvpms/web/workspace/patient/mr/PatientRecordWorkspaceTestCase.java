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

package org.openvpms.web.workspace.patient.mr;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemRecordCRUDWindow;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PatientRecordWorkspace}.
 *
 * @author Tim Anderson
 */
public class PatientRecordWorkspaceTestCase extends AbstractAppTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext(ContextApplicationInstance.getInstance().getContext());

        patient = TestHelper.createPatient();
        context.setPatient(patient);
    }

    /**
     * Tests initialisation of the workspace for a patient with no history.
     */
    @Test
    public void testInit() {
        TestRecordWorkspace workspace = new TestRecordWorkspace(context);
        workspace.getComponent();
        assertEquals(patient, workspace.getObject());
        CRUDWindow<Act> window = workspace.getCRUDWindow();
        assertTrue(window instanceof PatientHistoryCRUDWindow);
        assertNull(window.getObject());
        assertNull(((PatientHistoryCRUDWindow) window).getEvent());
    }

    /**
     * Verifies that if there is existing history, the most recent event is selected first.
     */
    @Test
    public void testInitWithEvents() {
        PatientTestHelper.createEvent(TestHelper.getDate("2014-07-01"), patient);
        Act event2 = PatientTestHelper.createEvent(TestHelper.getDate("2014-07-02"), patient);
        TestRecordWorkspace workspace = new TestRecordWorkspace(context);
        workspace.getComponent();

        assertEquals(patient, workspace.getObject());
        checkHistorySelection(event2, event2, workspace);
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

        TestRecordWorkspace workspace = new TestRecordWorkspace(context);
        workspace.getComponent();
        PatientHistoryBrowser history = workspace.getBrowser().getHistory();

        history.setSelected(event1);
        checkHistorySelection(event1, event1, workspace);

        history.setSelected(note1);
        checkHistorySelection(note1, event1, workspace);

        history.setSelected(event2);
        checkHistorySelection(event2, event2, workspace);

        history.setSelected(note2);
        checkHistorySelection(note2, event2, workspace);
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

        TestRecordWorkspace workspace = new TestRecordWorkspace(context);
        workspace.getComponent();

        TestRecordBrowser browser = workspace.getBrowser();
        browser.showProblems();
        checkProblemSelection(problem2, problem2, event2, workspace);

        browser.setSelected(problem1);
        checkProblemSelection(problem1, problem1, event1, workspace);

        browser.setSelected(note1);
        checkProblemSelection(note1, problem1, event1, workspace);

        browser.setSelected(event1);
        checkProblemSelection(event1, problem1, event1, workspace);
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

        TestRecordWorkspace workspace = new TestRecordWorkspace(context);
        workspace.getComponent();

        TestRecordBrowser browser = workspace.getBrowser();
        browser.followHyperlink(problem1);

        checkProblemSelection(problem1, problem1, event1, workspace);

        browser.followHyperlink(event2);
        checkHistorySelection(event2, event2, workspace);

        browser.followHyperlink(problem2);
        checkProblemSelection(problem2, problem2, event2, workspace);
    }

    /**
     * Verifies that the expected object is selected in the history browser and CRUD window.
     *
     * @param object    the expected object
     * @param event     the expected event
     * @param workspace the workspace
     */
    private void checkHistorySelection(Act object, Act event, TestRecordWorkspace workspace) {
        Browser<Act> selectedBrowser = workspace.getBrowser().getSelectedBrowser();
        assertTrue(selectedBrowser instanceof PatientHistoryBrowser);
        assertEquals(object, selectedBrowser.getSelected());
        assertEquals(event, ((PatientHistoryBrowser) selectedBrowser).getSelectedParent());
        CRUDWindow<Act> window = workspace.getCRUDWindow();
        assertTrue(window instanceof PatientHistoryCRUDWindow);
        assertEquals(object, window.getObject());
        assertEquals(event, ((PatientHistoryCRUDWindow) window).getEvent());
    }

    /**
     * Verifies that the expected object is selected in the problem browser and CRUD window.
     *
     * @param object    the expected object
     * @param problem   the expected problem
     * @param event     the expected event
     * @param workspace the workspace
     */
    private void checkProblemSelection(Act object, Act problem, Act event, TestRecordWorkspace workspace) {
        Browser<Act> selectedBrowser = workspace.getBrowser().getSelectedBrowser();
        assertTrue(selectedBrowser instanceof ProblemBrowser);
        assertEquals(object, selectedBrowser.getSelected());
        assertEquals(problem, ((ProblemBrowser) selectedBrowser).getSelectedParent());

        CRUDWindow<Act> window = workspace.getCRUDWindow();
        assertTrue(window instanceof ProblemRecordCRUDWindow);
        assertEquals(object, window.getObject());
        assertEquals(problem, ((ProblemRecordCRUDWindow) window).getProblem());
        assertEquals(event, ((ProblemRecordCRUDWindow) window).getEvent());
    }

    private static class TestRecordBrowser extends RecordBrowser {
        /**
         * Constructs a {@link TestRecordBrowser}.
         *
         * @param query   the patient history query
         * @param context the context
         * @param help    the help context
         */
        public TestRecordBrowser(Party patient, PatientHistoryQuery query, Context context, HelpContext help) {
            super(patient, query, context, help);
        }

        /**
         * Follow a hyperlink.
         * <p/>
         * If the object is a:
         * <ul>
         * <li>problem, the Problems tab will be shown, and the problem selected</li>
         * <li>event, the Summary tab will be shown, and the event selected</li>
         * </ul>
         *
         * @param object the object to display
         */
        @Override
        public void followHyperlink(IMObject object) {
            super.followHyperlink(object);
        }
    }

    private static class TestRecordWorkspace extends PatientRecordWorkspace {

        /**
         * Constructs a {@link TestRecordWorkspace}.
         *
         * @param context the context
         */
        public TestRecordWorkspace(Context context) {
            super(context);
        }

        /**
         * Returns the CRUD window, creating it if it doesn't exist.
         *
         * @return the CRUD window
         */
        @Override
        public CRUDWindow<Act> getCRUDWindow() {
            return super.getCRUDWindow();
        }

        /**
         * Returns the browser.
         *
         * @return the browser, or {@code null} if none has been registered
         */
        @Override
        public TestRecordBrowser getBrowser() {
            return (TestRecordBrowser) super.getBrowser();
        }

        /**
         * Creates a new patient record browser.
         *
         * @param patient the patient
         * @param query   the patient history query
         * @param context the context
         * @param help    the help context
         * @return a new record browser
         */
        @Override
        protected RecordBrowser createRecordBrowser(Party patient, PatientHistoryQuery query, Context context,
                                                    HelpContext help) {
            return new TestRecordBrowser(patient, query, context, help);
        }
    }
}
