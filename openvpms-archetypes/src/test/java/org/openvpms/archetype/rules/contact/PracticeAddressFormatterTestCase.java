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
        String singleLine = "concat($address, '|', $suburb, '|', $state, '|', $postcode)";
        String multiLine = "concat($address, $nl, $suburb, '|', $state.code, '|', $postcode)";
        PracticeService service = createPracticeService(singleLine, multiLine);

        PracticeAddressFormatter formatter = new PracticeAddressFormatter(service, getArchetypeService(),
                                                                          getLookupService());

        // verify the format is used
        Contact contact = TestHelper.createLocationContact("123 Smith St", "RESEARCH", "VIC", "3095");
        assertEquals("123 Smith St|Research|Vic|3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nResearch|VIC|3095", formatter.format(contact, false));

        // verify the default format is used if no address format is registered
        IMObjectBean bean = new IMObjectBean(service.getPractice());
        bean.setValue("addressFormat", null);
        assertEquals("123 Smith St, Research Vic 3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nResearch Vic 3095", formatter.format(contact, false));
    }

    /**
     * Creates a {@link PracticeService}, where the practice is configured to use the specified single and multi-line
     * address formats.
     *
     * @param singleLine the single line format
     * @param multiLine  the multi-line format
     * @return a new {@link PracticeService}
     */
    private PracticeService createPracticeService(String singleLine, String multiLine) {
        Lookup lookup = createAddressFormat(singleLine, multiLine);
        // associate the format with the practice
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        IMObjectBean practiceBean = new IMObjectBean(practice);
        practiceBean.setValue("addressFormat", lookup.getCode());
        PracticeService service = Mockito.mock(PracticeService.class);
        Mockito.when(service.getPractice()).thenReturn(practice);
        return service;
    }

    /**
     * Creates a <em>lookup.addressformat</em>.
     *
     * @param singleLine the single line format
     * @param multiLine  the multi-line format
     * @return the lookup
     */
    private Lookup createAddressFormat(String singleLine, String multiLine) {
        Lookup lookup = TestHelper.getLookup("lookup.addressformat", "TEST_ADDRESS_FORMAT");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("singleLineFormat", singleLine);
        bean.setValue("multiLineFormat", multiLine);
        bean.save();
        return lookup;
    }

    /**
     * Verifies that nulls properties are replaced with empty strings.
     */
    @Test
    public void testNull() {
        String singleLine = "concat($address, '|', toUpperCase($suburb), '|', $state, '|', $postcode)";
        String multiLine = "concat($address, $nl, toUpperCase($suburb), '|', $state.code, '|', $postcode)";
        PracticeService service = createPracticeService(singleLine, multiLine);

        PracticeAddressFormatter formatter = new PracticeAddressFormatter(service, getArchetypeService(),
                                                                          getLookupService());

        // verify the format is used
        Contact contact = TestHelper.createLocationContact("123 Smith St", "RESEARCH", "VIC", "3095");
        assertEquals("123 Smith St|RESEARCH|Vic|3095", formatter.format(contact, true));
        assertEquals("123 Smith St\nRESEARCH|VIC|3095", formatter.format(contact, false));

        // now set properties to null and verify no nulls appear in the formatted strings
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("address", null);
        bean.setValue("suburb", null);
        bean.setValue("postcode", null);
        bean.setValue("state", null);

        assertEquals("|||", formatter.format(contact, true));
        assertEquals("\n||", formatter.format(contact, false));
    }
}
