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

package org.openvpms.web.component.im.relationship;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditorTest;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;

import java.math.BigDecimal;


/**
 * Tests the {@link EntityLinkCollectionTargetPropertyEditor} class.
 *
 * @author Tim Anderson
 */
public class EntityLinkCollectionTargetPropertyEditorTestCase extends AbstractCollectionPropertyEditorTest {

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected IMObject createParent() {
        return TestHelper.createProduct();
    }

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected String getCollectionNode() {
        return "doses";
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected CollectionPropertyEditor createEditor(CollectionProperty property, IMObject parent) {
        return new EntityLinkCollectionTargetPropertyEditor(property, (Entity) parent);
    }

    /**
     * Returns an object to add to the collection.
     *
     * @param parent the parent of the collection
     * @return a new object to add to the collection
     */
    protected IMObject createObject(IMObject parent) {
        IMObject object = create(ProductArchetypes.DOSE);
        IMObjectBean bean = new IMObjectBean(object);
        bean.setValue("name", "Z Dose");
        bean.setValue("minWeight", BigDecimal.ZERO);
        bean.setValue("maxWeight", BigDecimal.TEN);
        bean.setValue("rate", BigDecimal.TEN);
        bean.setValue("roundTo", 2);
        return object;
    }

    /**
     * Makes an object valid or invalid.
     *
     * @param object the object
     * @param valid  if {@code true}, make it valid, otherwise make it invalid
     */
    @Override
    protected void makeValid(IMObject object, boolean valid) {
        IMObjectBean bean = new IMObjectBean(object);
        bean.setValue("minWeight", valid ? BigDecimal.ZERO : null);
    }
}