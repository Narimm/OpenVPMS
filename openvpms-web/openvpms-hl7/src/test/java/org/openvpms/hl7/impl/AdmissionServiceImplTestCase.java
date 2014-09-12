package org.openvpms.hl7.impl;

import org.junit.Test;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.AdmissionService;
import org.openvpms.hl7.PatientContext;

/**
 * Tests the {@link AdmissionServiceImpl}.
 *
 * @author Tim Anderson
 */
public class AdmissionServiceImplTestCase extends AbstractServiceTest {

    /**
     * The admission service.
     */
    private AdmissionService admissionService;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        ILookupService lookups = LookupServiceHelper.getLookupService();
        admissionService = new AdmissionServiceImpl(getArchetypeService(), lookups, getConnectors(), getManager());
    }

    /**
     * Tests the {@link AdmissionServiceImpl#admitted(PatientContext)} method.
     *
     * @throws Exception
     */
    @Test
    public void testAdmitted() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A01^ADT_A01|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A01|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r";

        admissionService.admitted(getContext());
        checkMessage(expected);
    }

    /**
     * Tests the {@link AdmissionServiceImpl#admissionCancelled(PatientContext)} method.
     *
     * @throws Exception
     */
    @Test
    public void testAdmissionCancelled() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A11^ADT_A09|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A11|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        admissionService.admissionCancelled(getContext());
        checkMessage(expected);
    }

    /**
     * Tests the {@link AdmissionServiceImpl#discharged(PatientContext)} method.
     *
     * @throws Exception
     */
    @Test
    public void testDischarged() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A03^ADT_A03|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A03|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        admissionService.discharged(getContext());
        checkMessage(expected);
    }

    /**
     * Tests the {@link AdmissionServiceImpl#updated(PatientContext)} method.
     *
     * @throws Exception
     */
    @Test
    public void testUpdated() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A08^ADT_A01|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A08|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r";

        admissionService.updated(getContext());
        checkMessage(expected);
    }

}
