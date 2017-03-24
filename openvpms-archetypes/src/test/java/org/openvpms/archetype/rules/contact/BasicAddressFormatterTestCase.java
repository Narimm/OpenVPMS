package org.openvpms.archetype.rules.contact;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link BasicAddressFormatter}.
 *
 * @author Tim Anderson
 */
public class BasicAddressFormatterTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link BasicAddressFormatter#format(Contact, boolean)} method.
     */
    @Test
    public void testFormat() {
        BasicAddressFormatter formatter = new BasicAddressFormatter(getArchetypeService(), getLookupService());

        Contact contact = TestHelper.createLocationContact("123 Smith St", "RESEARCH", "VIC", "3095");
        assertEquals("123 Smith St, Research Vic 3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nResearch Vic 3095", formatter.format(contact, false));
    }
}
