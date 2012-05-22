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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.visit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.web.app.patient.mr.SummaryCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * The CRUD window for editing events and their items.
 */
public class VisitCRUDWindow extends SummaryCRUDWindow {

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs a {@code VisitCRUDWindow}.
     *
     * @param context the context
     */
    public VisitCRUDWindow(Context context) {
        this.context = context;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Lays out the component.
     * <p/>
     * This implementation just returns a dummy component, as the display of the component is managed by the parent
     * dialog.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        return new Row();
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext() {
        LayoutContext context = super.createLayoutContext();
        context.setContext(this.context);
        return context;
    }

}
