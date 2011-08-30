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

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.web.component.print.AbstractPrinter;

import java.util.Arrays;


/**
 * Abstract implementation of the {@link IMPrinter} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMPrinter<T>
        extends AbstractPrinter implements IMPrinter<T> {

    /**
     * The objects to print.
     */
    private final Iterable<T> objects;

    /**
     * The object to print.
     */
    private final T object;


    /**
     * Constructs a new <tt>AbstractIMPrinter</tt> to print a single object.
     *
     * @param object the object to print
     */
    public AbstractIMPrinter(T object) {
        objects = Arrays.asList(object);
        this.object = object;
    }

    /**
     * Constructs a new <tt>AbstractIMPrinter</tt> to print a collection
     * of objects.
     *
     * @param objects the objects to print
     */
    public AbstractIMPrinter(Iterable<T> objects) {
        this.objects = objects;
        object = null;
    }

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public Iterable<T> getObjects() {
        return objects;
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        IMReport<T> report = createReport();
        report.print(getObjects().iterator(), getProperties(printer));
    }

    /**
     * Returns the object being printed.
     *
     * @return the object being printed, or <tt>null</tt> if a collection
     *         is being printed
     */
    protected T getObject() {
        return object;
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IMReport<T> createReport();

}
