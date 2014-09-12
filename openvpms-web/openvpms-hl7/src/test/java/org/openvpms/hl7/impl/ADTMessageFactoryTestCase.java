package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.PatientContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ADTMessageFactory} class.
 *
 * @author Tim Anderson
 */
public class ADTMessageFactoryTestCase extends AbstractMessageTest {

    /**
     * The message factory.
     */
    private ADTMessageFactory messageFactory;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();

        HapiContext hapiContext = new DefaultHapiContext();
        hapiContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });

        messageFactory = new ADTMessageFactory(hapiContext, getArchetypeService(),
                                               LookupServiceHelper.getLookupService());
    }

    /**
     * Tests the {@link ADTMessageFactory#createAdmit(PatientContext)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateAdmit() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A01^ADT_A01|1200022|P|2.5\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r";

        Message admit = messageFactory.createAdmit(getContext());
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createCancelAdmit(PatientContext)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateCancelAdmit() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A11^ADT_A09|1200022|P|2.5\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        Message admit = messageFactory.createCancelAdmit(getContext());
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createDischarge(PatientContext)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateDischarge() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A03^ADT_A03|1200022|P|2.5\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000|20140825100500+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        PatientContext context = getContext();
        Act visit = context.getVisit();
        visit.setActivityEndTime(getDatetime("2014-08-25 10:05:00").getTime());
        Message admit = messageFactory.createDischarge(context);
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createUpdate(PatientContext)}.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateUpdate() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A08^ADT_A01|1200022|P|2.5\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r";

        Message admit = messageFactory.createUpdate(getContext());
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

}
