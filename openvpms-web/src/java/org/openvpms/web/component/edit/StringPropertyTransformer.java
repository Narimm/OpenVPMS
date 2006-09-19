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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.web.component.util.Macros;


/**
 * String property transformer, that provides macro expansion.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StringPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(StringPropertyTransformer.class);

    /**
     * Construct a new <code>StringTransformer</code>.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor.
     */
    public StringPropertyTransformer(IMObject parent,
                                     NodeDescriptor descriptor) {
        super(parent, descriptor);
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
            String value = (String) object;
            String macro = Macros.getMacro(value.trim());
            if (macro != null) {
                try {
                    JXPathContext ctx = JXPathHelper.newContext(getParent());
                    object = ctx.getValue(macro);
                } catch (Throwable exception) {
                    log.warn(exception);
                }
            }
        }
        if (object instanceof String) {
            result = (String) object;
        } else if (object != null) {
            result = object.toString();
        }
        return result;
    }
}
