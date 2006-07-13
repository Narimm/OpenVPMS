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
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;

import java.io.InputStream;
import java.math.BigDecimal;


/**
 * Report helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportHelper {

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
