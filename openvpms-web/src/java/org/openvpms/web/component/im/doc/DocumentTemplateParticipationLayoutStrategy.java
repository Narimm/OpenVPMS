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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Layout strategy for <em>participation.documentTemplate</em> participation
 * relationships. This navigates the entity and its corresponding
 * <code>DocumentAct</code>, enabling any associated document do be downloaded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplateParticipationLayoutStrategy
        implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, PropertySet properties,
                           IMObject parent, LayoutContext context) {
        Property property = properties.get("entity");
        IMObjectReference ref = (IMObjectReference) property.getValue();
        return new IMObjectReferenceViewer(ref, true).getComponent();
        /*
        Entity entity = (Entity) IMObjectHelper.getObject(ref);
        if (entity != null) {
            TemplateHelper.refresh(entity); // todo - workaround for OBF-105
            EntityBean bean = new EntityBean(entity);
            final DocumentAct act = (DocumentAct) bean.getParticipant(
                    "participation.document");
            if (act != null) {
                return new DocumentActDownloader(act).getComponent();
            }
        }
        return LabelFactory.create();
        */
    }

}
