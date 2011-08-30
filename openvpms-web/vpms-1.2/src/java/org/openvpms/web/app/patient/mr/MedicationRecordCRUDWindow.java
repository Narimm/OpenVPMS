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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.edit.medication.PatientMedicationActLayoutStrategy;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.layout.EditLayoutStrategyFactory;


/**
 * CRUD Window for patient medication acts. Only supports the editing of
 * existing acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicationRecordCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * Layout strategy factory that returns customized instances of
     * {@link PatientMedicationActLayoutStrategy}.
     */
    private static final IMObjectLayoutStrategyFactory FACTORY
            = new MedicationLayoutStrategyFactory();


    /**
     * Create a new <tt>MedicationRecordCRUDWindow</tt>.
     */
    public MedicationRecordCRUDWindow() {
        super(Archetypes.create("act.patientMedication", Act.class));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getEditButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
        }
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext() {
        LayoutContext context = new DefaultLayoutContext(true);
        context.setLayoutStrategyFactory(FACTORY);
        return context;
    }

    /**
     * Factory that invokes marks the date and product read-only on
     * {@link PatientMedicationActLayoutStrategy} instances.
     */
    private static class MedicationLayoutStrategyFactory
            extends EditLayoutStrategyFactory {

        /**
         * Creates a new layout strategy for an object.
         *
         * @param object the object to create the layout strategy for
         * @param parent the parent object. May be <tt>null</tt>
         */
        @Override
        public IMObjectLayoutStrategy create(IMObject object, IMObject parent) {
            IMObjectLayoutStrategy result = super.create(object, parent);
            if (result instanceof PatientMedicationActLayoutStrategy) {
                PatientMedicationActLayoutStrategy strategy
                        = ((PatientMedicationActLayoutStrategy) result);
                strategy.setDateReadOnly(true);
                strategy.setProductReadOnly(true);
            }
            return result;
        }

    }
}
