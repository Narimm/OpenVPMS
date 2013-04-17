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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;

import java.lang.reflect.Constructor;


/**
 * A factory for {@link EditDialog} instances.
 *
 * @author Tim Anderson
 */
public class EditDialogFactory {

    /**
     * Editor implementations.
     */
    private static ArchetypeHandlers<EditDialog> dialogs;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(EditDialogFactory.class);

    /**
     * Prevent construction.
     */
    private EditDialogFactory() {
    }

    /**
     * Creates a new dialog for an editor.
     *
     * @param editor  the editor
     * @param context the context
     * @param help    the help context
     * @return a new dialog
     */
    public static EditDialog create(IMObjectEditor editor, Context context, HelpContext help) {
        EditDialog result = null;
        String shortName = editor.getArchetypeDescriptor().getShortName();
        ArchetypeHandler handler = getDialogs().getHandler(shortName);
        if (handler != null) {
            Class type = handler.getType();
            Constructor ctor = getConstructor(type, editor, context, help);
            if (ctor != null) {
                try {
                    result = (EditDialog) ctor.newInstance(editor, context, help);
                } catch (Throwable throwable) {
                    log.error(throwable, throwable);
                }
            } else {
                log.error("No valid constructor found for class: " + type.getName());
            }
        }
        if (result == null) {
            result = new EditDialog(editor, context, help);
        }
        return result;
    }

    /**
     * Returns the dialogs.
     *
     * @return the dialogs
     */
    private static synchronized ArchetypeHandlers<EditDialog> getDialogs() {
        if (dialogs == null) {
            dialogs = new ArchetypeHandlers<EditDialog>("EditDialogFactory.properties", EditDialog.class);
        }
        return dialogs;
    }

    /**
     * Returns a constructor to construct a new dialog.
     *
     * @param type    the editor dialog type
     * @param editor  the editor
     * @param context the context
     * @param help    the help context
     * @return a constructor to construct the dialog, or {@code null} if none can be found
     */
    private static Constructor getConstructor(Class type, IMObjectEditor editor, Context context, HelpContext help) {
        Constructor[] ctors = type.getConstructors();
        for (Constructor ctor : ctors) {
            // check parameters
            Class<?>[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 3) {
                Class<?> ctorEditor = ctorTypes[0];
                Class<?> ctorContext = ctorTypes[1];
                Class<?> ctorHelp = ctorTypes[2];
                if (ctorEditor.isAssignableFrom(editor.getClass()) && ctorContext.isAssignableFrom(context.getClass())
                        && ctorHelp.isAssignableFrom(help.getClass())) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
