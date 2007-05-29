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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.util.MacroEvaluator;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.im.edit.ValidationHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * String property transformer, that provides macro expansion.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StringPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * The macro evaluator.
     */
    private final MacroEvaluator macros;


    /**
     * Constructs a new <tt>StringTransformer</tt>.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor.
     */
    public StringPropertyTransformer(IMObject parent,
                                     NodeDescriptor descriptor) {
        super(parent, descriptor);
        macros = new MacroEvaluator(ServiceHelper.getMacroCache());
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        String result = null;
        if (object instanceof String) {
            String str = (String) object;
            result = macros.evaluate(str, getParent());
        } else if (object != null) {
            result = object.toString();
        }
        result = StringUtils.trimToNull(result);
        NodeDescriptor desc = getDescriptor();
        int minLength = desc.getMinLength();
        int maxLength = desc.getMaxLength();
        if ((result == null && minLength > 0)
                || (result != null && result.length() < minLength)) {
            String msg = Messages.get("node.error.minLength", minLength);
            throw ValidationHelper.createException(getParent(), desc, msg);
        }
        if (result != null && result.length() > maxLength) {
            String msg = Messages.get("node.error.maxLength", maxLength);
            throw ValidationHelper.createException(getParent(), desc, msg);
        }

        return result;
    }
}
