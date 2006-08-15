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

package org.openvpms.web.component.im.edit.invoice;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMedicationActEditor extends AbstractActEditor {

    /**
     * Construct a new <code>PatientMedicationActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context. May be <code>null</code>
     */
    public PatientMedicationActEditor(Act act, Act parent,
                                      LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.patientMedication")) {
            throw new IllegalArgumentException("Invalid act type:"
                    + act.getArchetypeId().getShortName());
        }

        ActBean bean = new ActBean(parent);
        IMObjectReference product
                = bean.getParticipantRef("participation.product");
        if (TypeHelper.isA(product, "product.medication")) {
            setProduct(product);
        } else {
            setProduct(null);
        }
        setPatient(bean.getParticipantRef("participation.patient"));
    }

    /**
     * Sets the product.
     *
     * @param product the product reference. May be <code>null</code>
     */
    public void setProduct(IMObjectReference product) {
        setParticipant("product", product);
    }

    /**
     * Returns the product.
     *
     * @return the product refereence. May be <code>null</code>
     */
    public IMObjectReference getProduct() {
        return getParticipant("product");
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient reference. May be <code>null</code>
     */
    public void setPatient(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns the patient.
     *
     * @return the patient reference. May be <code>null</code>
     */
    public IMObjectReference getPatient() {
        return getParticipant("patient");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Invoked when the 'Print Label' button is pressed.
     */
    private void onPrintLabel() {
        String type = getArchetypeDescriptor().getDisplayName();
        IMObjectPrinter printer = new IMObjectReportPrinter(type);
        printer.print(getObject());
    }

    /**
     * Layout strategy that includes a 'Print Label' button to print
     * the act.
     */
    class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Lay out out the object in the specified container.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doLayout(IMObject object, PropertySet properties,
                                Component container, LayoutContext context) {
            Button button = ButtonFactory.create(
                    "printlabel", new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPrintLabel();
                }
            });

            RowLayoutData rowLayout = new RowLayoutData();
            Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
            rowLayout.setAlignment(topRight);
            rowLayout.setWidth(new Extent(100, Extent.PERCENT));
            button.setLayoutData(rowLayout);
            Row row = RowFactory.create("InsetX", button);
            ColumnLayoutData columnLayout = new ColumnLayoutData();
            columnLayout.setAlignment(topRight);
            row.setLayoutData(columnLayout);
            container.add(row);
            super.doLayout(object, properties, container, context);
        }
    }

}
