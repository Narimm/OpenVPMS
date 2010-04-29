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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.ArchetypeServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Tests the {@link IMObjectDataSource} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectDataSourceTestCase extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    @Autowired
    private DocumentHandlers handlers;


    /**
     * Tests basic data source functionality.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDataSource() throws Exception {
        IMObject object = createCustomer("Foo", "Bar");
        IMObjectDataSource ds = createDataSource(object);
        JRField firstName = createField("firstName", String.class);
        JRField lastName = createField("lastName", String.class);

        assertTrue(ds.next());
        assertEquals("Foo", ds.getFieldValue(firstName));
        assertEquals("Bar", ds.getFieldValue(lastName));
        assertFalse(ds.next());
    }

    /**
     * Tests handling of documents.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDocument() throws Exception {
        // create a new act and associate an image with it
        ActBean act = createAct("act.patientDocumentImage");
        File file = new File("src/test/images/openvpms.gif");
        Document doc = DocumentHelper.create(file, "image/gif", handlers);
        getArchetypeService().save(doc);
        act.setValue("document", doc.getObjectReference());
        IMObjectDataSource ds = createDataSource(act.getAct());

        // get the image field
        JRField document = createField("document", InputStream.class);
        InputStream stream = (InputStream) ds.getFieldValue(document);

        // verify the image contents match that expected
        FileInputStream expectedStream = new FileInputStream(file);
        int expected;
        while ((expected = expectedStream.read()) != -1) {
            assertEquals(expected, stream.read());
        }
        assertEquals(-1, stream.read());
        expectedStream.close();
        stream.close();
    }

    /**
     * Helper to create a new field.
     *
     * @param name       the field name
     * @param valueClass the field value class
     * @return a new field
     */
    private JRField createField(String name, Class valueClass) {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(valueClass);
        return field;
    }

    /**
     * Helper to create a new data source.
     *
     * @param object the object
     * @return a new data source
     */
    private IMObjectDataSource createDataSource(IMObject object) {
        return new IMObjectDataSource(object, getArchetypeService(), handlers);
    }

}
