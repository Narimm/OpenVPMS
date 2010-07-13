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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.util.Date;


/**
 * Editor for <em>act.patientClinicalEvent</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientClinicalEventActEditor extends AbstractActEditor {

    /**
     * Constructs a new <tt>PatientClinicalEventActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public PatientClinicalEventActEditor(Act act, IMObject parent,
                                         LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("patient", context.getContext().getPatient());
        initParticipant("worklist", context.getContext().getWorkList());
        initParticipant("location", context.getContext().getLocation());

        addStartEndTimeListeners();

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
    }

    /**
     * Save any edits.
     * <p/>
     * This uses {@link #saveObject()} to save the object prior to saving
     * any children with {@link #saveChildren()}.
     * <p/>
     * This is necessary to avoid stale object exceptions when related acts
     * are deleted.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = saveObject();
        if (saved) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PatientRecordLayoutStrategy();
    }

    /**
     * Invoked when the status changes. Sets the end time to today if the
     * status is 'Completed', or <tt>null</tt> if it is 'Pending'.
     */
    private void onStatusChanged() {
        Property status = getProperty("status");
        String value = (String) status.getValue();
        Date time = COMPLETED.equals(value) ? new Date() : null;
        setEndTime(time, false);
    }

}
