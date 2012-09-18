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
 * String property transformer, that provides macro expansion for {@link IMObjectProperty} instances.
 * <p/>
 * For macro expansion to occur, the property must be editable (i.e not read-only or derived).
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
    private boolean trim;

    /**
     * The context.
     */
    private final Object context;

    /**
     * Determines if macros should be expanded.
     */
    private boolean expandMacros;


    /**
     * Constructs a <tt>StringTransformer</tt>.
     *
     * @param property the property
     */
    public StringPropertyTransformer(Property property) {
        this(property, true);
    }

    /**
     * Constructs a <tt>StringTransformer</tt>.
     *
     * @param property the property
     * @param trim     if <tt>true</tt> trim the string of leading and trailing spaces, new lines
     */
    public StringPropertyTransformer(Property property, boolean trim) {
        this(property, getContext(property), trim);
    }

    /**
     * Constructs a <tt>StringTransformer</tt>.
     *
     * @param property the property
     * @param context  the context, used for evaluating macros. If <tt>null</tt>, macros won't be evaluated
     * @param trim     if <tt>true</tt> trim the string of leading and trailing spaces and new lines
     */
    public StringPropertyTransformer(Property property, Object context, boolean trim) {
        super(property);
        this.context = context;
        if (context != null && !property.isReadOnly() && !property.isDerived()) {
            macros = new MacroEvaluator(ServiceHelper.getMacroCache());
        } else {
            macros = null;
        }
        expandMacros = macros != null;
        this.trim = trim;
    }

    /**
     * Determines if macros should be expanded.
     *
     * @return <tt>true</tt> if macros should be expanded, otherwise <tt>false</tt>
     */
    public boolean getExpandMacros() {
        return expandMacros;
    }

    /**
     * Determines if macros should be expanded.
     *
     * @param expand if <tt>true</tt>, macros should be expanded
     */
    public void setExpandMacros(boolean expand) {
        expandMacros = expand;
    }

    /**
     * Returns the macro evaluator.
     *
     * @return the macro evaluator. May be <tt>null</tt>
     */
    public MacroEvaluator getMacroEvaluator() {
        return macros;
    }

    /**
     * Determines if whitespace should be trimmed.
     *
     * @param trim if <tt>true</tt> trim the string of leading and trailing spaces and new lines
     */
    public void setTrim(boolean trim) {
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
            if (expandMacros && macros != null) {
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
        if (result != null && maxLength != -1 && result.length() > maxLength) {
            String msg = Messages.get("property.error.maxLength", maxLength);
            throw new PropertyException(property, msg);
        }

        return result;
    }

    /**
     * Helper to return the context from a property.
     *
     * @param property the property
     * @return the context, or <tt>null</tt> if the property has no context
     */
    private static Object getContext(Property property) {
        return (property instanceof IMObjectProperty) ? ((IMObjectProperty) property).getObject() : null;
    }

}