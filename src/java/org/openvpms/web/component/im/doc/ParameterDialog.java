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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.dialog.PopupDialog;

import java.util.Map;
import java.util.Set;


/**
 * Dialog to prompt for report parameters.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParameterDialog extends PopupDialog {

    /**
     * The report parameters.
     */
    private final ReportParameters parameters;

    /**
     * The 2 column dialog style.
     */
    private static final String WIDE_STYLE = "ParameterDialog2Column";

    /**
     * The 1 column dialog style.
     */
    private static final String NARROW_STYLE = "ParameterDialog1Column";


    /**
     * Constructs a new <tt>ParameterDialog</tt>.
     *
     * @param title      the dialog title
     * @param parameters the report parameter types
     * @param context    context object for evaluating macros against. If <tt>null</tt> macro expansion is disabled
     */
    public ParameterDialog(String title, Set<ParameterType> parameters, IMObject context) {
        super(title, null, OK_CANCEL);
        setModal(true);
        this.parameters = new ReportParameters(parameters, context);
        String style = (parameters.size() >= 4) ? WIDE_STYLE : NARROW_STYLE;
        setStyleName(style);
        getLayout().add(this.parameters.getComponent());
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
            super.onOK();
        }
    }

}
