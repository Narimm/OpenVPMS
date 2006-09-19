/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.edit;

import junit.framework.Assert;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;


/**
 * {@link NumericPropertyTransformer} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyTransformerTestCase
        extends AbstractAppTest {

    /**
     * Integer node.
     */
    private NodeDescriptor _intNode;

    /**
     * BigDecimal node.
     */
    private NodeDescriptor _decNode;


    /**
     * Tests {@link NumericPropertyTransformer#apply} for an integer node.
     */
    public void testIntegerApply() {
        final Integer one = 1;
        NumericPropertyTransformer handler = new NumericPropertyTransformer(
                _intNode);

        // test string conversions
        try {
            handler.apply("abc");
        } catch (ValidationException exception) {
            assertFalse(exception.getErrors().isEmpty());
        }

        Integer int1 = (Integer) handler.apply("1");
        assertEquals(one, int1);

        try {
            handler.apply("1.0");
        } catch (ValidationException exception) {
            // see comments in testIntegerIsValid
            assertFalse(exception.getErrors().isEmpty());
        }

        // test numeric conversions
        assertEquals(one, handler.apply(new Long(1)));
        assertEquals(one, handler.apply(new BigDecimal(1.0)));
        assertEquals(one, handler.apply(new Double(1.5)));
    }

    /**
     * Tests {@link NumericPropertyTransformer#apply} for a BigDecimal node.
     */
    public void testDecimalApply() {
        final BigDecimal one = new BigDecimal("1.0");
        final BigDecimal half = new BigDecimal("0.5");
        NumericPropertyTransformer handler = new NumericPropertyTransformer(
                _decNode);

        // test string conversions
        try {
            handler.apply("abc");
        } catch (ValidationException exception) {
            assertFalse(exception.getErrors().isEmpty());
        }

        // Note: BigDecimal.compareTo() is used instead of equals as equals
        // considers equal values with different scales to be different.
        BigDecimal dec1 = (BigDecimal) handler.apply("0.5");
        assertTrue(half.compareTo(dec1) == 0);

        BigDecimal dec2 = (BigDecimal) handler.apply("1.0");
        assertTrue(one.compareTo(dec2) == 0);

        // test numeric conversions
        BigDecimal dec3 = (BigDecimal) handler.apply(new Long(1));
        assertTrue(one.compareTo(dec3) == 0);

        BigDecimal dec4 = (BigDecimal) handler.apply(new BigDecimal(0.5));
        assertTrue(half.compareTo(dec4) == 0);

        BigDecimal dec5 = (BigDecimal) handler.apply(new Double(0.5));
        assertTrue(half.compareTo(dec5) == 0);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        // get the node descriptors
        _intNode = getDescriptor("party.organisationSchedule", "slotSize");
        Assert.assertEquals(Integer.class, _intNode.getClazz());

        _decNode = getDescriptor("act.patientWeight", "weight");
        Assert.assertEquals(BigDecimal.class, _decNode.getClazz());
    }

    /**
     * Helper to return a node descriptor.
     *
     * @param archetype the archetype name
     * @param node      the node name
     * @return the node descriptor
     */
    protected NodeDescriptor getDescriptor(String archetype, String node) {
        ArchetypeDescriptor type
                = DescriptorHelper.getArchetypeDescriptor(archetype);
        assertNotNull(type);
        NodeDescriptor result = type.getNodeDescriptor(node);
        assertNotNull(result);
        return result;
    }

}
