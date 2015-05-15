package org.openvpms.web.workspace.customer.export;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.help.HelpContext;

/**
 * @author benjamincharlton on 14/05/2015.
 */
public class ExportCrudWindow extends ActCRUDWindow<Act> {
    public ExportCrudWindow(Archetypes<Act> archetypes, Context context, HelpContext help){
        super(archetypes, new ActActions<Act>() {
        }, context, help);
    }
}
