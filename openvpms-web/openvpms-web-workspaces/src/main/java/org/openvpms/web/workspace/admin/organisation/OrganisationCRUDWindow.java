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

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.eftpos.EFTPOSService;
import org.openvpms.eftpos.Terminal;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectDeleter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * CRUD window for the Organisation workspace.
 *
 * @author Tim Anderson
 */
public class OrganisationCRUDWindow extends ResultSetCRUDWindow<Entity> {

    /**
     * Button identifier to register an EFTPOS terminal.
     */
    private static final String REGISTER_ID = "button.register";

    /**
     * Constructs an {@link OrganisationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public OrganisationCRUDWindow(Archetypes<Entity> archetypes, Query<Entity> query, ResultSet<Entity> set,
                                  Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(REGISTER_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onRegister();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(REGISTER_ID, enable && TypeHelper.isA(getObject(), "entity.EFTPOSTerminal*"));
    }

    /**
     * Creates a deleter to delete an object.
     * <p>
     * This ensures that entity.job* can be deleted rather than deactivated.
     *
     * @param object the object to delete
     * @return a new deleter
     */
    @Override
    protected IMObjectDeleter createDeleter(Entity object) {
        IMObjectDeleter deleter = super.createDeleter(object);
        if (TypeHelper.isA(object, "entity.job*")) {
            deleter.setExcludeRelationships("entityRelationship.jobUser");
        }
        return deleter;
    }

    /**
     * Registers an EFTPOS terminal.
     */
    private void onRegister() {
        Entity config = IMObjectHelper.reload(getObject());
        if (config != null) {
            final String title = Messages.get("admin.eftpos.register.title");
            try {
                final Terminal terminal = getTerminal(config);
                if (terminal != null) {
                    if (terminal.isRegistered()) {
                        ConfirmationDialog.show(title, Messages.get("admin.eftpos.register.alreadyregistered"),
                                                ConfirmationDialog.YES_NO, new PopupDialogListener() {
                                    @Override
                                    public void onYes() {
                                        register(terminal, title);
                                    }
                                });
                    } else {
                        register(terminal, title);
                    }
                } else {
                    ErrorDialog.show(title, Messages.get("admin.eftpos.register.noprovider"));
                }
            } catch (Exception exception) {
                ErrorHelper.show(title, exception);
            }
        }
    }

    private void register(Terminal terminal, String title) {
        try {
            terminal.register();
            InformationDialog.show(title, Messages.get("admin.eftpos.register.registered"));
        } catch (Exception exception) {
            ErrorHelper.show(title, exception);
        }
    }

    private Terminal getTerminal(Entity config) {
        Terminal terminal = null;
        PluginManager manager = ServiceHelper.getBean(PluginManager.class);
        List<EFTPOSService> services = manager.getServices(EFTPOSService.class);
        for (EFTPOSService service : services) {
            if (config.getArchetypeId().getShortName().equals(service.getConfigurationType())) {
                terminal = service.getTerminal(config);
                break;
            }
        }
        return terminal;
    }
}
