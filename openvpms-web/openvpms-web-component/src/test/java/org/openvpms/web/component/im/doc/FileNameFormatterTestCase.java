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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link FileNameFormatter}.
 *
 * @author Tim Anderson
 */
public class FileNameFormatterTestCase extends AbstractAppTest {

    @Test
    public void testFileNameFormatter() {
        DocumentTemplate template1 = createTemplate("$file");
        FileNameFormatter formatter = new FileNameFormatter();
        assertEquals("foo", formatter.format("foo.txt", null, template1));
    }

    private DocumentTemplate createTemplate(String expression) {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        Lookup lookup = (Lookup) create(DocumentArchetypes.FILE_NAME_FORMAT);

        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        assertNull(template.getFileNameExpression());

        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("expression", expression);
        entity.addClassification(lookup);
        return new DocumentTemplate(entity, getArchetypeService());
    }
}
