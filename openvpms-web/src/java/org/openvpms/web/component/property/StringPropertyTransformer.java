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

package org.openvpms.web.component.property;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.util.MacroEvaluator;
import org.openvpms.web.component.util.TextHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * String property transformer, that provides macro expansion for
 * {@link IMObjectProperty} instances.
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
     * Determines if leading and trailing spaces and new lines should be
     * trimmed.
     */
    private final boolean trim;

    /**
     * The context.
     */
    private final Object context;


    /**
     * Constructs a new <tt>StringTransformer</tt>.
     *
     * @param property the property
     */
    public StringPropertyTransformer(Property property) {
        this(property, true);
    }

    /**
     * Constructs a new <tt>StringTransformer</tt>.
     *
     * @param property the property
     * @param trim     if <tt>true</tt> trim the string of leading and trailing
     *                 spaces, new lines
     */
    public StringPropertyTransformer(Property property, boolean trim) {
        super(property);
        if (property instanceof IMObjectProperty) {
            macros = new MacroEvaluator(ServiceHelper.getMacroCache());
            context = ((IMObjectProperty) property).getObject();
        } else {
            macros = null;
            context = null;
        }
        this.trim = trim;
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no transformation
     *         is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) {
        Property property = getProperty();
        String result = null;
        if (object instanceof String) {
            String str = (String) object;
            if (TextHelper.hasControlChars(str)) {
                String msg = Messages.get("property.error.invalidchars",
                                          property.getDisplayName());
                throw new PropertyException(property, msg);
            }
            if (macros != null) {
                result = macros.evaluate(str, context);
            } else {
                result = str;
            }
        } else if (object != null) {
            result = object.toString();
        }
        if (trim) {
            result = StringUtils.trimToNull(result);
        }
        int minLength = property.getMinLength();
        int maxLength = property.getMaxLength();
        if ((result == null && minLength > 0)
                || (result != null && result.length() < minLength)) {
            String msg = Messages.get("property.error.minLength", minLength);
            throw new PropertyException(property, msg);
        }
        if (result != null && result.length() > maxLength) {
            String msg = Messages.get("property.error.maxLength", maxLength);
            throw new PropertyException(property, msg);
        }

        return result;
    }

}