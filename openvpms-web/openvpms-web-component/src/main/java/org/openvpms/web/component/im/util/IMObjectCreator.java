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

package org.openvpms.web.component.im.util;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.SelectionDialog;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ListBoxFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.util.StyleSheetHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * {@link IMObject} creator.
 *
 * @author Tim Anderson
 */
public final class IMObjectCreator {

    /**
     * Prevent construction.
     */
    private IMObjectCreator() {
    }

    /**
     * Create a new object of the specified archetype.
     *
     * @param shortName the archetype shortname
     * @return a new object, or {@code null} if the short name is not known
     */
    public static IMObject create(String shortName) {
        IMObject result = null;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            result = service.create(shortName);
            if (result == null) {
                String title = Messages.get("imobject.create.failed.title");
                String message = Messages.format("imobject.create.noarchetype", shortName);
                ErrorHelper.show(title, message);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.format("imobject.create.failed", shortName);
            ErrorHelper.show(title, exception);
        }
        return result;
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param type       the type of object being created, for display purposes
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     * @param help       the help context
     */
    public static void create(String type, List<String> shortNames, IMObjectCreatorListener listener,
                              HelpContext help) {
        create(type, shortNames.toArray(new String[shortNames.size()]), listener, help);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param type       the type of object being created, for display purposes
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     * @param help       the help context
     */
    public static void create(String type, String[] shortNames, final IMObjectCreatorListener listener,
                              HelpContext help) {
        create(type, shortNames, null, listener, help);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a selection dialog if needed.
     *
     * @param archetypes the set of possible archetypes to create
     * @param listener   the listener to notify
     * @param help       the help context. May be {@code null}
     */
    public static void create(Archetypes archetypes, IMObjectCreatorListener listener, HelpContext help) {
        create(archetypes.getDisplayName(), archetypes.getShortNames(), archetypes.getDefaultShortName(), listener,
               help);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param type             the type of object being created, for display purposes
     * @param shortNames       the archetype shortnames
     * @param defaultShortName the default short name. May be {@code null}
     * @param listener         the listener to notify
     * @param help             the help context. May be {@code null}
     */
    public static void create(String type, String[] shortNames, String defaultShortName,
                              final IMObjectCreatorListener listener, HelpContext help) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        if (shortNames.length == 0) {
            String title = Messages.get("imobject.create.noshortnames");
            ErrorHelper.show(title, type);
            listener.cancelled();
        } else if (shortNames.length > 1) {
            final ShortNameListModel model = new ShortNameListModel(shortNames, false);
            String title = Messages.format("imobject.create.title", type);
            String message = Messages.format("imobject.create.message", type);
            ListBox list = ListBoxFactory.create(model);
            // TODO - move into SelectionDialog.
            int height = StyleSheetHelper.getProperty("SelectionDialog.list.height", -1);
            if (height != -1) {
                list.setHeight(new Extent(height, Extent.PX));
            } else {
                list.setHeight(new Extent(12, Extent.EM));
            }
            list.setCellRenderer(new ShortNameListCellRenderer());
            if (defaultShortName != null) {
                int index = model.indexOf(defaultShortName);
                if (index != -1) {
                    list.setSelectedIndex(index);
                }
            }

            final SelectionDialog dialog = new SelectionDialog(title, message, list, help);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    int selected = dialog.getSelectedIndex();
                    if (selected != -1) {
                        IMObject object = create(model.getShortName(selected));
                        if (object != null) {
                            listener.created(object);
                        } else {
                            listener.cancelled();
                        }
                    }
                }

            });
            dialog.show();
        } else {
            IMObject object = create(shortNames[0]);
            if (object != null) {
                listener.created(object);
            } else {
                listener.cancelled();
            }
        }
    }

}
