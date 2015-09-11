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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ProductParticipationEditor} class.
 *
 * @author Tim Anderson
 */
public class ProductParticipationEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the product reference can be updated from within the callback notifying that the product has
     * changed, and that the product text reflects the update.
     * <p>
     * This replicates what happens when a product template is selected, and is replaced with an actual product.
     */
    @Test
    public void testReplaceProductWithinUpdateCallback() {
        final Product template = TestHelper.createProduct(ProductArchetypes.TEMPLATE, null);
        final Product medication = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);
        template.setName("Z Template");
        medication.setName("Z Medication");
        save(template, medication);

        // create the participation editor
        Participation participation = (Participation) create(ProductArchetypes.PRODUCT_PARTICIPATION);
        Act parent = (Act) create(PatientArchetypes.PATIENT_MEDICATION);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        final ProductParticipationEditor editor = new ProductParticipationEditor(participation, parent, context) {
            @Override
            protected IMObjectReferenceEditor<Product> createEntityEditor(Property property) {
                return new TestReferenceEditor(this, property, getLayoutContext());
            }
        };
        editor.getComponent();
        TestReferenceEditor productEditor = (TestReferenceEditor) editor.getEditor();
        assertNotNull(editor);

        // set up the editor so the template is returned automatically when its name is entered
        productEditor.setQueryObjects(template);

        // register a listener so that when the template is entered, it is replaced with the medication
        editor.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                editor.setEntity(medication);
            }
        });

        // select the template
        productEditor.getSelector().getTextField().setText(template.getName());

        // verify it has been replaced with the medication, and the selector text has the medication name
        assertEquals(medication, editor.getEntity());
        assertEquals(medication.getName(), productEditor.getSelector().getText());
    }

    private class TestReferenceEditor extends ProductReferenceEditor {

        private List<Product> queryObjects = new ArrayList<>();

        public TestReferenceEditor(ProductParticipationEditor editor, Property property, LayoutContext context) {
            super(editor, property, context);
        }

        public void setQueryObjects(Product... objects) {
            queryObjects = Arrays.asList(objects);
        }

        /**
         * Returns the selector.
         *
         * @return the selector
         */
        @Override
        public IMObjectSelector<Product> getSelector() {
            return super.getSelector();
        }

        @Override
        protected Query<Product> createQuery(String name) {
            return new ListQuery<>(queryObjects, "product.*", Product.class);
        }
    }
}
