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

package org.openvpms.report;

import org.apache.commons.jxpath.Functions;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Map;


/**
 * Evaluates report expressions.
 * <p/>
 * Expressions may take one of two forms:
 * <ol>
 * <li>node1.node2.nodeN</li>
 * <li>[expr]</li>
 * </ol>
 * Expressions of the first type are evaluated using {@link NodeResolver};
 * the second by {@code JXPath}.
 *
 * @author Tim Anderson
 */
public class IMObjectExpressionEvaluator extends AbstractExpressionEvaluator<IMObject> {

    /**
     * The node resolver.
     */
    private NodeResolver resolver;

    /**
     * Constructs a {@link IMObjectExpressionEvaluator}.
     *
     * @param object     the object
     * @param parameters parameters available to expressions as variables. May be {@code null}
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the JXPath extension functions
     */
    public IMObjectExpressionEvaluator(IMObject object, Parameters parameters, Map<String, Object> fields,
                                       IArchetypeService service, ILookupService lookups, Functions functions) {
        super(object, parameters, fields != null ? new ResolvingPropertySet(fields, service, lookups) : null, service,
              lookups, functions);
    }

    /**
     * Constructs a {@link IMObjectExpressionEvaluator}.
     *
     * @param object     the object
     * @param resolver   the node resolver. May be {@code null}
     * @param parameters parameters available to expressions as variables. May be {@code null}
     * @param fields     fields to pass to the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the JXPath extension functions
     */
    public IMObjectExpressionEvaluator(IMObject object, NodeResolver resolver, Parameters parameters,
                                       PropertySet fields, IArchetypeService service, ILookupService lookups,
                                       Functions functions) {
        super(object, parameters, fields, service, lookups, functions);
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
            resolver = new NodeResolver(getObject(), getService(), getLookups());
        }
        return getValue(name, resolver);
    }

}
