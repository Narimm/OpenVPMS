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

package org.openvpms.web.component.im.doc;

import static org.junit.Assert.assertNotNull;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;
import java.util.Arrays;


/**
 * Helper for document tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DocumentTestHelper {

    /**
     * Creates a document from a resource.
     *
     * @param path the resource path
     * @return a new document
     */
    public static Document createDocument(String path) {
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        DocumentHandler handler = handlers.get(path, null);
        assertNotNull(handler);

        InputStream stream = DocumentTestHelper.class.getResourceAsStream(path);
        assertNotNull(stream);
        return handler.create(path, stream, null, -1);
    }

    /**
     * Creates a new <em>entity.documentTemplate</em>, associated with an <em>act.documentTemplate</em>.
     *
     * @param archetype the archetype the template is associated with
     * @return a new template
     */
    public static Entity createDocumentTemplate(String archetype) {
        Document document = createDocument("/blank.jrxml");
        Entity entity = (Entity) TestHelper.create("entity.documentTemplate");
        IMObjectBean template = new IMObjectBean(entity);
        template.setValue("name", document.getName());
        template.setValue("archetype", archetype);

        DocumentAct act = (DocumentAct) TestHelper.create("act.documentTemplate");
        act.setDocument(document.getObjectReference());
        act.setFileName(document.getName());
        act.setMimeType(document.getMimeType());
        act.setDescription(DescriptorHelper.getDisplayName(document));
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("template", entity);
        TestHelper.save(Arrays.asList(act, entity, document));
        return entity;
    }
}
