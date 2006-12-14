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

package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.web.component.im.edit.ValidationHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Validator for numeric nodes..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyTransformer implements PropertyTransformer {

    /**
     * The node descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * The type converter.
     */
    private static final OpenVPMSTypeConverter CONVERTER
            = new OpenVPMSTypeConverter();


    /**
     * Constructs a new <code>NumericPropertyTransformer</code>.
     *
     * @param descriptor the node descriptor
     */
    public NumericPropertyTransformer(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Transform an object to the required type, performing validation.
     * <p/>
     * Notes:
     * <ul>
     * <li>conversion from one numeric type to another may result
     * in loss of precision, without error</li>
     * <li>conversion from a string to an integer type will produce a
     * ValidationException if the string contains a decimal point.</li>
     * </ul>
     * The inconsistency is tolerable in that all user input is via strings
     * and implicit conversion is not desired.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        Object result;
        try {
            Class type = descriptor.getClazz();
            result = CONVERTER.convert(object, type);
        } catch (Throwable exception) {
            String message = Messages.get("node.error.invalidnumeric",
                                          descriptor.getDisplayName());
            throw ValidationHelper.createException(descriptor, message,
                                                   exception);
        }

        return result;
    }

}
