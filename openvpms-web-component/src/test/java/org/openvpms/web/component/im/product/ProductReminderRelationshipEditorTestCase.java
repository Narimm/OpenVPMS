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

package org.openvpms.web.component.im.product;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ProductReminderRelationshipEditor} class.
 *
 * @author Tim Anderson
 */
public class ProductReminderRelationshipEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the period, periodUom and interval nodes update when the reminder type changes.
     */
    @Test
    public void testDefaultFromReminderType() {
        Entity type1 = createReminderType(2, DateUnits.YEARS, false);
        Entity type2 = createReminderType(4, DateUnits.MONTHS, true);

        EntityRelationship rel = (EntityRelationship) create("entityRelationship.productReminder");
        ProductReminderRelationshipEditor editor = new ProductReminderRelationshipEditor(
                rel, null, new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null)));
        checkValues(rel, 1, DateUnits.YEARS, false);

        editor.getProperty("target").setValue(type1.getObjectReference());
        checkValues(rel, 2, DateUnits.YEARS, false);

        editor.getProperty("target").setValue(type2.getObjectReference());
        checkValues(rel, 4, DateUnits.MONTHS, true);

        // clear the reminder type. Values shouldn't change
        editor.getProperty("target").setValue(null);
        checkValues(rel, 4, DateUnits.MONTHS, true);
    }

    /**
     * Verifies that the {@link ProductReminderRelationshipEditor} is returned by {@link IMObjectEditorFactory}.
     */
    @Test
    public void testFactory() {
        EntityRelationship rel = (EntityRelationship) create("entityRelationship.productReminder");
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        IMObjectEditor editor = IMObjectEditorFactory.create(rel, context);
        assertTrue(editor instanceof ProductReminderRelationshipEditor);
    }

    /**
     * Verifies that the period, periodUom and interactive node values matches those expected.
     *
     * @param rel         the <em>entityRelationship.productReminder</em>
     * @param period      the expected period
     * @param periodUom   the expected period units
     * @param interactive the expected interactive flag
     */
    private void checkValues(EntityRelationship rel, int period, DateUnits periodUom, boolean interactive) {
        IMObjectBean bean = new IMObjectBean(rel);
        assertEquals(period, bean.getInt("period"));
        assertEquals(periodUom.toString(), bean.getString("periodUom"));
        assertEquals(interactive, bean.getBoolean("interactive"));
    }

    /**
     * Helper to create a new <em>entity.reminderType</em>.
     *
     * @param interval    the default interval
     * @param units       the default interval units
     * @param interactive determines if the reminder is interactive
     * @return a new reminder type
     */
    private Entity createReminderType(int interval, DateUnits units, boolean interactive) {
        Entity result = (Entity) create("entity.reminderType");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("name", "ZReminderType-" + System.currentTimeMillis());
        bean.setValue("defaultInterval", interval);
        bean.setValue("defaultUnits", units.toString());
        bean.setValue("interactive", interactive);
        save(result);
        return result;
    }

}
