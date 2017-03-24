package org.openvpms.archetype.rules.contact;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link PracticeAddressFormatter}.
 *
 * @author Tim Anderson
 */
public class PracticeAddressFormatterTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link PracticeAddressFormatter#format(Contact, boolean)} method.
     */
    @Test
    public void testFormat() {
        // create an address format
        String singleLine = "concat($address, '|', $suburb, '|', $state, '|', $postcode)";
        String multiLine = "concat($address, $nl, $suburb, '|', $state.code, '|', $postcode)";
        Lookup lookup = TestHelper.getLookup("lookup.addressformat", "TEST_ADDRESS_FORMAT");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("singleLineFormat", singleLine);
        bean.setValue("multiLineFormat", multiLine);
        bean.save();

        // associate the format with the practice
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        IMObjectBean practiceBean = new IMObjectBean(practice);
        practiceBean.setValue("addressFormat", lookup.getCode());
        PracticeService service = Mockito.mock(PracticeService.class);
        Mockito.when(service.getPractice()).thenReturn(practice);

        PracticeAddressFormatter formatter = new PracticeAddressFormatter(service, getArchetypeService(),
                                                                          getLookupService());

        // verify the format is used
        Contact contact = TestHelper.createLocationContact("123 Smith St", "RESEARCH", "VIC", "3095");
        assertEquals("123 Smith St|Research|Vic|3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nResearch|VIC|3095", formatter.format(contact, false));

        // verify the default format is used if no address format is registered
        practiceBean.setValue("addressFormat", null);
        assertEquals("123 Smith St, Research Vic 3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nResearch Vic 3095", formatter.format(contact, false));
    }
}
