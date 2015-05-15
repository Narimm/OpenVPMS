/*
 * Copyright (c) 2015.
 *
 * Copy Charlton IT
 *
 * All rights reserved.
 */

package org.openvpms.web.workspace.customer.export;

import org.openvpms.archetype.rules.export.ExportArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.workspace.customer.CustomerActWorkspace;

/**
 * @author benjamincharlton on 12/03/2015.
 */
public class ExportWorkspace extends CustomerActWorkspace<Act> {

    public ExportWorkspace(Context context) {
        super("customer", "export", context);
        setChildArchetypes(Act.class, ExportArchetypes.EXPORT);
    }

    protected CRUDWindow<Act> createCRUDWindow() {
        return new ExportCrudWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    @Override
    protected ActQuery<Act> createQuery() {
        return new ExportQuery(getObject());
    }
}
