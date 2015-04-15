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
 */

package org.openvpms.web.workspace.supplier.document;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentActEditor;
import org.openvpms.web.component.im.doc.TemplatedVersionedDocumentActEditorTest;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Tests the {@link SupplierDocumentActEditor} class for <em>act.supplierDocumentLetter</em> acts.
 *
 * @author Tim Anderson
 */
public class SupplierLetterActEditorTestCase extends TemplatedVersionedDocumentActEditorTest {

    /**
     * Creates a new act.
     *
     * @return a new act
     */
    protected DocumentAct createAct() {
        return (DocumentAct) TestHelper.create(SupplierArchetypes.DOCUMENT_LETTER);
    }

    /**
     * Creates a new editor.
     *
     * @param act the act to edit
     * @return a new editor
     */
    protected DocumentActEditor createEditor(DocumentAct act) {
        DefaultLayoutContext layout = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        Context context = layout.getContext();
        context.setSupplier(TestHelper.createSupplier());
        context.setUser(TestHelper.createUser());
        return new SupplierDocumentActEditor(act, null, layout);
    }

    /**
     * Creates a new document.
     *
     * @return a new document
     */
    protected Document createDocument() {
        return createImage();
    }
}