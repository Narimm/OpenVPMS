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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.report;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Generates {@link Document}s from one or more objects, using a
 * {@link IMReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class Reporter<T> {

    /**
     * The default document format.
     */
    public static final String DEFAULT_MIME_TYPE = DocFormats.PDF_TYPE;

    /**
     * Parameter to pass to reports to indicate that the report is being emailed.
     */
    public static final String IS_EMAIL = "IsEmail";

    /**
     * The objects to generate the document from.
     */
    private final Iterable<T> objects;

    /**
     * The object to generate the document from, or <tt>null</tt> if the
     * document is being generated from a collection.
     */
    private final T object;

    /**
     * The parameters to pass to the report.
     */
    private Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * The document mime type.
     */
    private String mimeType = DEFAULT_MIME_TYPE;


    /**
     * Constructs a <tt>Reporter</tt> to generate documents from a single object.
     *
     * @param object the object
     */
    @SuppressWarnings("unchecked")
    public Reporter(T object) {
        objects = Arrays.asList(object);
        this.object = object;
    }

    /**
     * Constructs a new <tt>Reporter</tt> to generate documents from a
     * collection of objects.
     *
     * @param objects the objects
     */
    public Reporter(Iterable<T> objects) {
        this.objects = objects;
        object = null;
    }

    /**
     * Returns the object that the document is being generated from.
     *
     * @return the object, or <tt>null</tt> if the document is being generated
     *         from a collection
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the objects.
     *
     * @return the objects
     */
    public Iterable<T> getObjects() {
        return objects;
    }

    /**
     * Creates the document.
     * <p/>
     * Documents are formatted according to the default mime type. If the document has an {@link #IS_EMAIL} parameter,
     * then this will be set <tt>false</tt>.
     *
     * @return the document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        return getDocument(mimeType, false);
    }

    /**
     * Creates the document, in the specified mime type.
     *
     * @param type  the mime type. If <tt>null</tt> the default mime type associated with the report will be used.
     * @param email if <tt>true</tt> indicates that the document will be emailed. Documents generated from templates
     *              can perform custom formatting
     * @return the document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument(String type, boolean email) {
        IMReport<T> report = getReport();
        if (type == null) {
            type = report.getDefaultMimeType();
        }
        Map<String, Object> map = new HashMap<String, Object>(getParameters(email));
        return report.generate(getObjects().iterator(), map, type);
    }

    /**
     * Prints the report.
     *
     * @param objects    the objects to print
     * @param properties the print properties
     */
    public void print(Iterator<T> objects, PrintProperties properties) {
        getReport().print(objects, getParameters(false), properties);
    }

    /**
     * Returns the document mime type.
     *
     * @return the mime type. May be <tt>null</tt>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the document mime type.
     *
     * @param mimeType the mime type. If <tt>null</tt> the default mime type
     *                 will be used
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets parameters to pass to the report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns a map of parameters names and their values, to pass to the
     * report.
     *
     * @return a map of parameter names and their values. May be <tt>null</tt>
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     * <p/>
     * This supresses return of the {@link #IS_EMAIL} parameter as this is dealt with automatically.
     *
     * @return the parameter types
     */
    public Set<ParameterType> getParameterTypes() {
        Set<ParameterType> result = new LinkedHashSet<ParameterType>();
        for (ParameterType type : getReport().getParameterTypes()) {
            if (!IS_EMAIL.equals(type.getName())) {
                result.add(type);
            }
        }
        return result;
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws OpenVPMSException         for any error
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IMReport<T> getReport();

    /**
     * Returns the report parameters.
     *
     * @param email if the report has an {@link #IS_EMAIL} parameter, then this will be supplied with the value of
     *              <tt>email</tt>. This enables reports to be customised for email vs printing.
     * @return the report parameters
     */
    protected Map<String, Object> getParameters(boolean email) {
        Map<String, Object> result;
        if (getReport().hasParameter(IS_EMAIL)) {
            result = new HashMap<String, Object>();
            if (parameters != null) {
                result.putAll(parameters);
            }
            result.put(IS_EMAIL, email);
        } else {
            result = parameters;
        }
        return result;
    }

}
