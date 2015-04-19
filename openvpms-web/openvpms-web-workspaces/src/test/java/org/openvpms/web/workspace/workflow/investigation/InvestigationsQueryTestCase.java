package org.openvpms.web.workspace.workflow.investigation;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.QueryTestHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertNull;

/**
 * Tests the {@link InvestigationsQuery} class.
 *
 * @author Tim Anderson
 */
public class InvestigationsQueryTestCase extends AbstractAppTest {

    /**
     * Verifies that investigations can be filtered by type.
     */
    @Test
    public void testQueryByInvestigationType() {
        Party patient = TestHelper.createPatient();
        Entity type1 = PatientTestHelper.createInvestigationType();
        Entity type2 = PatientTestHelper.createInvestigationType();
        Act investigation1 = PatientTestHelper.createInvestigation(patient, type1);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, type2);
        InvestigationsQuery query = createQuery(new LocalContext());

        QueryTestHelper.checkExists(query, investigation1, investigation2);

        query.setInvestigationType(type1);
        QueryTestHelper.checkExists(query, investigation1);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setInvestigationType(type2);
        QueryTestHelper.checkNotExists(query, investigation1);
        QueryTestHelper.checkExists(query, investigation2);

        query.setInvestigationType(null);
        QueryTestHelper.checkExists(query, investigation1, investigation2);
    }

    /**
     * Verifies that investigations can be filtered by clinician.
     */
    @Test
    public void testQueryByClinician() {
        Party patient = TestHelper.createPatient();
        User clinician1 = TestHelper.createClinician();
        User clinician2 = TestHelper.createClinician();
        Entity type = PatientTestHelper.createInvestigationType();
        Act investigation1 = PatientTestHelper.createInvestigation(patient, clinician1, type);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, clinician2, type);
        InvestigationsQuery query = createQuery(new LocalContext());

        QueryTestHelper.checkExists(query, investigation1, investigation2);

        query.setClinician(clinician1);
        QueryTestHelper.checkExists(query, investigation1);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setClinician(clinician2);
        QueryTestHelper.checkNotExists(query, investigation1);
        QueryTestHelper.checkExists(query, investigation2);

        query.setClinician(null);
        QueryTestHelper.checkExists(query, investigation1, investigation2);
    }

    /**
     * Verifies that investigations can be filtered by location.
     * <p/>
     * Location filtering requires the context to be populated with the user and practice.
     * When the practice is supplied, only those investigations for the practice, or that have no location will be
     * returned.
     */
    @Test
    public void testQueryByLocation() {
        LocalContext context = new LocalContext();
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        Entity type = PatientTestHelper.createInvestigationType();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        Party practice = createPractice(location1, location2);
        context.setPractice(practice);
        context.setUser(clinician);   // clinician has no location relationships, so can see all locations
        Act investigation1 = PatientTestHelper.createInvestigation(patient, clinician, location1, type);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, clinician, location2, type);
        Act investigation3 = PatientTestHelper.createInvestigation(patient, clinician, location3, type);
        // location3 is not linked to the practice, so it should not be returned in queries

        Act investigation4 = PatientTestHelper.createInvestigation(patient, clinician, type); // no location

        InvestigationsQuery query = createQuery(context);

        QueryTestHelper.checkExists(query, investigation1, investigation2, investigation4);
        QueryTestHelper.checkNotExists(query, investigation3);

        query.setLocation(location1);
        QueryTestHelper.checkExists(query, investigation1, investigation4);
        QueryTestHelper.checkNotExists(query, investigation2, investigation3);

        query.setLocation(location2);
        QueryTestHelper.checkExists(query, investigation2, investigation4);
        QueryTestHelper.checkNotExists(query, investigation1, investigation3);

        query.setLocation(null);
        QueryTestHelper.checkExists(query, investigation1, investigation2, investigation4);
        QueryTestHelper.checkNotExists(query, investigation3);
    }

    /**
     * Verifies that when a user is configured with locations, they only see investigations for those locations,
     * or investigations without any location.
     */
    @Test
    public void testQueryByLocationForRestrictedUser() {
        LocalContext context = new LocalContext();
        Party patient = TestHelper.createPatient();
        Entity type = PatientTestHelper.createInvestigationType();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party practice = createPractice(location1, location2);
        User clinician = createClinician(location1); // can only see results for location1, or those with no location
        context.setPractice(practice);
        context.setUser(clinician);
        Act investigation1 = PatientTestHelper.createInvestigation(patient, clinician, location1, type);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, clinician, location2, type);
        Act investigation3 = PatientTestHelper.createInvestigation(patient, clinician, type); // no location

        InvestigationsQuery query = createQuery(context);

        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setLocation(location1);
        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setLocation(location2);
        assertNull(query.getLocation());   // location2 not visible, so set unsuccessful. Effectively setLocation(null)

        query.setLocation(null);
        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);
    }

    /**
     * Checks querying by location when only a single location is configured, and the user has a link to the location.
     */
    @Test
    public void testQueryByLocationForSingleLocation() {
        LocalContext context = new LocalContext();
        Party patient = TestHelper.createPatient();
        Entity type = PatientTestHelper.createInvestigationType();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation(); // not linked to anything
        Party practice = createPractice(location1);
        User clinician = createClinician(location1);
        context.setPractice(practice);
        context.setUser(clinician);
        Act investigation1 = PatientTestHelper.createInvestigation(patient, clinician, location1, type);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, clinician, location2, type);
        Act investigation3 = PatientTestHelper.createInvestigation(patient, clinician, type); // no location

        InvestigationsQuery query = createQuery(context);

        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setLocation(location1);
        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);

        query.setLocation(null);
        QueryTestHelper.checkExists(query, investigation1, investigation3);
        QueryTestHelper.checkNotExists(query, investigation2);
    }

    /**
     * Tests querying by location and investigation type.
     */
    @Test
    public void testQueryByLocationAndInvestigationType() {
        LocalContext context = new LocalContext();
        Party patient = TestHelper.createPatient();
        Entity type1 = PatientTestHelper.createInvestigationType();
        Entity type2 = PatientTestHelper.createInvestigationType();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party practice = createPractice(location1, location2);
        User clinician = createClinician(location1); // can only see results for location1, or those with no location
        context.setPractice(practice);
        context.setUser(clinician);
        Act investigation1 = PatientTestHelper.createInvestigation(patient, clinician, location1, type1);
        Act investigation2 = PatientTestHelper.createInvestigation(patient, clinician, location1, type2);
        Act investigation3 = PatientTestHelper.createInvestigation(patient, clinician, location2, type1);
        Act investigation4 = PatientTestHelper.createInvestigation(patient, clinician, type1); // no location

        InvestigationsQuery query = createQuery(context);

        QueryTestHelper.checkExists(query, investigation1, investigation2, investigation4);
        QueryTestHelper.checkNotExists(query, investigation3);

        query.setLocation(location1);
        query.setInvestigationType(type1);
        QueryTestHelper.checkExists(query, investigation1, investigation4);
        QueryTestHelper.checkNotExists(query, investigation2, investigation3);

        query.setLocation(location2);
        assertNull(query.getLocation());   // location2 not visible, so set unsuccessful. Effectively setLocation(null)

        query.setLocation(null);
        query.setInvestigationType(null);
        QueryTestHelper.checkExists(query, investigation1, investigation2, investigation4);
        QueryTestHelper.checkNotExists(query, investigation3);
    }

    private InvestigationsQuery createQuery(LocalContext context) {
        DefaultLayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        return new InvestigationsQuery(layout);
    }

    /**
     * Creates a practice linked to locations.
     *
     * @param locations the locations
     * @return a new practice
     */
    private Party createPractice(Party... locations) {
        return addLocations((Party) create(PracticeArchetypes.PRACTICE), locations);
    }

    /**
     * Creates a clinician linked to locations.
     *
     * @param locations the locations
     * @return a new clinician
     */
    private User createClinician(Party... locations) {
        return addLocations(TestHelper.createClinician(), locations);
    }

    /**
     * Adds locations to a party.
     *
     * @param party     the party
     * @param locations the locations
     * @return the party
     */
    private <T extends Party> T addLocations(T party, Party[] locations) {
        EntityBean bean = new EntityBean(party);
        for (Party location : locations) {
            bean.addNodeRelationship("locations", location);
        }
        return party;
    }

}
