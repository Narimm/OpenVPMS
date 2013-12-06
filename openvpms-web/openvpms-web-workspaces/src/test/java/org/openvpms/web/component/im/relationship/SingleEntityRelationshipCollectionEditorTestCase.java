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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link SingleEntityRelationshipCollectionEditor} class.
 *
 * @author Tim Anderson
 */
public class SingleEntityRelationshipCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that optional relationships are excluded when there is no associated entity.
     */
    @Test
    public void testOptionalRelationship() {
        Party location = TestHelper.createLocation();
        Party stockLocation = ProductTestHelper.createStockLocation();

        IMObjectBean bean = new IMObjectBean(location);
        assertNull(bean.getNodeTargetObject("stockLocations"));

        // create a SingleEntityRelationshipCollectionEditor for a stockLocation relationship
        CollectionProperty property = createCollectionProperty(location, "stockLocations");
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(
                new LocalContext(), new HelpContext("foo", null));
        SingleEntityRelationshipCollectionEditor editor
                = new SingleEntityRelationshipCollectionEditor(property, location, layoutContext);
        editor.getComponent();
        assertTrue(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNull(bean.getNodeTargetObject("stockLocations"));

        // set a valid stock location. The relationship will be added to the location
        EntityRelationshipEditor stockLocationEditor = (EntityRelationshipEditor) editor.getCurrentEditor();
        stockLocationEditor.setTarget(stockLocation);
        assertFalse(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNotNull(bean.getNodeTargetObject("stockLocations"));

        // remove the stock location. The relationship will be removed from the location
        stockLocationEditor.setTarget(null);
        assertTrue(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNull(bean.getNodeTargetObject("stockLocations"));
    }

    /**
     * Creates a new collection property.
     *
     * @param object the parent object
     * @param name   the node name
     * @return a new collection property
     */
    private CollectionProperty createCollectionProperty(IMObject object, String name) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(object);
        assertNotNull(archetype);
        NodeDescriptor node = archetype.getNodeDescriptor(name);
        assertNotNull(node);
        return new IMObjectProperty(object, node);

    }
}
