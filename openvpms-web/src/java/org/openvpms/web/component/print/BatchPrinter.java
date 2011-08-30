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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.print;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Prints a batch of objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class BatchPrinter<T extends IMObject> implements PrinterListener {

    /**
     * Iterator over the objects to  print.
     */
    private Iterator<T> iterator;

    /**
     * The object being printed.
     */
    private T object;

    /**
     * Constructs a <tt>BatchPrinter</tt>.
     */
    public BatchPrinter() {
        this(Collections.<T>emptyList());
    }

    /**
     * Constructs a <tt>BatchPrinter</tt>.
     *
     * @param objects the objects to print
     */
    public BatchPrinter(List<T> objects) {
        setObjects(objects);
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
     * Initiates printing of the objects.
     */
    public void print() {
        if (iterator.hasNext()) {
            object = iterator.next();
            try {
                IMPrinter<T> printer = IMPrinterFactory.create(object);
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
     * Creates a new interactive printer.
     * <p/>
     * When printing interactively (i.e for those templates that specify interactive=<tt>true</tt>),
     * objects may be skipped.
     * <p/>
     * 'This' is registered as a listener.
     *
     *
     * @param printer the printer to delegate to
     * @return a new interactive printer
     */
    protected InteractiveIMPrinter<T> createInteractivePrinter(IMPrinter<T> printer) {
        InteractiveIMPrinter<T> result = new InteractiveIMPrinter<T>(printer, true);
        result.setListener(this);
        return result;
    }

    /**
     * Invoked when an object has been successfully printed.
     * <p/>
     * This updates the state of the <em>printed</em> flag, if the object has one, and prints the next object, if any.
     *
     * @param printer the printer that was used. May be <tt>null</tt>
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

}
