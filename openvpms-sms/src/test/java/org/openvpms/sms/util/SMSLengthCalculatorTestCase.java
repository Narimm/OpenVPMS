package org.openvpms.sms.util;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SMSLengthCalculator}.
 *
 * @author Tim Anderson
 */
public class SMSLengthCalculatorTestCase {

    /**
     * Tests the {@link SMSLengthCalculator#getParts(String)} method.
     */
    @Test
    public void testGetParts() {
        // check parts for messages that will be encoded as GSM
        assertEquals(1, SMSLengthCalculator.getParts(""));
        assertEquals(1, SMSLengthCalculator.getParts(StringUtils.repeat("x", 160)));
        assertEquals(2, SMSLengthCalculator.getParts(StringUtils.repeat("x", 161)));
        assertEquals(2, SMSLengthCalculator.getParts(StringUtils.repeat("x", 306)));
        assertEquals(3, SMSLengthCalculator.getParts(StringUtils.repeat("x", 307)));
        assertEquals(3, SMSLengthCalculator.getParts(StringUtils.repeat("x", 459)));
        assertEquals(4, SMSLengthCalculator.getParts(StringUtils.repeat("x", 460)));
        assertEquals(4, SMSLengthCalculator.getParts(StringUtils.repeat("x", 612)));
        assertEquals(5, SMSLengthCalculator.getParts(StringUtils.repeat("x", 613)));

        // checks parts for messages that will be encoded as UCS-2
        assertEquals(1, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 70)));
        assertEquals(2, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 71)));
        assertEquals(2, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 134)));
        assertEquals(3, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 135)));
        assertEquals(3, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 201)));
        assertEquals(4, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 202)));
        assertEquals(4, SMSLengthCalculator.getParts(StringUtils.repeat("\u0080", 268)));
    }

    /**
     * Tests the {@link SMSLengthCalculator#getGSMLength(String)} message.
     */
    @Test
    public void testGSMLength() {
        assertEquals(0, SMSLengthCalculator.getGSMLength(""));
        assertEquals(1, SMSLengthCalculator.getGSMLength("x"));
        assertEquals(1, SMSLengthCalculator.getGSMLength("\u00E8"));     // LATIN SMALL LETTER E WITH GRAVE
        assertEquals(2, SMSLengthCalculator.getGSMLength("\u20AC"));     // EURO SIGN - escaped
        assertEquals(-1, SMSLengthCalculator.getGSMLength("\u0080"));    // can't be encoded as GSM

        // now go through each mapping and verify the correct length is reported
        for (char[] mapping : SMSLengthCalculator.GSM_TO_UNICODE) {
            char unicode = mapping[1];
            char gsm = mapping[0];
            int length = SMSLengthCalculator.getGSMLength("" + unicode);
            if (gsm > 0x7F) {
                // escaped
                assertEquals(2, length);
            } else {
                assertEquals(1, length);
            }
        }
    }

}
