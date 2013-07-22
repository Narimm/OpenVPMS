/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.apache.commons.lang.StringUtils;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.web.echo.text.TextHelper;
import org.openvpms.web.resource.i18n.Messages;


/**
 * String property transformer, that provides macro expansion for {@link IMObjectProperty} instances.
 * <p/>
 * For macro expansion to occur, the property must be editable (i.e not read-only or derived).
 *
 * @author Tim Anderson
 */
public class StringPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * Used to expand macros. May be {@code null} if macro expansion is not supported.
     */
    private final Macros macros;

    /**
     * Variables for macro expansion. May be {@code null}
     */
    private final Variables variables;

    /**
     * Determines if leading and trailing spaces and new lines should be
     * trimmed.
     */
    private boolean trim;

    /**
     * The object to evaluate macros against. May be {@code null}.
     */
    private final Object context;

    /**
     * Determines if macros should be expanded.
     */
    private boolean expandMacros;


    /**
     * Constructs a {@code StringTransformer}.
     *
     * @param property the property
     */
    public StringPropertyTransformer(Property property) {
        this(property, true);
    }

    /**
     * Constructs a {@code StringTransformer}.
     *
     * @param property the property
     * @param macros   if non-null, enables macro support for editable properties
     */
    public StringPropertyTransformer(Property property, Macros macros) {
        this(property, true, macros, getContext(property), null);
    }

    /**
     * Constructs a {@code StringTransformer}.
     *
     * @param property the property
     * @param trim     if {@code true} trim the string of leading and trailing spaces, new lines
     */
    public StringPropertyTransformer(Property property, boolean trim) {
        this(property, trim, null, null, null);
    }

    /**
     * Constructs a {@code StringTransformer}.
     *
     * @param property  the property
     * @param trim      if {@code true} trim the string of leading and trailing spaces and new lines
     * @param macros    if non-null, enables macro support for editable properties
     * @param context   the context, used for evaluating macros against. May be {@code null}
     * @param variables variables for use in macro expansion. May be {@code null}
     */
    public StringPropertyTransformer(Property property, boolean trim, Macros macros, Object context,
                                     Variables variables) {
        super(property);
        this.trim = trim;
        if (!property.isReadOnly() && !property.isDerived()) {
            this.macros = macros;
            this.context = context;
            this.variables = variables;
        } else {
            this.macros = null;
            this.context = null;
            this.variables = null;
        }
        expandMacros = macros != null;
    }

    /**
     * Determines if macros should be expanded.
     *
     * @param expand if {@code true}, macros should be expanded
     */
    public void setExpandMacros(boolean expand) {
        expandMacros = expand;
    }

    /**
     * Determines if whitespace should be trimmed.
     *
     * @param trim if {@code true} trim the string of leading and trailing spaces and new lines
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or {@code object} if no transformation is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) {
        Property property = getProperty();
        String result = null;
        if (object instanceof String) {
            String str = (String) object;
            if (TextHelper.hasControlChars(str)) {
                String msg = Messages.format("property.error.invalidchars", property.getDisplayName());
                throw new PropertyException(property, msg);
            }
            if (expandMacros && macros != null) {
                result = macros.runAll(str, context, variables);
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
            String msg = Messages.format("property.error.minLength", minLength);
            throw new PropertyException(property, msg);
        }
        if (result != null && maxLength != -1 && result.length() > maxLength) {
            String msg = Messages.format("property.error.maxLength", maxLength);
            throw new PropertyException(property, msg);
        }

        return result;
    }

    /**
     * Helper to return the context from a property.
     *
     * @param property the property
     * @return the context, or {@code null} if the property has no context
     */
    private static Object getContext(Property property) {
        return (property instanceof IMObjectProperty) ? ((IMObjectProperty) property).getObject() : null;
    }

}