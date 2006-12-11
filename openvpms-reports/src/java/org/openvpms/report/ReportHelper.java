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

package org.openvpms.report;

import java.util.Collection;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.NodeResolverException;


/**
 * Report helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportHelper {

    /**
     * Helper to return a value for an object, for display purposes.
     * If the object is a:
     * <ul>
     * <li>Participation, returns the name/description of the participating
     * Entity</li>
     * <li>EntityRelationship, returns the name/description of the target
     * entity</li>
     * <li>otherwise, returns the object's name, or its description if the name
     * is null</li>
     * <ul>
     *
     * @param object the object. May be <code>null</code>
     * @return a value for the object
     */
    public static String getValue(IMObject object) {
        String value = null;
        if (object instanceof Participation) {
            value = getValue(((Participation) object).getEntity());
        } else if (object instanceof EntityRelationship) {
            value = getValue(((EntityRelationship) object).getTarget());
        } else if (object != null) {
            value = object.getName();
            if (value == null) {
                value = object.getDescription();
            }
        }
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * Helper to return a value for an object, for display purposes.
     *
     * @param ref the object reference. May be <code>null</code>
     * @return a value for the object
     */
    public static String getValue(IMObjectReference ref) {
        IMObject object = null;
        if (ref != null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            object = ArchetypeQueryHelper.getByObjectReference(
                    service, ref);
        }
        return getValue(object);
    }

    /**
     * Helper to return a the value of a node, handling collection nodes.
     * If the node doesn't exist, a localised message indicating this will
     * be returned.
     *
     * @param name     the node name
     * @param resolver the node resolver
     * @return the node value
     */
    public static Object getValue(String name, NodeResolver resolver) {
        Object result = null;
        try {
            NodeResolver.State state = resolver.resolve(name);
            NodeDescriptor descriptor = state.getLeafNode();
            Object value = null;
            if (descriptor != null && descriptor.isLookup()) {
            	value = LookupHelper.getName(ArchetypeServiceHelper.getArchetypeService(), descriptor, state.getParent());
            }
            else {
            	value = state.getValue();            	
            }
            if (value != null) {
                if (state.getLeafNode() != null
                        && state.getLeafNode().isCollection()) {
                    if (value instanceof Collection) {
                        Collection<IMObject> values
                                = (Collection<IMObject>) value;
                        StringBuffer descriptions = new StringBuffer();
                        for (IMObject object : values) {
                            descriptions.append(ReportHelper.getValue(object));
                            descriptions.append('\n');
                        }
                        result = descriptions.toString();
                    } else {
                        // single value collection.
                        IMObject object = (IMObject) value;
                        result = ReportHelper.getValue(object);
                    }
                } else {
                    result = value;
                }
            }
        } catch (NodeResolverException exception) {
            return exception.getLocalizedMessage();
        }
        return result;
    }
}
