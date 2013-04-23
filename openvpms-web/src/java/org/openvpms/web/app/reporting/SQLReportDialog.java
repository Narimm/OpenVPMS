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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.util.Variables;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.doc.ReportParameters;
import org.openvpms.web.component.print.PrintDialog;
import org.openvpms.web.component.util.GroupBoxFactory;

import java.util.Map;
import java.util.Set;


/**
 * Reporting dialog for SQL reports that accept parameters.
 *
 * @author Tim Anderson
 */
public class SQLReportDialog extends PrintDialog {

    /**
     * The report parameters.
     */
    private final ReportParameters parameters;

    /**
     * The export button identifier.
     */
    private static final String EXPORT_ID = "export";

    /**
     * The export mail button identifier.
     */
    private static final String EXPORT_MAIL_ID = "exportMail";


    /**
     * Constructs an {@code SQLReportDialog}.
     *
     * @param title      the dialog title
     * @param parameters the report parameter types
     * @param variables  variables for macro expansion
     */
    public SQLReportDialog(String title, Set<ParameterType> parameters, Variables variables) {
        super(title);
        setStyleName("SQLReportDialog");
        this.parameters = new ReportParameters(parameters, variables);
    }

    /**
     * Returns the report properties.
     *
     * @return the report properties
     */
    public Map<String, Object> getValues() {
        return parameters.getValues();
    }

    /**
     * Invoked when the 'OK' button is pressed. If the parameters are valid,
     * closes the window.
     */
    @Override
    protected void onOK() {
        if (parameters.validate()) {
            doPrint();
            super.onOK();
        }
    }

    /**
     * Invoked when the the report should be printed.
     * This implementation does nothing.
     */
    protected void doPrint() {
    }

    /**
     * Invoked when the preview button is pressed. If the parameters are valid,
     * invokes {@link #doPreview}.
     */
    @Override
    protected void onPreview() {
        if (parameters.validate()) {
            doPreview();
        }
    }

    /**
     * Invoked when the report should be previewed.
     * This implementation does nothing.
     */
    protected void doPreview() {
    }

    /**
     * Invoked when the mail button is pressed. If the parameters are valid, invokes {@link #doMail}.
     */
    @Override
    protected void onMail() {
        if (parameters.validate()) {
            doMail();
        }
    }

    /**
     * Invoked when the report should be mailed.
     * This implementation does nothing.
     */
    protected void doMail() {
    }

    /**
     * Invoked when the export button is pressed. If the parameters are valid,
     * invokes {@link #doExport}.
     */
    protected void onExport() {
        if (parameters.validate()) {
            doExport();
        }
    }

    /**
     * Invoked when the export mail button is pressed. If the parameters are valid, invokes {@link #doExportMail()}.
     */
    protected void onExportMail() {
        if (parameters.validate()) {
            doExportMail();
        }
    }

    /**
     * Invoked when the report should be exported.
     * This implementation does nothing.
     */
    protected void doExport() {
    }

    /**
     * Invoked when the report should be exported and mailed.
     * This implementation does nothing.
     */
    protected void doExportMail() {
    }

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        addButton(EXPORT_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onExport();
            }
        });

        addButton(EXPORT_MAIL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onExportMail();
            }
        });


        Component component = GroupBoxFactory.create("reporting.run.parameters",
                                                     parameters.getComponent());
        container.add(component);
    }

}
