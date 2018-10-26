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

package org.openvpms.web.component.alert;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Alert dialog.
 *
 * @author Tim Anderson
 */
public class AlertDialog extends ModalDialog {

    /**
     * The alert to display.
     */
    private final Alert alert;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Edit button identifier.
     */
    private static final String EDIT_ID = "button.edit";

    /**
     * Constructs an {@link AlertDialog}.
     *
     * @param alert   the alert
     * @param context context
     * @param help    the help context
     */
    public AlertDialog(Alert alert, Context context, HelpContext help) {
        super(Messages.format("alert.dialog.title", alert.getName()), "AlertDialog", OK);
        this.alert = alert;
        this.context = context;
        this.help = help;
        if (alert.getAlert() != null) {
            addButton(EDIT_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onEdit();
                }
            });
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING);
        setTitleBackground(alert.getColour());
        setTitleForeground(alert.getTextColour());
        Act act;

        AccountType accountType = null;
        if (alert instanceof AccountTypeAlert) {
            // AccountTypeAlerts don't have an act, so create a dummy one for rendering purposes.
            IArchetypeRuleService service = ServiceHelper.getArchetypeService();
            act = (Act) service.create(CustomerArchetypes.ALERT);
            IMObjectBean bean = service.getBean(act);
            AccountTypeAlert accountTypeAlert = (AccountTypeAlert) alert;
            bean.setValue("startTime", null);
            bean.setTarget("customer", accountTypeAlert.getCustomer());
            bean.setValue("alertType", accountTypeAlert.getAlertType().getCode());
            accountType = accountTypeAlert.getAccountType();
        } else {
            act = alert.getAlert();
        }
        if (act != null) {
            IMObjectViewer viewer = new IMObjectViewer((IMObject) act, null, new ActiveAlertLayoutStrategy(accountType),
                                                       new DefaultLayoutContext(context, help));
            column.add(viewer.getComponent());
        }
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Edits the alert.
     */
    private void onEdit() {
        IMObject object = IMObjectHelper.reload((IMObject) alert.getAlert());
        if (object != null) {
            IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(
                    object, new DefaultLayoutContext(context, help));
            EditDialog dialog = EditDialogFactory.create(editor, context);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    // close this dialog as well
                    AlertDialog.this.onOK();
                }
            });
            dialog.show();
        }
    }

}
