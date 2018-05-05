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
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Map;

import static org.openvpms.report.ReportException.ErrorCode.NoExpressionEvaluatorForType;


/**
 * Factory for {@link ExpressionEvaluator}s.
 *
 * @author Tim Anderson
 */
public class ExpressionEvaluatorFactory {

    /**
     * Creates a new evaluator for the supplied object.
     *
     * @param object    the object
     * @param fields    a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param functions the JXPath extension functions
     * @return a new evaluator for the object
     */
    public static ExpressionEvaluator create(Object object, Map<String, Object> fields, IArchetypeService service,
                                             ILookupService lookups, Functions functions) {
        if (object instanceof IMObject) {
            return new IMObjectExpressionEvaluator((IMObject) object, null, fields, service, lookups, functions);
        } else if (object instanceof ObjectSet) {
            return new ObjectSetExpressionEvaluator((ObjectSet) object, null, fields, service, lookups, functions);
        }
        throw new ReportException(NoExpressionEvaluatorForType, object.getClass().getName());
    }

}
