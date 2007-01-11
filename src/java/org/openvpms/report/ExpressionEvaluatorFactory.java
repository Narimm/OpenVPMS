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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.report.IMReportException.ErrorCode.NoExpressionEvaluatorForType;


/**
 * Factory for {@link ExpressionEvaluator}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExpressionEvaluatorFactory {

    /**
     * Creates a new evaluator for the supplied object.
     *
     * @param object  the object
     * @param service the archetype service
     * @return a new evaluator for the object
     */
    public static ExpressionEvaluator create(Object object,
                                             IArchetypeService service) {
        if (object instanceof IMObject) {
            return new IMObjectExpressionEvaluator((IMObject) object, service);
        } else if (object instanceof ObjectSet) {
            return new ObjectSetExpressionEvaluator((ObjectSet) object,
                                                    service);
        }
        throw new IMReportException(NoExpressionEvaluatorForType,
                                    object.getClass().getName());
    }
}
