package org.openvpms.web.workspace.reporting.report;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectVariables;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.im.report.ReportContextFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Evaluates report parameters that have a default value expression starting with "$OpenVPMS." against supplied
 * variables.
 * <p/>
 * Note that in order for parameters to be evaluated, they must be strings. This is required to avoid JasperReports
 * compilation errors.
 *
 * @author Tim Anderson
 * @see ReportContextFactory
 * @see IMObjectVariables
 */
public class ParameterEvaluator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ParameterEvaluator.class);

    /**
     * The expression prefix.
     */
    private static final String PREFIX = "$OpenVPMS.";

    /**
     * Constructs a {@link ParameterEvaluator}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public ParameterEvaluator(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Evaluates parameters with default value expressions starting with "$OpenVPMS." against the supplied variables.
     * <p/>
     * For each expression there must be a corresponding variable. Any expression that cannot be evaluated will be
     * replaced with {@code null}.
     *
     * @param parameterTypes the parameter types
     * @param variables      the variables
     * @return the parameter types, with "$OpenVPMS..." default values expressions replaced with their actual values
     */
    public Set<ParameterType> evaluate(Set<ParameterType> parameterTypes, Map<String, Object> variables) {
        Set<ParameterType> result = new LinkedHashSet<ParameterType>();
        JXPathContext context = null;
        for (ParameterType type : parameterTypes) {
            ParameterType toAdd;
            if (canEvaluate(type)) {
                if (context == null) {
                    context = createContext(variables);
                }
                String value = evaluate(context, type.getName(), type.getDefaultValue().toString());
                toAdd = new ParameterType(type.getName(), type.getType(), type.getDescription(), value);
            } else {
                toAdd = type;
            }
            result.add(toAdd);
        }
        return result;
    }

    /**
     * Determines if a parameter can be evaluated.
     *
     * @param type the parameter
     * @return {@code true} if the parameter can be evaluated
     */
    private boolean canEvaluate(ParameterType type) {
        boolean result = false;
        Object defaultValue = type.getDefaultValue();
        if (!type.isSystem() && defaultValue instanceof String && ((String) defaultValue).startsWith(PREFIX)) {
            if (String.class == type.getType()) {
                result = true;
            } else {
                log.error("Cannot evaluate parameter=" + type.getName() + ", type=" + type.getType()
                          + ", defaultValue=" + type.getDefaultValue() + ": type must be a " + String.class.getName());
            }
        }
        return result;
    }

    /**
     * Evaluates an expression.
     *
     * @param context    the JXPath context
     * @param name       the parameter name for error reporting
     * @param expression the expression to evaluate
     * @return the result of the expression. May be {@code null}
     */
    private String evaluate(JXPathContext context, String name, String expression) {
        String result = null;
        try {
            Object value = context.getValue(expression);
            if (value instanceof Date && !(value instanceof Timestamp)) {
                // convert dates to SQL Timestamps to enable strings to be used as parameters in SQL queries
                value = new Timestamp(((Date) value).getTime());
            }
            if (value != null) {
                result = value.toString();
            }
        } catch (Throwable exception) {
            log.warn("Failed to evaluate " + expression, exception);
        }
        return result;
    }

    /**
     * Creates a new JXPathContext, populated with the supplied variables.
     *
     * @param variables the variables
     * @return a new context
     */
    private JXPathContext createContext(Map<String, Object> variables) {
        IMObjectVariables objectVariables = new IMObjectVariables(service, lookups);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            objectVariables.add(entry.getKey(), entry.getValue());
        }
        JXPathContext context = JXPathHelper.newContext(new Object());
        context.setVariables(objectVariables);
        return context;
    }
}
