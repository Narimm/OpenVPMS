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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentActEditor;
import org.openvpms.web.component.im.doc.VersionedDocumentActEditorTest;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;

import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link PatientInvestigationActEditor} class.
 *
 * @author Tim Anderson
 */
public class PatientInvestigationActEditorTestCase extends VersionedDocumentActEditorTest {

    /**
     * Verifies that if an investigation type is removed from a product, existing investigations using both
     * the product an investigation type may still be saved without triggering validation errors.
     * <p/>
     * This is required if investigation types change on a product (e.g. a different provider is used), but existing
     * investigations still need to be edited.
     */
    @Test
    public void testDeleteInvestigationTypeFromProduct() {
        DocumentAct act = (DocumentAct) TestHelper.create(InvestigationArchetypes.PATIENT_INVESTIGATION);
        Entity type = ProductTestHelper.createInvestigationType();
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("investigationType", type);
        Product product = TestHelper.createProduct();
        ProductTestHelper.addInvestigationType(product, type);

        PatientInvestigationActEditor editor = createInvestigationEditor(act);
        editor.setPatient(TestHelper.createPatient());
        editor.setInvestigationType(type);
        editor.setProduct(product);
        editor.setAuthor(TestHelper.createUser());
        editor.setLocation(TestHelper.createLocation());
        editor.save();

        EntityBean productBean = new EntityBean(product);
        productBean.removeRelationship(productBean.getRelationship(type));
        productBean.save();

        editor = createInvestigationEditor(act);
        editor.getProperty("description").setValue("Some notes to flag the editor as modified");
        assertTrue(editor.isModified());
        editor.save();
    }

    /**
     * Creates a new act.
     *
     * @return a new act
     */
    protected DocumentAct createAct() {
        DocumentAct act = (DocumentAct) TestHelper.create(InvestigationArchetypes.PATIENT_INVESTIGATION);
        Entity type = ProductTestHelper.createInvestigationType();
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("investigationType", type);
        return act;
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
        context.setPatient(TestHelper.createPatient());
        context.setUser(TestHelper.createUser());
        context.setLocation(TestHelper.createLocation());
        return new PatientInvestigationActEditor(act, null, layout);
    }

    /**
     * Creates a new document.
     *
     * @return a new document
     */
    protected Document createDocument() {
        return createImage();
    }

    /**
     * Creates an editor for an act.
     *
     * @param act the act
     * @return the new editor
     */
    private PatientInvestigationActEditor createInvestigationEditor(DocumentAct act) {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        PatientInvestigationActEditor editor = new PatientInvestigationActEditor(act, null, context);
        editor.getComponent();
        return editor;
    }

}
