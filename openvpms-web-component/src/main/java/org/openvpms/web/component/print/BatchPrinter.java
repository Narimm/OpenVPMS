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
package org.openvpms.web.component.print;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.StaticDocumentTemplateLocator;
import org.openvpms.web.echo.help.HelpContext;

import java.util.Iterator;
import java.util.List;


/**
 * Prints a batch of objects.
 *
 * @author Tim Anderson
 */
public abstract class BatchPrinter<T extends IMObject> implements PrinterListener {

    /**
     * Associates an object with its template.
     */
    public static class ObjectTemplate<T> {

        /**
         * The object to print.
         */
        private final T object;

        /**
         * The template to use
         */
        private final DocumentTemplate template;

        /**
         * Constructs an {@link ObjectTemplate}.
         *
         * @param object   the object to print
         * @param template the template to use
         */
        public ObjectTemplate(T object, DocumentTemplate template) {
            this.object = object;
            this.template = template;
        }

        /**
         * Returns the object.
         *
         * @return the object
         */
        public T getObject() {
            return object;
        }

        /**
         * Returns the template.
         *
         * @return the template
         */
        public DocumentTemplate getTemplate() {
            return template;
        }
    }

    /**
     * Iterator over the objects to  print.
     */
    private Iterator<?> iterator;

    /**
     * The object being printed.
     */
    private T object;

    /**
     * The context, used to locate document templates.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@link BatchPrinter}.
     *
     * @param objects the objects to print
     * @param context the context, used to locate document templates
     * @param help    the help context
     */
    public BatchPrinter(List<T> objects, Context context, HelpContext help) {
        this(context, help);
        setObjects(objects);
    }

    /**
     * Constructs a {@link BatchPrinter}.
     *
     * @param context the context
     * @param help    the help context
     */
    public BatchPrinter(Context context, HelpContext help) {
        this.context = context;
        this.help = help;
    }

    /**
     * Sets the objects to print.
     *
     * @param objects the objects to print
     */
    public void setObjects(List<T> objects) {
        iterator = objects.iterator();
    }

    /**
     * Sets the objects to print.
     *
     * @param objects the objects to print, with their associated templates
     */
    public void setObjectTemplates(List<ObjectTemplate<T>> objects) {
        iterator = objects.iterator();
    }

    /**
     * Initiates printing of the objects.
     */
    @SuppressWarnings("unchecked")
    public void print() {
        if (iterator != null && iterator.hasNext()) {
            Object next = iterator.next();
            DocumentTemplateLocator locator;

            try {
                if (next instanceof ObjectTemplate) {
                    ObjectTemplate<T> entry = (ObjectTemplate<T>) next;
                    object = entry.getObject();
                    locator = new StaticDocumentTemplateLocator(entry.getTemplate());
                } else {
                    object = (T) iterator.next();
                    locator = createDocumentTemplateLocator(object, context);
                }

                IMPrinter<T> printer = IMPrinterFactory.create(object, locator, context);
                InteractiveIMPrinter<T> iPrinter = createInteractivePrinter(printer);
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                failed(exception);
            }
        } else {
            completed();
        }
    }

    /**
     * Creates a new document template locator to locate the template for the object being printed.
     *
     * @param object  the object to print
     * @param context the context
     * @return a new document template locator
     */
    protected DocumentTemplateLocator createDocumentTemplateLocator(T object, Context context) {
        return new ContextDocumentTemplateLocator(object, context);
    }

    /**
     * Creates a new interactive printer.
     * <p/>
     * When printing interactively (i.e for those templates that specify interactive={@code true}),
     * objects may be skipped.
     * <p/>
     * 'This' is registered as a listener.
     *
     * @param printer the printer to delegate to
     * @return a new interactive printer
     */
    protected InteractiveIMPrinter<T> createInteractivePrinter(IMPrinter<T> printer) {
        InteractiveIMPrinter<T> result = new InteractiveIMPrinter<T>(printer, true, context, help);
        result.setListener(this);
        return result;
    }

    /**
     * Invoked when an object has been successfully printed.
     * <p/>
     * This updates the state of the <em>printed</em> flag, if the object has one, and prints the next object, if any.
     *
     * @param printer the printer that was used. May be {@code null}
     */
    public void printed(String printer) {
        boolean next = false;
        try {
            // update the print flag, if it exists
            IMObjectBean bean = new IMObjectBean(object);
            if (bean.hasNode("printed")) {
                bean.setValue("printed", true);
                bean.save();
            }
            next = true;
        } catch (OpenVPMSException exception) {
            failed(exception);
        }
        if (next) {
            print(); // print the next available object
        }
    }

    /**
     * Notifies that the print was cancelled.
     * <p/>
     * This implementation delegates to {@link #completed}.
     */
    public void cancelled() {
        completed();
    }

    /**
     * Notifies that the print was skipped.
     * <p/>
     * This implementation prints the next object using {@link #print}.
     */
    public void skipped() {
        print();
    }

    /**
     * Invoked when printing completes.
     * <p/>
     * This implementation is a no-op.
     */
    protected void completed() {
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }
}
