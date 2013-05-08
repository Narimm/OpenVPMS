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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * Factory for {@link Reporter} instances.
 *
 * @author Tim Anderson
 */
public class ReporterFactory {

    /**
     * Reporter implementations.
     */
    private static ArchetypeHandlers<Reporter> reporters;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReporterFactory.class);


    /**
     * Creates a new {@link Reporter}.
     *
     * @param object   the object to report on
     * @param template the document template
     * @param type     the expected type of the reporter
     * @return a new reporter
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject, R extends Reporter<T>> R create(T object, DocumentTemplate template,
                                                                       Class type) {
        R result = newInstance(object.getArchetypeId().getShortName(), object, template, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) new IMObjectReporter<T>(object, template);
            } else {
                throw new IllegalArgumentException("No Reporters extend " + type.getName()
                                                   + " and support archetype="
                                                   + object.getArchetypeId().getShortName());
            }
        }
        return result;
    }

    /**
     * Creates a new {@link Reporter}.
     *
     * @param objects  the objects to report on
     * @param template the document template
     * @param type     the expected type of the reporter
     * @return a new reporter
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject, R extends Reporter<T>> R create(Iterable<T> objects, DocumentTemplate template,
                                                                       Class type) {
        R result = newInstance(template.getArchetype(), objects, template, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) new IMObjectReporter<T>(objects, template);
            } else {
                throw new IllegalArgumentException("No Reporters extend " + type.getName()
                                                   + " and support archetype=" + template.getArchetype());
            }
        }
        return result;
    }

    /**
     * Creates a new {@link Reporter}.
     *
     * @param object  the object to report on
     * @param locator the document template locator
     * @param type    the expected type of the reporter
     * @return a new reporter
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject, R extends Reporter<T>> R create(T object, DocumentTemplateLocator locator,
                                                                       Class type) {
        R result = newInstance(object.getArchetypeId().getShortName(), object, locator, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) new IMObjectReporter<T>(object, locator);
            } else {
                throw new IllegalArgumentException("No Reporters extend " + type.getName() + " and support archetype="
                                                   + object.getArchetypeId().getShortName());
            }
        }
        return result;
    }

    /**
     * Creates a new {@link Reporter}.
     *
     * @param objects the objects to report on
     * @param locator the document template locator
     * @param type    the expected type of the reporter
     * @return a new reporter
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject, R extends Reporter> R create(Iterable<T> objects,
                                                                    DocumentTemplateLocator locator, Class type) {
        R result = newInstance(locator.getShortName(), objects, locator, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) new IMObjectReporter<T>(objects, locator);
            } else {
                throw new IllegalArgumentException("No Reporters extend " + type.getName() + " and support archetype="
                                                   + locator.getShortName());
            }
        }
        return result;
    }

    /**
     * Attempts to create a new reporter.
     *
     * @param shortName the archetype short name to create a reporter for
     * @param required  the parameter that must be passed as the first argument to the constructor
     * @param optional  the paramter that may be passed as the second argument
     * @param type      the expected type of the reporter
     * @return a new reporter, or <tt>null</tt> if no appropriate constructor can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <R extends Reporter> R newInstance(String shortName, Object required, Object optional,
                                                      Class type) {
        ArchetypeHandler handler = getReporters().getHandler(shortName);
        Object result = null;
        if (handler != null) {
            try {
                try {
                    Object[] args = new Object[]{required, optional};
                    result = handler.create(args);
                } catch (NoSuchMethodException exception) {
                    try {
                        Object[] args = new Object[]{required};
                        result = handler.create(args);
                    } catch (NoSuchMethodException nested) {
                        log.error(nested, nested);
                    }
                }
                if (result != null && !type.isAssignableFrom(result.getClass())) {
                    log.error("Reporter of type " + result.getClass().getName()
                              + " is not an instance of " + type.getName());
                    result = null;
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
        return (R) result;
    }

    /**
     * Returns the reporter implementations.
     *
     * @return the reporters
     */
    private static synchronized ArchetypeHandlers<Reporter> getReporters() {
        if (reporters == null) {
            reporters = new ArchetypeHandlers<Reporter>("ReporterFactory.properties", Reporter.class);
        }
        return reporters;
    }

}
