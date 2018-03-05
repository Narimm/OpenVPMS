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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


/**
 * Editor for <em>act.patientAlert</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientAlertEditor extends AbstractActEditor {

    /**
     * Determines if matching alerts should be marked completed on save.
     */
    private boolean markCompleted = true;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;


    /**
     * Constructs a {@link PatientAlertEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientAlertEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("patient", context.getContext().getPatient());
        addStartEndTimeListeners();
        rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return (Party) getParticipant("patient");
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        setParticipant("product", product);
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        return (Product) getParticipant("product");
    }

    /**
     * Sets the alert type.
     *
     * @param alertType the alert type. May be {@code null}
     */
    public void setAlertType(Entity alertType) {
        setParticipant("alertType", alertType);
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type
     */
    public Entity getAlertType() {
        return (Entity) getParticipant("alertType");
    }

    /**
     * Determines if matching alerts should be marked completed, if the reminder is new and IN_PROGRESS when it is
     * saved.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param markCompleted if {@code true}, mark matching reminders as completed
     */
    public void setMarkMatchingAlertsCompleted(boolean markCompleted) {
        this.markCompleted = markCompleted;
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        boolean isNew = getObject().isNew();
        super.doSave();
        if (markCompleted && isNew) {
            rules.markMatchingAlertsCompleted(getObject());
        }
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        Editor alertType = getEditor("alertType");

        if (alertType != null) {
            // add a listener to update the due date when the reminder type is modified
            ModifiableListener listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onAlertTypeChanged();
                }
            };
            alertType.addModifiableListener(listener);
        }
    }

    /**
     * Invoked when the alert type changes. Updates the reason, and calculates the end time based on the alert type
     * duration, if any.
     */
    private void onAlertTypeChanged() {
        try {
            Entity alertType = getAlertType();
            if (alertType != null) {
                IMObjectBean bean = new IMObjectBean(alertType);
                String reason = bean.getString("reason");
                getProperty("reason").setValue(reason);

                Date startTime = getStartTime();
                if (startTime != null) {
                    Date endTime = null;
                    int duration = bean.getInt("duration");
                    String units = bean.getString("durationUnits");
                    if (duration > 0 && units != null) {
                        endTime = DateRules.getDate(startTime, duration, DateUnits.valueOf(units));
                    }
                    setEndTime(endTime);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}