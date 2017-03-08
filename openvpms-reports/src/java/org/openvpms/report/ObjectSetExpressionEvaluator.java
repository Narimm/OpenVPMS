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
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Map;


/**
 * Evaluates report expressions.
 * <p/>
 * Expressions may take one of the followingforms:
 * <ol>
 * <li>objectName</li>
 * <li>objectName.node1.node2.nodeN</li>
 * <li>[expr]</li>
 * </ol>
 *
 * @author Tim Anderson
 */
public class ObjectSetExpressionEvaluator extends AbstractExpressionEvaluator<ObjectSet> {

    /**
     * Constructs a {@link ObjectSetExpressionEvaluator}.
     *
     * @param set        the object set
     * @param parameters parameters available to expressions as variables. May be {@code null}
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the JXPath extension functions
     */
    public ObjectSetExpressionEvaluator(ObjectSet set, Parameters parameters, Map<String, Object> fields,
                                        IArchetypeService service, ILookupService lookups, Functions functions) {
        this(set, parameters, fields != null ? new ResolvingPropertySet(fields, service, lookups) : null, service,
             lookups, functions);
    }

    /**
     * Constructs a {@link ObjectSetExpressionEvaluator}.
     *
     * @param set        the object set
     * @param parameters parameters available to expressions as variables. May be {@code null}
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the JXPath extension functions
     */
    public ObjectSetExpressionEvaluator(ObjectSet set, Parameters parameters, PropertySet fields,
                                        IArchetypeService service, ILookupService lookups, Functions functions) {
        super(set, parameters, fields, service, lookups, functions);
    }

    /**
     * Returns a node value.
     *
     * @param name the node name
     * @return the node value
     */
    public Object getNodeValue(String name) {
        int index = 0;
        String objectName = name;
        String nodeName = "";
        Object object = null;
        ObjectSet set = getObject();
        while (object == null && index != -1) {
            if (set.getNames().contains(objectName)) {
                object = set.get(objectName);
                if (object instanceof IMObject) {
                    if (!StringUtils.isEmpty(nodeName)) {
                        NodeResolver resolver = new NodeResolver((IMObject) object, getService(), getLookups());
                        object = getValue(nodeName, resolver);
                        break;
                    }
                } else {
                    break;
                }
            } else {
                index = name.indexOf('.', index);
                if (index != -1) {
                    objectName = name.substring(0, index);
                    nodeName = name.substring(index + 1);
                    ++index;
                }
            }
        }
        if (index == -1) {
            object = "Invalid object/node name: " + name;
        }
        return object;
    }

}
