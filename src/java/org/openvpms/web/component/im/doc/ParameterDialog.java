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

package org.openvpms.web.component.im.doc;

import echopointng.KeyStrokes;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.util.Variables;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.macro.MacroDialog;

import java.util.Map;
import java.util.Set;


/**
 * Dialog to prompt for report parameters.
 *
 * @author Tim Anderson
 */
public class ParameterDialog extends PopupDialog {

    /**
     * The report parameters.
     */
    private final ReportParameters parameters;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The 2 column dialog style.
     */
    private static final String WIDE_STYLE = "ParameterDialog2Column";

    /**
     * The 1 column dialog style.
     */
    private static final String NARROW_STYLE = "ParameterDialog1Column";


    /**
     * Constructs a {@code ParameterDialog}.
     *
     * @param title      the dialog title
     * @param parameters the report parameter types
     * @param object     object for evaluating macros against. May be {@code null}
     * @param context    the context
     * @param help       the help context
     * @param variables  the variables for macro expansion
     */
    public ParameterDialog(String title, Set<ParameterType> parameters, IMObject object, Context context,
                           HelpContext help, Variables variables) {
        super(title, null, OK_CANCEL, help);
        setModal(true);
        this.parameters = new ReportParameters(parameters, object, variables);
        this.context = context;
        String style = (parameters.size() >= 4) ? WIDE_STYLE : NARROW_STYLE;
        setStyleName(style);
        getLayout().add(this.parameters.getComponent());

        if (object != null) {
            getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onMacro();
                }
            });
        }
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

    /**
     * Displays the macros.
     */
    protected void onMacro() {
        MacroDialog dialog = new MacroDialog(context, getHelpContext());
        dialog.show();
    }

}
