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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;


/**
 * Factory for {@link Reporter} instances.
 *
 * @author Tim Anderson
 */
public class ReporterFactory {

    /**
     * The report factory.
     */
    private final ReportFactory factory;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Reporter implementations.
     */
    private final ArchetypeHandlers<Reporter> reporters;

    /**
     * Document converter.
     */
    private final Converter converter;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReporterFactory.class);

    /**
     * Constructs a {@link ReporterFactory}.
     *
     * @param factory   the report factory
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param converter the document converter
     */
    public ReporterFactory(ReportFactory factory, FileNameFormatter formatter, IArchetypeService service,
                           ILookupService lookups, Converter converter) {
        this.factory = factory;
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
        this.converter = converter;
        reporters = new ArchetypeHandlers<>("ReporterFactory.properties", Reporter.class, service);
    }

    /**
     * Creates a new {@link Reporter}.
     *
     * @param object   the object to report on
     * @param template the document template
     * @param type     the expected type of the reporter
     * @return a new reporter
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject, R extends Reporter<T>> R create(T object, DocumentTemplate template, Class type) {
        R result = newInstance(object.getArchetypeId().getShortName(), object, template, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) createIMObjectReporter(object, template);
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
    public <T extends IMObject, R extends Reporter<T>> R create(Iterable<T> objects, DocumentTemplate template,
                                                                Class type) {
        R result = newInstance(template.getArchetype(), objects, template, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) createIMObjectReporter(objects, template);
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
    public <T extends IMObject, R extends Reporter<T>> R create(T object, DocumentTemplateLocator locator,
                                                                Class type) {
        R result = newInstance(object.getArchetypeId().getShortName(), object, locator, type);
        if (result == null) {

            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) createIMObjectReporter(object, locator);
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
    public <T extends IMObject, R extends Reporter> R create(Iterable<T> objects, DocumentTemplateLocator locator,
                                                             Class type) {
        R result = newInstance(locator.getShortName(), objects, locator, type);
        if (result == null) {
            if (type.isAssignableFrom(IMObjectReporter.class)) {
                result = (R) createIMObjectReporter(objects, locator);
            } else {
                throw new IllegalArgumentException("No Reporters extend " + type.getName() + " and support archetype="
                                                   + locator.getShortName());
            }
        }
        return result;
    }

    /**
     * Creates a new {@link IMObjectReporter}.
     *
     * @param object   the object to report on
     * @param template the document template
     * @return a new reporter
     */
    public <T extends IMObject> IMObjectReporter<T> createIMObjectReporter(T object, DocumentTemplate template) {
        return new IMObjectReporter<>(object, template, factory, formatter, service, lookups);
    }

    /**
     * Creates a new {@link IMObjectReporter}.
     *
     * @param object  the object to report on
     * @param locator the document template locator
     * @return a new reporter
     */
    public <T extends IMObject> IMObjectReporter<T> createIMObjectReporter(T object, DocumentTemplateLocator locator) {
        return new IMObjectReporter<>(object, locator, factory, formatter, service, lookups);
    }

    /**
     * Creates a new {@link IMObjectReporter}.
     *
     * @param objects  the objects to report on
     * @param template the document template
     * @return a new reporter
     */
    public <T extends IMObject> IMObjectReporter<T> createIMObjectReporter(Iterable<T> objects,
                                                                           DocumentTemplate template) {
        return new IMObjectReporter<>(objects, template, factory, formatter, service, lookups);
    }

    /**
     * Creates a new {@link IMObjectReporter}.
     *
     * @param objects the objects to report on
     * @param locator the document template locator
     * @return a new reporter
     */
    public <T extends IMObject> IMObjectReporter<T> createIMObjectReporter(Iterable<T> objects,
                                                                           DocumentTemplateLocator locator) {
        return new IMObjectReporter<>(objects, locator, factory, formatter, service, lookups);
    }

    /**
     * Creates a {@link ObjectSetReporter}.
     *
     * @param objects  the objects to report on
     * @param template the document template
     * @return a new reporter
     */
    public ObjectSetReporter createObjectSetReporter(Iterable<ObjectSet> objects, DocumentTemplate template) {
        return new ObjectSetReporter(objects, template, factory, formatter, service, lookups);
    }

    /**
     * Creates a {@link ObjectSetReporter}.
     *
     * @param objects the objects to report on
     * @param locator the document template locator
     * @return a new reporter
     */
    public ObjectSetReporter createObjectSetReporter(Iterable<ObjectSet> objects, DocumentTemplateLocator locator) {
        return new ObjectSetReporter(objects, locator, factory, formatter, service, lookups);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    public IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the document converter.
     *
     * @return the document converter
     */
    public Converter getConverter() {
        return converter;
    }

    /**
     * Attempts to create a new reporter.
     *
     * @param shortName the archetype short name to create a reporter for
     * @param object    the object to report on
     * @param template  the template
     * @param type      the expected type of the reporter
     * @return a new reporter, or {@code null} if no appropriate constructor can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private <R extends Reporter> R newInstance(String shortName, Object object, Object template, Class type) {
        ArchetypeHandler handler = reporters.getHandler(shortName);
        Object result = null;
        if (handler != null) {
            if (!type.isAssignableFrom(handler.getType())) {
                log.error("Reporter of type " + handler.getClass().getName() + " for archetype=" + shortName
                          + " is not an instance of " + type.getName());

            } else {
                DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
                beanFactory.registerSingleton("object", object);
                beanFactory.registerSingleton("template", template);
                beanFactory.registerSingleton("factory", factory);
                beanFactory.registerSingleton("formatter", formatter);
                beanFactory.registerSingleton("service", service);
                beanFactory.registerSingleton("lookups", lookups);

                try {
                    result = beanFactory.createBean(handler.getType(), AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR,
                                                    false);
                } catch (Throwable exception) {
                    log.error(exception, exception);
                }
            }
        }
        return (R) result;
    }

}
