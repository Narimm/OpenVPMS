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

package org.openvpms.report.openoffice;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;
import org.apache.commons.io.FilenameUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.NodeResolver;
import org.openvpms.report.jasper.ReportHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Generates a report for an <code>IMObject</code>, using an OpenOffice document
 * as the template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeIMObjectReport implements IMObjectReport {

    /**
     * The document template.
     */
    private final Document _template;

    /**
     * The mime-type to generate the document as.
     */
    private String _mimeType;

    /**
     * Prefix for user fields. The value of these fields are populated with
     * data from the IMObject.
     */
    private static final String USER_FIELD_PREFIX
            = "com.sun.star.text.FieldMaster.User.";


    /**
     * Creates a new <code>OpenOfficeIMObjectReport</code>.
     *
     * @param template  the document template
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @throws IMObjectReportException if the mime-type is invalid
     */
    public OpenOfficeIMObjectReport(Document template, String[] mimeTypes) {
        _template = template;
        for (String mimeType : mimeTypes) {
            if (DocFormats.ODT_TYPE.equals(mimeType)
                    || DocFormats.RTF_TYPE.equals(mimeType)) {
                _mimeType = mimeType;
                break;
            }
        }
        if (_mimeType == null) {
            throw new IMObjectReportException("No valid mime-types provided");
        }
    }

    /**
     * Generates a report for an object.
     *
     * @param object the object
     * @return a document containing the report
     * @throws IMObjectReportException for any error
     */
    public Document generate(IMObject object) {
        NodeResolver resolver = new NodeResolver(
                object, ArchetypeServiceHelper.getArchetypeService());
        try {
            XComponent template = DocumentHelper.newDocComponentFromTemplate(
                    _template, OpenOfficeHelper.getService());

            // get XTextFieldsSupplier interface
            XTextFieldsSupplier textFieldsSupplier = (XTextFieldsSupplier)
                    UnoRuntime.queryInterface(XTextFieldsSupplier.class,
                                              template);

            // access the TextFields and the TextFieldMasters collections
            XNameAccess namedFieldMasters
                    = textFieldsSupplier.getTextFieldMasters();
            XEnumerationAccess enumeratedFields
                    = textFieldsSupplier.getTextFields();
            List<String> fieldNames = getUserFieldNames(namedFieldMasters);

            for (String name : fieldNames) {
                String value = getValue(name, resolver);

                // access corresponding field master
                Object fieldMaster = namedFieldMasters.getByName(
                        USER_FIELD_PREFIX + name);

                // query the XPropertySet interface, in order to set the
                // Content property
                XPropertySet propertySet
                        = (XPropertySet) UnoRuntime.queryInterface(
                        XPropertySet.class, fieldMaster);

                // insert the value into field master
                propertySet.setPropertyValue("Content", value);
            }
            // refresh the textfields collection
            XRefreshable refreshable = (XRefreshable) UnoRuntime.queryInterface(
                    XRefreshable.class, enumeratedFields);
            refreshable.refresh();
            return export(template);
        } catch (com.sun.star.uno.Exception exception) {
            throw new IMObjectReportException(exception);
        }
    }

    /**
     * Returns a list of user field names.
     *
     * @param namedFieldMasters the a list of all field masters
     * @return a list of user field names
     */
    private List<String> getUserFieldNames(XNameAccess namedFieldMasters) {
        List<String> result = new ArrayList<String>();
        for (String name : namedFieldMasters.getElementNames()) {
            if (name.startsWith(USER_FIELD_PREFIX)) {
                result.add(name.substring(USER_FIELD_PREFIX.length()));
            }
        }
        return result;
    }

    /**
     * Returns the value of a field.
     *
     * @param name     the field name
     * @param resolver the node resolver
     * @return the value of the field
     */
    private String getValue(String name, NodeResolver resolver) {
        NodeResolver.State state = resolver.resolve(name);
        Object value = state.getValue();
        Object result = null;
        if (value != null) {
            if (state.getLeafNode() != null
                    && state.getLeafNode().isCollection()) {
                if (value instanceof Collection) {
                    Collection<IMObject> values = (Collection<IMObject>) value;
                    StringBuffer descriptions = new StringBuffer();
                    for (IMObject object : values) {
                        descriptions.append(ReportHelper.getValue(object));
                        descriptions.append('\n');
                    }
                    result = descriptions.toString();
                } else {
                    // single value collection.
                    IMObject object = (IMObject) value;
                    result = ReportHelper.getValue(object);
                }
            } else {
                result = value;
            }
        }
        return (result == null) ? "" : result.toString();
    }

    /**
     * Exports the template as a PDF, serializing to a {@link Document}.
     *
     * @param template the template
     * @return a new document, containing the serialized template
     * @throws IOException for any I/O error
     */
    private Document export(XComponent template) throws IOException {
        boolean isPDF = _mimeType.equals(DocFormats.PDF_TYPE);
        XTextDocument textDocument = (XTextDocument) UnoRuntime.queryInterface(
                XTextDocument.class, template);
        XOutputStreamToByteArrayAdapter stream
                = new XOutputStreamToByteArrayAdapter();

        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, textDocument);

        PropertyValue[] properties;
        PropertyValue outputStream
                = DocumentHelper.newProperty("OutputStream", stream);
        PropertyValue overwrite = DocumentHelper.newProperty("Overwrite", true);
        if (isPDF) {
            PropertyValue filter = DocumentHelper.newProperty(
                    "FilterName", "writer_pdf_Export");
            properties = new PropertyValue[]{outputStream, overwrite, filter};
        } else {
            properties = new PropertyValue[]{outputStream, overwrite};
        }

        storable.storeToURL("private:stream", properties);
        stream.closeOutput();
        DocumentHelper.close(template);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        Document doc = (Document) service.create("document.other");
        if (isPDF) {
            String name = FilenameUtils.removeExtension(_template.getName())
                    + "." + DocFormats.PDF_EXT;
            doc.setName(name);
        } else {
            doc.setName(_template.getName());
        }
        byte[] content = stream.getBuffer();
        doc.setContents(content);
        doc.setDocSize(content.length);
        doc.setMimeType(_mimeType);
        return doc;
    }
}
