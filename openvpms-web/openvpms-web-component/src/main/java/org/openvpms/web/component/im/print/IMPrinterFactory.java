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

package org.openvpms.web.component.im.print;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;


/**
 * A factory for {@link IMPrinter} instances. The factory is configured to return
 * specific {@link IMPrinter} implementations based on the supplied criteria, with
 * {@link IMObjectReportPrinter} returned if no implementation matches.
 * <p>
 * The factory is configured using a <em>IMObjectPrinterFactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding printer implementations. Short names may be wildcarded
 * e.g:
 * <p>
 * <table>
 * <tr><td>party.*</td><td>org.openvpms.web.component.im.print.APrinter</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.print.BPrinter</td></tr>
 * <tr><td>act.customerAccountChargesInvoice.*</td><td>org.openvpms.web.component.im.print.CPrinter</td></tr>
 * </table>
 * <p>
 * Multiple <em>IMPrinterFactory.properties</em> may be used.
 *
 * @author Tim Anderson
 */
public class IMPrinterFactory {

    /**
     * The reporter factory.
     */
    private final ReporterFactory factory;

    /**
     * IMPrinter implementations.
     */
    private final ArchetypeHandlers<IMPrinter> printers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMPrinterFactory.class);


    /**
     * Constructs an {@link IMPrinterFactory}.
     *
     * @param factory the reporter factory
     * @param service the archetype service
     */
    public IMPrinterFactory(ReporterFactory factory, IArchetypeService service) {
        this.factory = factory;
        printers = new ArchetypeHandlers<>("IMPrinterFactory.properties", IMPrinter.class, service);
    }

    /**
     * Construct a new {@link IMPrinter}.
     * <p>
     * IMPrinter implementations must provide a public constructor accepting the object to print, and optionally a
     * document locator.
     *
     * @param object  the object to print
     * @param locator the document template locator
     * @param context the context
     * @return a new printer
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject> IMPrinter<T> create(T object, DocumentTemplateLocator locator, Context context) {
        String[] shortNames = {object.getArchetypeId().getShortName()};
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler<IMPrinter> handler = printers.getHandler(shortNames);
        IMPrinter<T> result = null;
        if (handler != null) {
            try {
                DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
                beanFactory.registerSingleton("object", object);
                beanFactory.registerSingleton("locator", locator);
                beanFactory.registerSingleton("context", context);
                beanFactory.registerSingleton("factory", factory);
                Object printer = beanFactory.createBean(handler.getType(),
                                                        AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
                if (!IMPrinter.class.isAssignableFrom(printer.getClass())) {
                    log.error("Class of type " + printer.getClass().getName()
                              + " is not an instance of " + IMPrinter.class.getName());
                    result = null;
                } else {
                    result = (IMPrinter<T>) printer;
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
        if (result == null) {
            result = createIMObjectReportPrinter(object, locator, context);
        }
        return result;
    }

    /**
     * Creates an {@link IMObjectReportPrinter}.
     *
     * @param object  the object to print
     * @param locator the template locator
     * @param context the context
     * @return a new printer
     */
    public <T extends IMObject> IMObjectReportPrinter<T> createIMObjectReportPrinter(
            T object, DocumentTemplateLocator locator, Context context) {
        return new IMObjectReportPrinter<T>(object, locator, context, factory);
    }

    /**
     * Creates an {@link IMObjectReportPrinter}.
     *
     * @param objects the object to print
     * @param locator the template locator
     * @param context the context
     * @return a new printer
     */
    public <T extends IMObject> IMObjectReportPrinter<T> createIMObjectReportPrinter(
            Iterable<T> objects, DocumentTemplateLocator locator, Context context) {
        return new IMObjectReportPrinter<T>(objects, locator, context, factory);
    }

    /**
     * Creates an {@link ObjectSetReportPrinter}.
     *
     * @param set     the set to print
     * @param locator the template locator
     * @param context the context
     */
    public ObjectSetReportPrinter createObjectSetReportPrinter(Iterable<ObjectSet> set, DocumentTemplateLocator locator,
                                                               Context context) {
        return new ObjectSetReportPrinter(set, locator, context, factory);
    }

}
