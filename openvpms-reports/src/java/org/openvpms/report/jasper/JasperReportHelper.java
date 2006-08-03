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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.io.InputStream;
import java.math.BigDecimal;


/**
 * Jasper Report helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class JasperReportHelper {

    /**
     * Returns the value class of a node.
     *
     * @param descriptor the node descriptor
     */
    public static Class getValueClass(NodeDescriptor descriptor) {
        if (descriptor.isMoney()) {
            return BigDecimal.class;
        } else if (descriptor.isCollection()
                || descriptor.isObjectReference()) {
            return String.class;
        }
        return descriptor.getClazz();
    }

    /**
     * Loads a report resource.
     *
     * @param path the resource path
     * @return the design corresponding to <code>path</code>
     * @throws JRException if the resource can't be loaded
     */
    public static JasperDesign getReportResource(String path)
            throws JRException {
        InputStream stream = JasperReportHelper.class.getResourceAsStream(path);
        if (stream == null) {
            throw new JRException("Report resource not found: " + path);
        }
        return JRXmlLoader.load(stream);
    }
}
