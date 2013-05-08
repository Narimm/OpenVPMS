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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;


/**
 * Layout strategy for <em>document.participation</em> participation
 * relationships. This displays the associated document act name and
 * description, enabling the document do be downloaded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentParticipationLayoutStrategy
    implements IMObjectLayoutStrategy {

    /**
     * Pre-registers a component for inclusion in the layout.
     * <p/>
     * This implementation is a no-op.
     *
     * @param state the component state
     */
    public void addComponent(ComponentState state) {
        // do nothing
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        Property property = properties.get("act");
        IMObjectReference ref = (IMObjectReference) property.getValue();
        final DocumentAct act = (DocumentAct) context.getCache().get(ref);
        Component component;
        if (act != null && act.getDocument() != null) {
            DocumentActDownloader downloader = new DocumentActDownloader(act);

            // wrap in a row to left justify
            component = RowFactory.create(downloader.getComponent());
        } else {
            component = LabelFactory.create();
        }
        return new ComponentState(component);
    }

}
