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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;
import junit.framework.TestCase;
import org.openvpms.etl.ETLValue;

import java.util.List;


/**
 * Tests the {@link RowMapper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RowMapperTestCase extends TestCase {

    /**
     * Tests a single node.
     *
     * @throws Exception for any error
     */
    public void testSingleNode() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);

        RowMapper mapper = new RowMapper(mappings);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value firstName = createValue("FIRST_NAME", "Foo");
        Value lastName = createValue("LAST_NAME", "Bar");
        legacyId.setValue("ID1");
        row.addValue(legacyId);
        row.addValue(firstName);
        row.addValue(lastName);
        List<ETLValue> values = mapper.map(row);

        assertEquals(2, values.size());
        ETLValue firstNameVal = values.get(0);
        ETLValue lastNameVal = values.get(1);
        checkValue(firstNameVal, "ID1.1", "party.customerperson", "ID1",
                   "firstName", "Foo");
        checkValue(lastNameVal, "ID1.1", "party.customerperson", "ID1",
                   "lastName", "Bar");
    }

    /**
     * Tests a single collection node.
     *
     * @throws Exception for any error
     */
    public void testSingleCollectionNode() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping addressMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>address");
        Mapping suburbMap = createMapping(
                "SUBURB",
                "<party.customerperson>contacts[0]<contact.location>suburb");
        mappings.addMapping(addressMap);
        mappings.addMapping(suburbMap);

        RowMapper mapper = new RowMapper(mappings);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value address = createValue("ADDRESS", "49 Foo St Bar");
        Value suburb = createValue("SUBURB", "Coburg");
        legacyId.setValue("ID1");
        row.addValue(legacyId);
        row.addValue(address);
        row.addValue(suburb);
        List<ETLValue> objects = mapper.map(row);

        assertEquals(3, objects.size());

        ETLValue personVal = objects.get(0);
        ETLValue addressVal = objects.get(1);
        ETLValue suburbVal = objects.get(2);
        checkValue(personVal, "ID1.1", "party.customerperson", "ID1",
                   "contacts", 0, "ID1.2");
        checkValue(addressVal, "ID1.2", "contact.location", "ID1",
                   "address", -1, "49 Foo St Bar");
        checkValue(suburbVal, "ID1.2", "contact.location", "ID1",
                   "suburb", -1, "Coburg");
    }

    /**
     * Tests a collection heirarchy.
     *
     * @throws Exception for any error
     */
    public void testCollectionHeirarchy() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping suburbMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>purposes[1]");
        suburbMap.setIsReference(true);
        String ref = "<lookup.contactPurpose>code=MAILING";
        suburbMap.setValue(ref);
        mappings.addMapping(suburbMap);

        RowMapper mapper = new RowMapper(mappings);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value mailing = createValue("ADDRESS", "49 Foo St Bar");
        row.addValue(legacyId);
        row.addValue(mailing);
        List<ETLValue> objects = mapper.map(row);

        assertEquals(2, objects.size());

        ETLValue person = objects.get(0);
        ETLValue contact = objects.get(1);
        checkValue(person, "ID1.1", "party.customerperson", "ID1",
                   "contacts", 0, "ID1.2");
        checkValue(contact, "ID1.2", "contact.location", "ID1",
                   "purposes", 1, "<lookup.contactPurpose>code=MAILING");
    }

    /**
     * Verifies that the string <em>$value</em> is expanded with the input
     * value.
     */
    public void testValueExpansion() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping mapping = createMapping(
                "INVOICEID",
                "<actRelationship.customerAccountChargesInvoice>source");
        mapping.setValue("<act.customerAccountChargesInvoice>$value");
        mappings.addMapping(mapping);

        RowMapper mapper = new RowMapper(mappings);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value invoiceId = createValue("INVOICEID", "INVOICE1");
        row.addValue(legacyId);
        row.addValue(invoiceId);
        List<ETLValue> objects = mapper.map(row);

        assertEquals(1, objects.size());

        ETLValue value = objects.get(0);
        checkValue(value, "ID1.1",
                   "actRelationship.customerAccountChargesInvoice", "ID1",
                   "source", -1, "<act.customerAccountChargesInvoice>INVOICE1");
    }

    /**
     * Verifies that a null mapping value when the mapping is a reference
     * defaults to a legacy id reference of the form &lt;archetype&gt;legacyId
     */
    public void testDefaultReference() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping mapping = createMapping(
                "INVOICEID",
                "<act.customerAccountChargesInvoice>items[0]<actRelationship.customerAccountChargesInvoice>source");
        mapping.setIsReference(true);
        mappings.addMapping(mapping);

        RowMapper mapper = new RowMapper(mappings);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value invoiceId = createValue("INVOICEID", "INVOICE1");
        row.addValue(legacyId);
        row.addValue(invoiceId);
        List<ETLValue> objects = mapper.map(row);

        assertEquals(2, objects.size());

        ETLValue invoice = objects.get(0);
        ETLValue source = objects.get(1);
        checkValue(invoice, "ID1.1",
                   "act.customerAccountChargesInvoice", "ID1",
                   "items", 0, "ID1.2");
        assertTrue(invoice.isReference());
        checkValue(source, "ID1.2",
                   "actRelationship.customerAccountChargesInvoice", "ID1",
                   "source", -1, "<act.customerAccountChargesInvoice>INVOICE1");
        assertTrue(source.isReference());
    }

    /**
     * Helper to check a value.
     *
     * @param v         the value to check
     * @param objectId  the expected object id
     * @param archetype the expected archetype
     * @param legacyId  the expected legacy id
     * @param name      the expected name
     * @param value     the expected value
     */
    private void checkValue(ETLValue v, String objectId, String archetype,
                            String legacyId, String name, String value) {
        checkValue(v, objectId, archetype, legacyId, name, -1, value);
    }

    /**
     * Helper to check a value.
     *
     * @param v         the value to check
     * @param objectId  the expected object id
     * @param archetype the expected archetype
     * @param legacyId  the expected legacy id
     * @param name      the expected name
     * @param index     the expected index
     * @param value     the expected value
     */
    private void checkValue(ETLValue v, String objectId, String archetype,
                            String legacyId, String name, int index,
                            String value) {
        assertEquals(objectId, v.getObjectId());
        assertEquals(archetype, v.getArchetype());
        assertEquals(legacyId, v.getLegacyId());
        assertEquals(name, v.getName());
        assertEquals(index, v.getIndex());
        if (index != -1) {
            assertTrue(v.isReference());
        }
        assertEquals(value, v.getValue());
    }

    /**
     * Helper to create a new value.
     *
     * @param name  the name
     * @param value the value
     * @return a new value
     */
    private Value createValue(String name, String value) {
        Value result = new Value(name, Value.VALUE_TYPE_STRING);
        result.setValue(value);
        return result;
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target) {
        Mapping mapping = new Mapping();
        mapping.setSource(source);
        mapping.setTarget(target);
        return mapping;
    }

}
