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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.i18n;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.oasis.ubl.common.aggregate.DocumentReferenceType;
import org.oasis.ubl.common.basic.DocumentTypeType;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.map.UBLHelper;


/**
 * Tests the {@link ESCIAdapterMessages} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCIAdapterMessagesTestCase extends AbstractESCITest {

    /**
     * Tests the {@link ESCIAdapterMessages#duplicateOrder}.
     */
    @Test
    public void testDuplicateOrder() {
        Party supplier = TestHelper.createSupplier(false);
        String message = "ESCIA-0200: Duplicate order 50 reported by supplier " + supplier.getName() + " ("
                         + supplier.getId() + ")";
        check(message, ESCIAdapterMessages.duplicateOrder(50, supplier));
    }

    /**
     * Tests the {@link ESCIAdapterMessages#unsupportedDocument} method.
     */
    @Test
    public void testUnsupportedDocument() {
        Party supplier = TestHelper.createSupplier(false);
        DocumentReferenceType ref = new DocumentReferenceType();
        ref.setID(UBLHelper.createID(10));
        ref.setDocumentType(UBLHelper.initText(new DocumentTypeType(), "Foo"));
        String message = "ESCIA-0800: Unsupported document received from supplier " + supplier.getName() + " ("
                         + supplier.getId() + "): ID=" + ref.getID().getValue() + ", DocumentType="
                         + ref.getDocumentType().getValue();
        check(message, ESCIAdapterMessages.unsupportedDocument(supplier, ref));
    }

    /**
     * Tests the {@link ESCIAdapterMessages#documentNotFound} method.
     */
    @Test
    public void testDocumentNotFound() {
        Party supplier = TestHelper.createSupplier(false);
        DocumentReferenceType ref = new DocumentReferenceType();
        ref.setID(UBLHelper.createID(10));
        ref.setDocumentType(UBLHelper.initText(new DocumentTypeType(), "OrderResponseSimple"));
        String message = "ESCIA-0801: Document not found for supplier " + supplier.getName() + " (" + supplier.getId()
                         + "): ID=" + ref.getID().getValue() + ", DocumentType=" + ref.getDocumentType().getValue();
        check(message, ESCIAdapterMessages.documentNotFound(supplier, ref));
    }

    /**
     * Verifies a message matches that expected.
     *
     * @param expected the expected message
     * @param actual   the actual message
     */
    private void check(String expected, Message actual) {
        assertEquals(expected, actual.toString());
    }
}
