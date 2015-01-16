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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.component.system.common.util.PropertyState;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;


/**
 * Abstract implementation of the {@link ExpressionEvaluator} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractExpressionEvaluator<T> implements ExpressionEvaluator {

    /**
     * The object.
     */
    private final T object;

    /**
     * Additional report fields. May be {@code null}.
     */
    private final PropertySet fields;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The JXPath context.
     */
    private JXPathContext context;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractExpressionEvaluator.class);


    /**
     * Constructs an {@link AbstractExpressionEvaluator}.
     *
     * @param object  the object
     * @param fields  additional report fields. These override any in the report. May be {@code null}
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public AbstractExpressionEvaluator(T object, PropertySet fields, IArchetypeService service,
                                       ILookupService lookups) {
        this.object = object;
        this.fields = fields;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Returns the value of an expression.
     * If the expression is of the form [expr] it will be evaluated using {@link #evaluate(String)} else it will be
     * evaluated using {@link #getNodeValue(String)}.
     *
     * @param expression the expression
     * @return the result of the expression
     */
    public Object getValue(String expression) {
        Object result;
        try {
            if (expression.startsWith("[") && expression.endsWith("]")) {
                String eval = expression.substring(1, expression.length() - 1);
                result = evaluate(eval);
            } else {
                if (fields != null && fields.exists(expression)) {
                    result = getFieldValue(expression);
                } else {
                    result = getNodeValue(expression);
                }
            }
        } catch (Exception exception) {
            log.warn("Failed to evaluate: " + expression, exception);
            // TODO localise
            result = "Expression Error";
        }
        return result;
    }

    /**
     * Returns the formatted value of an expression.
     *
     * @param expression the expression
     * @return the result of the expression
     */
    @Override
    public String getFormattedValue(String expression) {
        Object value = getValue(expression);
        if (value instanceof Date) {
            Date date = (Date) value;
            return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } else if (value instanceof Money) {
            return NumberFormat.getCurrencyInstance().format(value);
        } else if (value instanceof BigDecimal) {
            DecimalFormat format = new DecimalFormat("#,##0.00;-#,##0.00");
            return format.format(value);
        } else if (value instanceof IMObject) {
            return getValue((IMObject) value);
        } else if (value instanceof IMObjectReference) {
            return getValue((IMObjectReference) value);
        } else if (value != null) {
            return value.toString();
        }
        return null;
    }

    /**
     * Evaluates an expression.
     *
     * @param expression the expression to evaluate
     * @return the value of the expression
     */
    protected Object evaluate(String expression) {
        if (context == null) {
            context = JXPathHelper.newContext(object);
            if (fields != null) {
                for (String name : fields.getNames()) {
                    context.getVariables().declareVariable(name, fields.get(name));
                }
            }
        }
        return context.getValue(expression);
    }

    /**
     * Returns a node value.
     *
     * @param name the node name
     * @return the node value
     */
    protected abstract Object getNodeValue(String name);

    /**
     * Returns a field value.
     *
     * @param name the field name
     * @return the field value
     */
    protected Object getFieldValue(String name) {
        return getValue(fields.resolve(name));
    }

    /**
     * Returns the object.
     *
     * @return the object
     */
    protected T getObject() {
        return object;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
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
     * @param object the object. May be {@code null}
     * @return a value for the object
     */
    protected String getValue(IMObject object) {
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
     * @param ref the object reference. May be {@code null}
     * @return a value for the object
     */
    protected String getValue(IMObjectReference ref) {
        IMObject object = null;
        if (ref != null) {
            object = service.get(ref);
        }
        return getValue(object);
    }

    /**
     * Helper to return a the value of a node, handling collection nodes.
     * If the node doesn't exist, a localised message indicating this will be returned.
     *
     * @param name     the node name
     * @param resolver the node resolver
     * @return the node value
     */
    protected Object getValue(String name, PropertyResolver resolver) {
        Object result;
        try {
            PropertyState state = resolver.resolve(name);
            result = getValue(state);
        } catch (PropertyResolverException exception) {
            return exception.getLocalizedMessage();
        }
        return result;
    }

    /**
     * Returns the value of a {@link PropertyState}, handling collection nodes.
     *
     * @param state the state
     * @return the value
     */
    @SuppressWarnings("unchecked")
    private Object getValue(PropertyState state) {
        Object result = null;
        NodeDescriptor descriptor = state.getNode();
        Object value;
        if (descriptor != null && descriptor.isLookup()) {
            value = LookupHelper.getName(service, lookups, descriptor, state.getParent());
        } else {
            value = state.getValue();
        }
        if (value != null) {
            if (state.getNode() != null && state.getNode().isCollection()) {
                if (value instanceof Collection) {
                    Collection<IMObject> values = (Collection<IMObject>) value;
                    StringBuilder descriptions = new StringBuilder();
                    for (IMObject object : values) {
                        descriptions.append(getValue(object));
                        descriptions.append('\n');
                    }
                    result = descriptions.toString();
                } else if (value instanceof IMObject) {
                    // single value collection.
                    IMObject object = (IMObject) value;
                    result = getValue(object);
                } else {
                    result = value;
                }
            } else {
                result = value;
            }
        }
        return result;
    }
}
