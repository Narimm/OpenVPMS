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
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import junit.framework.TestCase;
import org.openvpms.etl.ETLNode;
import org.openvpms.etl.ETLObject;
import org.openvpms.etl.ETLObjectDAO;
import org.openvpms.etl.ETLReference;
import org.openvpms.etl.ETLText;
import org.openvpms.etl.ETLValue;

import java.util.ArrayList;
import java.util.Collection;
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
        TestETLObjectDAO dao = new TestETLObjectDAO();
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);

        RowMapper mapper = new RowMapper(mappings, dao);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value firstName = createValue("FIRST_NAME", "Foo");
        Value lastName = createValue("LAST_NAME", "Bar");
        legacyId.setValue("ID1");
        row.addValue(legacyId);
        row.addValue(firstName);
        row.addValue(lastName);
        mapper.map(row);

        List<ETLObject> objects = dao.getObjects();
        assertEquals(1, objects.size());
        ETLObject object = objects.get(0);
        checkObject(object, "party.customerperson", "ID1", 2);
        checkNode(object, "firstName", "Foo");
        checkNode(object, "lastName", "Bar");
    }

    /**
     * Tests a single collection node.
     *
     * @throws Exception for any error
     */
    public void testSingleCollectionNode() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping suburbMap = createMapping(
                "SUBURB",
                "<party.customerperson>contacts[0]<contact.location>suburb");
        mappings.addMapping(suburbMap);

        TestETLObjectDAO dao = new TestETLObjectDAO();
        RowMapper mapper = new RowMapper(mappings, dao);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value suburb = createValue("SUBURB", "Coburg");
        legacyId.setValue("ID1");
        row.addValue(legacyId);
        row.addValue(suburb);
        mapper.map(row);

        List<ETLObject> objects = dao.getObjects();
        assertEquals(2, objects.size());

        ETLObject person = objects.get(0);
        ETLObject contact = objects.get(1);
        checkObject(person, "party.customerperson", "ID1", 1);
        checkCollectionNode(person, "contacts", contact);
        checkObject(contact, "contact.location", "ID1", 1);
        checkNode(contact, "suburb", "Coburg");
    }

    public void testCollectionHeirarchy() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping suburbMap = createMapping("ADDRESS",
                                          "<party.customerperson>contacts[0]<contact.location>purposes[1]");
        suburbMap.setIsReference(true);
        String ref = "<lookup.contactPurpose>code=MAILING";
        suburbMap.setValue(ref);
        mappings.addMapping(suburbMap);

        TestETLObjectDAO dao = new TestETLObjectDAO();
        RowMapper mapper = new RowMapper(mappings, dao);
        Row row = new Row();
        Value legacyId = createValue("LEGACY_ID", "ID1");
        Value mailing = createValue("ADDRESS", "49 Foo St Bar");
        legacyId.setValue("ID1");
        row.addValue(legacyId);
        row.addValue(mailing);
        mapper.map(row);

        List<ETLObject> objects = dao.getObjects();
        assertEquals(2, objects.size());

        ETLObject person = objects.get(0);
        ETLObject contact = objects.get(1);
        checkObject(person, "party.customerperson", "ID1", 1);
        checkCollectionNode(person, "contacts", contact);
        checkObject(contact, "contact.location", "ID1", 1);
        checkCollectionNode(contact, "purposes", ref);
    }

    private void checkNode(ETLObject object, String name, String value) {
        ETLNode node = object.getNode(name);
        assertNotNull(node);
        List<ETLValue> values = node.getValues();
        assertNotNull(values);
        assertEquals(1, values.size());
        ETLText text = (ETLText) values.get(0);
        assertEquals(value, text.getValue());
    }

    private void checkObject(ETLObject object, String shortName,
                             String legacyId, int size) {
        assertEquals(shortName, object.getArchetype());
        assertEquals(legacyId, object.getLegacyId());
        assertEquals(size, object.getNodes().size());
    }

    private void checkCollectionNode(ETLObject object, String name,
                                     ETLObject ... expected) {
        ETLNode node = object.getNode(name);
        assertNotNull(node);
        List<ETLValue> values = node.getValues();
        assertEquals(expected.length, values.size());
        for (ETLObject o : expected) {
            boolean found = false;
            for (ETLValue value : values) {
                assertTrue(value instanceof ETLReference);
                ETLReference reference = (ETLReference) value;
                if (o.equals(reference.getObject())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    private void checkCollectionNode(ETLObject object, String name,
                                     String ... expected) {
        ETLNode node = object.getNode(name);
        assertNotNull(node);
        List<ETLValue> values = node.getValues();
        assertEquals(expected.length, values.size());
        for (String o : expected) {
            boolean found = false;
            for (ETLValue value : values) {
                assertTrue(value instanceof ETLReference);
                ETLReference reference = (ETLReference) value;
                if (o.equals(reference.getValue())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    private Value createValue(String name, String value) {
        Value result = new Value(name, Value.VALUE_TYPE_STRING);
        result.setValue(value);
        return result;
    }

    private Mapping createMapping(String source, String target) {
        Mapping mapping = new Mapping();
        mapping.setSource(source);
        mapping.setTarget(target);
        return mapping;
    }

    private class TestETLObjectDAO extends ETLObjectDAO
            implements RowMapperListener {

        public void output(Collection<ETLObject> objects) throws
                                                          KettleException {
            save(objects);
        }

        private List<ETLObject> objects = new ArrayList<ETLObject>();

        @Override
        public void save(ETLObject object) {
            objects.add(object);
        }

        @Override
        public void save(Iterable<ETLObject> objects) {
            for (ETLObject object : objects) {
                save(object);
            }
        }

        public List<ETLObject> getObjects() {
            return objects;
        }
    }

}
