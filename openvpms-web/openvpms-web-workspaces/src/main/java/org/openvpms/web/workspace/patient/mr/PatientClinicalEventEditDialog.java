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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Edit dialog for <em>act.patientClinicalEvent</em>s that provides support for printing.
 *
 * @author Tim Anderson
 */
public class PatientClinicalEventEditDialog extends EditDialog {

    /**
     * The print button identifier.
     */
    private static final String PRINT_ID = "print";


    /**
     * Constructs a {@code PatientClinicalEventEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public PatientClinicalEventEditDialog(IMObjectEditor editor, Context context) {
        super(editor, context);
        addButton(PRINT_ID, false);
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (PRINT_ID.equals(button)) {
            onPrint();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Prints the event.
     * <p/>
     * This only prints if the event is successfully saved first, in order for related objects to be accessible to
     * the print template.
     */
    private void onPrint() {
        if (save()) {
            try {
                List<Act> objects = new ArrayList<Act>();
                objects.add((Act) getEditor().getObject());
                Context context = getContext();
                Iterable<Act> acts = new ActHierarchyIterator<Act>(objects);
                DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(PatientArchetypes.CLINICAL_EVENT,
                                                                                     context);
                ReporterFactory factory = ServiceHelper.getBean(ReporterFactory.class);
                IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<>(acts, locator, context, factory);
                IMPrinter<Act> interactive = new InteractiveIMPrinter<>(printer, context, getHelpContext());
                interactive.print();
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }
}

