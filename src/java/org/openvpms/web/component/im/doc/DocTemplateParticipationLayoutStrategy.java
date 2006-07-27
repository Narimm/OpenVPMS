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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Document template participation layout strategy. This displays the associated
 * document act name and description, enabling the document do be downloaded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocTemplateParticipationLayoutStrategy
        implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, PropertySet properties,
                           LayoutContext context) {
        Property property = properties.get("act");
        IMObjectReference ref = (IMObjectReference) property.getValue();
        final DocumentAct act = (DocumentAct) IMObjectHelper.getObject(ref);
        if (act != null) {
            Button button = ButtonFactory.create();
            button.setStyleName("hyperlink");
            button.setText(act.getFileName());
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onDownload(act);
                }
            });

            Label label = LabelFactory.create();
            label.setText(act.getDescription());
            return RowFactory.create("WideCellSpacing", button, label);
        }
        return LabelFactory.create();
    }

    /**
     * Invoked when the act is selected to download the associated
     * document template.
     *
     * @param act the document act
     */
    private void onDownload(DocumentAct act) {
        DownloadHelper.download(act.getDocReference());
    }

}
