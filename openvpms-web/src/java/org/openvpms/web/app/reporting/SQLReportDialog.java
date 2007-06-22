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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.property.Property;

import java.util.List;
import java.util.Map;


/**
 * Reporting dialog for SQL reports that accept parameters.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SQLReportDialog extends PrintDialog {

    /**
     * The report parameters.
     */
    private final ReportParameters parameters;


    /**
     * Constructs a new <tt>ReportDialog</tt>.
     *
     * @param title      the dialog title
     * @param properties the report properties
     */
    public SQLReportDialog(String title, List<Property> properties) {
        super(title);
        setStyleName("SQLReportDialog");
        parameters = new ReportParameters(properties);
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
     * Lays out the dialog.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        container.add(parameters.getComponent());
    }

}
