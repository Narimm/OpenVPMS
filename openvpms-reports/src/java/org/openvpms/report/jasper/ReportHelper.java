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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.StringUtilities;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Report helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ReportHelper.class);


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
     * Returns archetype short names from a descriptor.
     * This expands any wildcards. If the {@link NodeDescriptor#getFilter}
     * is non-null, matching shortnames are returned, otherwise matching short
     * names from {@link NodeDescriptor#getArchetypeRange()} are returned.
     *
     * @param service    the archetype service
     * @param descriptor the node descriptor
     * @return a list of short names
     */
    public static String[] getShortNames(IArchetypeService service,
                                         NodeDescriptor descriptor) {
        String filter = descriptor.getFilter();
        String[] names;
        if (!StringUtils.isEmpty(filter)) {
            names = getShortNames(service, new String[]{filter}, false);
        } else {
            names = getShortNames(service, descriptor.getArchetypeRange(),
                                  false);
        }
        return names;
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortNames  the shortNames. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(IArchetypeService service,
                                         String[] shortNames,
                                         boolean primaryOnly) {
        Set<String> result = new HashSet<String>();
        try {
            for (String shortName : shortNames) {
                List<String> matches = service.getArchetypeShortNames(
                        shortName, primaryOnly);
                result.addAll(matches);
            }
        } catch (OpenVPMSException exception) {
            _log.error(exception, exception);
        }
        return result.toArray(new String[0]);
    }

    /**
     * Determines if a set of short names match a particular archetype.
     *
     * @param shortNames the short names
     * @param type       the archetype wildcard
     */
    public static boolean matches(String[] shortNames, String type) {
        String regexp = StringUtilities.toRegEx(type);
        for (String shortName : shortNames) {
            if (!shortName.matches(regexp)) {
                return false;
            }
        }
        return true;
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
        InputStream stream = ReportHelper.class.getResourceAsStream(path);
        if (stream == null) {
            throw new JRException("Report resource not found: " + path);
        }
        return JRXmlLoader.load(stream);
    }
}
