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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;


/**
 * Evaluates report expressions.
 * <p/>
 * Expressions may take one of two forms:
 * <ol>
 * <li>node1.node2.nodeN</li>
 * <li>[expr]</li>
 * </ol>
 * Expressions of the first type are evaluated using {@link NodeResolver};
 * the second by <code>JXPath</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectExpressionEvaluator extends AbstractExpressionEvaluator<IMObject> {

    /**
     * The node resolver.
     */
    private NodeResolver resolver;


    /**
     * Constructs a new <code>IMObjectExpressionEvaluator</code>.
     *
     * @param object  the object
     * @param service the archetype service
     */
    public IMObjectExpressionEvaluator(IMObject object,
                                       IArchetypeService service) {
        super(object, service);
    }

    /**
     * Constructs a new <code>IMObjectExpressionEvaluator</code>.
     *
     * @param object   the object
     * @param resolver the NodeResolver
     */
    public IMObjectExpressionEvaluator(IMObject object, NodeResolver resolver) {
        super(object, ArchetypeServiceHelper.getArchetypeService());
        this.resolver = resolver;
    }

    /**
     * Returns a node value.
     *
     * @param name the node name
     * @return the node value
     */
    protected Object getNodeValue(String name) {
        if (resolver == null) {
            resolver = new NodeResolver(object, service);
        }
        return getValue(name, resolver);
    }

}
