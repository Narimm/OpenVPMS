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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * {@link IMObject} creator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
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
     * @return a new object, or <code>null</code> if the short name is not
     *         known
     */
    public static IMObject create(String shortName) {
        IMObject result = null;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            result = service.create(shortName);
            if (result == null) {
                String title = Messages.get("imobject.create.failed.title");
                String message = Messages.get("imobject.create.noarchetype",
                                              shortName);
                ErrorHelper.show(title, message);

            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("imobject.create.failed", shortName);
            ErrorHelper.show(title, exception);
        }
        return result;
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     */
    public static void create(String type, List<String> shortNames,
                              final IMObjectCreatorListener listener) {
        create(type, shortNames.toArray(new String[0]), listener);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     */
    public static void create(String type, String[] shortNames,
                              final IMObjectCreatorListener listener) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        if (shortNames.length == 0) {
            String title = Messages.get("imobject.create.noshortnames");
            ErrorHelper.show(title, type);
            listener.cancelled();
        } else if (shortNames.length > 1) {
            final ShortNameListModel model
                    = new ShortNameListModel(shortNames, false);
            String title = Messages.get("imobject.create.title", type);
            String message = Messages.get("imobject.create.message", type);
            ListBox list = new ListBox(model);
            list.setCellRenderer(new ShortNameListCellRenderer());
            final SelectionDialog dialog
                    = new SelectionDialog(title, message, list);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
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
