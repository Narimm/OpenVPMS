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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.act.PatientActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

/**
 * An editor for {@link Act}s which have an archetype of <em>act.patientReminder</em>.
 *
 * @author Tim Anderson
 */

public class ReminderEditor extends PatientActEditor {

    /**
     * Constructs a <tt>ReminderEditor</tt>.
     *
     * @param act     the reminder act
     * @param parent  the parent. May be <tt>null</tt>
     * @param context the layout context
     */
    public ReminderEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, ReminderArchetypes.REMINDER)) {
            throw new IllegalArgumentException(
                    "Invalid act type:" + act.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType the reminder type. May be <tt>null</tt>
     */
    public void setReminderType(Entity reminderType) {
        setParticipant("reminderType", reminderType);
    }

    /**
     * Returns the reminder type.
     *
     * @return the reminder type
     */
    public Entity getReminderType() {
        return (Entity) getParticipant("reminderType");
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be <tt>null</tt>
     */
    public void setProduct(Product product) {
        setParticipant("product", product);
    }

    /**
     * Returns the product.
     *
     * @return the product. May be <tt>null</tt>
     */
    public Product getProduct() {
        return (Product) getParticipant("product");
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        Editor editor = getEditor("reminderType");

        if (editor != null) {
            // add a listener to update the due date when the reminder type is modified
            ModifiableListener listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onReminderTypeChanged();
                }
            };
            editor.addModifiableListener(listener);
        }
    }

    /**
     * Updates the Due Date based on the reminderType reminder interval.
     */
    private void onReminderTypeChanged() {
        try {
            ReminderRules rules = new ReminderRules(ServiceHelper.getArchetypeService(),
                                                    new PatientRules(ServiceHelper.getArchetypeService(),
                                                                     ServiceHelper.getLookupService()));
            rules.calculateReminderDueDate((Act) getObject());
            Property property = getProperty("endTime");
            property.refresh();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
