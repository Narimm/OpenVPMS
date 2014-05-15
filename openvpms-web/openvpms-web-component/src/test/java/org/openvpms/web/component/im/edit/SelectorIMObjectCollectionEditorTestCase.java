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

package org.openvpms.web.component.im.edit;

import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link SelectorIMObjectCollectionEditor} class.
 *
 * @author Tim Anderson
 */
public class SelectorIMObjectCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link SelectorIMObjectCollectionEditor#add(IMObject)}
     * and {@link SelectorIMObjectCollectionEditor#remove(IMObject)} methods.
     */
    @Test
    public void testAddRemove() {
        Party customer = TestHelper.createCustomer(false);
        LayoutContext layoutContext = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        PropertySet set = new PropertySet(customer, layoutContext);
        CollectionProperty property = (CollectionProperty) set.get("type");
        SelectorIMObjectCollectionEditor editor
                = new SelectorIMObjectCollectionEditor(property, customer, layoutContext);
        final MutableInt modCount = new MutableInt(0);

        editor.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                modCount.setValue(modCount.intValue() + 1);
            }
        });

        assertFalse(editor.isModified());
        Lookup accountType = TestHelper.getLookup("lookup.customerAccountType", "BAD_DEBT");
        editor.add(accountType);
        assertEquals(1, modCount.intValue());

        assertEquals(1, property.getValues().size());
        assertTrue(property.getValues().contains(accountType));
        assertTrue(editor.isModified());
        assertTrue(editor.isValid());
        assertFalse(editor.isSaved());

        assertTrue(editor.save());
        assertTrue(editor.isSaved());
        assertFalse(editor.isModified());

        editor.remove(accountType);
        assertTrue(property.getValues().isEmpty());
        assertEquals(2, modCount.intValue());
        assertTrue(editor.isModified());
        assertTrue(editor.isValid());
    }
}
