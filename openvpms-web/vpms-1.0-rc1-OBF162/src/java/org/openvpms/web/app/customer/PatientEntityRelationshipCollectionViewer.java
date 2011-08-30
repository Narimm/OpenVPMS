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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionViewer;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Date;
import java.util.List;


/**
 * Viewer for collections of <em>entityRelationship.patientOwner</em> and
 * <em>entityRelationship.patientLocation</em> relationships.
 * Hides any inactive/deceased patients  if the 'hide inactive' checkbox is
 * selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEntityRelationshipCollectionViewer
        extends EntityRelationshipCollectionViewer {

    /**
     * Construct a new <code>PatientEntityRelationshipCollectionViewer</code>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     */
    public PatientEntityRelationshipCollectionViewer(
            CollectionProperty property, Party parent, LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Filters objects.
     * This implementation filters inactive objects, if {@link #hideInactive()}
     * is <code>true</code>.
     *
     * @param objects the objects to filter
     * @return the filtered objects
     */
    @Override
    protected List<IMObject> filter(List<IMObject> objects) {
        if (hideInactive()) {
            Party object = (Party) getObject();
            objects = RelationshipHelper.filterPatients(object, objects,
                                                        new Date());
        }
        return objects;
    }
}
