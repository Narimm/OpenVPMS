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
import org.openvpms.report.ReportException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


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
     * Constructs a new <tt>Reporter</tt> to generate documents from a single
     * object.
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
     *
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        IMReport<T> report = getReport();
        String type = mimeType;
        if (type == null) {
            type = report.getDefaultMimeType();
        }
        return report.generate(getObjects().iterator(), getParameters(), type);
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
     * Returns the report.
     *
     * @return the report
     * @throws OpenVPMSException         for any error
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public abstract IMReport<T> getReport();

}
