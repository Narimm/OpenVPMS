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

package org.openvpms.web.component.print;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;

import java.util.List;

/**
 * Default implementation of {@link BatchPrinter}.
 *
 * @author Tim Anderson
 */
public class DefaultBatchPrinter<T extends IMObject> extends BatchPrinter<T> {

    /**
     * Constructs a {@link DefaultBatchPrinter}.
     *
     * @param objects the objects to print
     * @param context the context, used to locate document templates
     * @param help    the help context
     */
    public DefaultBatchPrinter(List<T> objects, Context context, HelpContext help) {
        super(objects, context, help);
    }

    /**
     * Invoked when a print fails.
     *
     * @param cause the reason for the failure
     */
    @Override
    public void failed(Throwable cause) {
        ErrorHelper.show(cause, new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                print(); // print the next document
            }
        });
    }
}
