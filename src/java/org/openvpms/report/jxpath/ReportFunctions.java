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

package org.openvpms.report.jxpath;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.NodeResolver;


/**
 * JXPath extension functions for reporting.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportFunctions {

    /**
     * Resolves the value at the specified node.
     *
     * @param object the expression context
     * @return the value at the specified node
     * @throws IMObjectReportException if the name is invalid
     */
    public static Object get(IMObject object, String node) {
        NodeResolver resolver = new NodeResolver(
                object, ArchetypeServiceHelper.getArchetypeService());
        return resolver.getObject(node);
    }

}
