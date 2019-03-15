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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.consult;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.clinician.ClinicianReferenceEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.component.business.service.archetype.helper.DescriptorHelper.getDisplayName;

/**
 * A dialog to prompt for the clinician, and update the context with the selected clinician.
 *
 * @author Tim Anderson
 */
public class ClinicianSelectionDialog extends ConfirmationDialog {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The selected clinician.
     */
    private User clinician;


    /**
     * Constructs a {@link ClinicianSelectionDialog}.
     *
     * @param context the context
     * @param title   the window title
     * @param message the message
     * @param help    the help context
     */
    public ClinicianSelectionDialog(Context context, String title, String message, HelpContext help) {
        super(title, message, help);
        this.context = context;
        this.clinician = context.getClinician();
    }

    /**
     * Returns the selected clinician.
     * <p/>
     * The dialog cannot be OK'ed until a clinician is selected.
     *
     * @return the selected clinician. May be {@code null}
     */
    public User getClinician() {
        return clinician;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create(true, true);
        message.setText(getMessage());
        IArchetypeService service = ServiceHelper.getArchetypeService();
        SimpleProperty property = new SimpleProperty("clinician", Reference.class);
        property.setDisplayName(getDisplayName(ScheduleArchetypes.APPOINTMENT, "clinician", service));
        property.setArchetypeRange(new String[]{UserArchetypes.USER});

        ClinicianReferenceEditor editor = new ClinicianReferenceEditor(
                property, null, new DefaultLayoutContext(context, getHelpContext()));
        editor.setObject(clinician);
        editor.addModifiableListener(modifiable -> {
            clinician = editor.getObject();
            getButtons().setEnabled(OK_ID, clinician != null);
        });

        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, editor.getComponent());
        Row row = RowFactory.create(Styles.LARGE_INSET, column);
        getLayout().add(row);
        FocusGroup group = getFocusGroup();
        group.add(editor.getFocusGroup());
        group.setDefault(editor.getComponent());

        if (clinician != null) {
            setDefaultButton(OK_ID);
        } else {
            getButtons().setEnabled(OK_ID, false);
        }
        group.setFocus();
    }

    @Override
    protected void onOK() {
        if (clinician != null) {
            context.setClinician(clinician);
            super.onOK();
        }
    }
}
