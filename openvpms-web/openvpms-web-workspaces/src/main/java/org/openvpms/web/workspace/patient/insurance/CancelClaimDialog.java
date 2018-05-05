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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Dialog for prompting to cancel insurance claims.
 *
 * @author Tim Anderson
 */
public class CancelClaimDialog extends ConfirmationDialog {

    /**
     * The cancellation message to store/submit with the cancellation.
     */
    private final SimpleProperty reason;

    /**
     * Constructs a {@link CancelClaimDialog}.
     *
     * @param title   the window title
     * @param message the message
     * @param help    the help context
     */
    public CancelClaimDialog(String title, String message, HelpContext help) {
        super(title, message, YES_NO, help);

        reason = new SimpleProperty("message", null, String.class, Messages.get("patient.insurance.cancel.reason"));
        reason.setRequired(true);
        NodeDescriptor node = DescriptorHelper.getNode(InsuranceArchetypes.CLAIM, "message",
                                                       ServiceHelper.getArchetypeService());
        if (node != null) {
            reason.setMaxLength(node.getMaxLength());
        }
    }

    /**
     * Returns the reason for the cancellation.
     *
     * @return the reason for the cancellation
     */
    public String getReason() {
        return reason.getString();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create(true, true);
        message.setStyleName(Styles.BOLD);
        message.setText(getMessage());

        ComponentGrid grid = new ComponentGrid();
        grid.add(message, 2);
        grid.add(new ComponentState(BoundTextComponentFactory.createTextArea(reason, 40, 5), reason));

        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid.createGrid()));
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes the window.
     */
    @Override
    protected void onYes() {
        Validator validator = new DefaultValidator();
        if (reason.validate(validator)) {
            super.onYes();
        } else {
            ErrorDialog.show(Messages.format("property.error.required", reason.getDisplayName()));
        }
    }

}
