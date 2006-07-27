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

package org.openvpms.web.app.admin.doc;

import nextapp.echo2.app.Row;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUDWindow for document templates. Enables templates to be printed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentCRUDWindow extends CRUDWindow {

    /**
     * Create a new <code>DocumentCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public DocumentCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        super.layoutButtons(buttons);
        buttons.add(getPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    protected void enableButtons(boolean enable) {
        super.enableButtons(enable);
        Row buttons = getButtons();
        if (enable) {
            if (buttons.indexOf(getPrintButton()) == -1) {
                buttons.add(getPrintButton());
            }
        } else {
            buttons.remove(getPrintButton());
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        try {
            IMObjectBean bean = new IMObjectBean(getObject());
            String shortName = getShortName(bean.getString("archetype"));
            if (shortName != null) {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                IMObject object = service.create(shortName);
                print(object);
            } else {
                ErrorDialog.show(Messages.get(
                        "admin.documentTemplate.print.noarchetype"));
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    private String getShortName(String shortName) {
        if (!StringUtils.isEmpty(shortName)) {
            String[] shortNames = DescriptorHelper.getShortNames(shortName);
            if (shortNames.length != 0) {
                return shortNames[0];
            }
        }
        return null;
    }
}
