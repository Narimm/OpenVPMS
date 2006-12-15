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
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XRefreshable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Thin wrapper around an OpenOffice document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeDocument {

    /**
     * The document component.
     */
    private XComponent document;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * Prefix for user fields.
     */
    private static final String USER_FIELD_PREFIX
            = "com.sun.star.text.FieldMaster.User.";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OpenOfficeDocument.class);

    /**
     * Constructs a new <code>OpenOfficeDocument</code>.
     *
     * @param document the document
     */
    public OpenOfficeDocument(XComponent document, DocumentHandlers handlers) {
        this.document = document;
        this.handlers = handlers;
    }

    /**
     * Constructs a new <code>OpenOfficeDocument</code>.
     *
     * @param document the source document
     * @param service  the OpenOffice service
     * @throws OpenOfficeException for any error
     */
    public OpenOfficeDocument(Document document, OpenOfficeService service,
                              DocumentHandlers handlers) {
        XComponentLoader loader = service.getComponentLoader();

        InputStream input;
        byte[] content;
        try {
            DocumentHandler handler = handlers.get(document);
            input = handler.getContent(document);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);
            IOUtils.closeQuietly(input);
            content = output.toByteArray();
            IOUtils.closeQuietly(output);
        } catch (DocumentException exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc,
                                          document.getName());
        } catch (java.io.IOException exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc,
                                          document.getName());
        }
        XInputStream xstream = new ByteArrayToXInputStreamAdapter(content);
        // XInputStream xstream = new InputStreamToXInputStreamAdapter(stream);
        PropertyValue[] properties = new PropertyValue[]{
                property("ReadOnly", Boolean.TRUE),
                property("Hidden", Boolean.TRUE),
                property("AsTemplate", true),
                property("InputStream", xstream)
        };
        // AsTemplate tells office to create a new document from the given
        // stream

        XComponent component;
        try {
            component = loader.loadComponentFromURL("private:stream", "_blank",
                                                    0, properties);
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc,
                                          document.getName());
        }
        if (component == null) {
            throw new OpenOfficeException(FailedToCreateDoc,
                                          document.getName());
        }
        this.document = component;
        this.handlers = handlers;
    }

    /**
     * Returns the underlying component.
     *
     * @return the underlying component
     */
    public XComponent getComponent() {
        return document;
    }

    /**
     * Returns the set of user field names.
     *
     * @return the list of user field names.
     */
    public List<String> getUserFieldNames() {
        List<String> result = new ArrayList<String>();
        XNameAccess fields = getTextFieldMasters();
        for (String name : fields.getElementNames()) {
            if (name.startsWith(USER_FIELD_PREFIX)) {
                result.add(name.substring(USER_FIELD_PREFIX.length()));
            }
        }
        return result;
    }

    /**
     * Gets the value of a user field.
     *
     * @param name the field name
     * @return the value of the field
     * @throws OpenOfficeException if the field cannot be accessed
     */
    public String getUserField(String name) {
        Object content;
        try {
            XNameAccess fields = getTextFieldMasters();
            Object fieldMaster = fields.getByName(USER_FIELD_PREFIX + name);
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, fieldMaster);
            content = propertySet.getPropertyValue("Content");
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetField, name);
        }
        return (content != null) ? content.toString() : null;
    }

    /**
     * Sets the value of a user field.
     *
     * @param name  the field name
     * @param value the value of the field
     * @throws OpenOfficeException if the field cannot be updated
     */
    public void setUserField(String name, String value) {
        try {
            XNameAccess fields = getTextFieldMasters();
            Object fieldMaster = fields.getByName(USER_FIELD_PREFIX + name);
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, fieldMaster);
            propertySet.setPropertyValue("Content", value);
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToSetField, name);
        }
    }

    /**
     * Refreshes the document fields.
     */
    public void refresh() {
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XRefreshable refreshable = (XRefreshable) UnoRuntime.queryInterface(
                XRefreshable.class, fields);
        refreshable.refresh();
    }

    /**
     * Exports the document.
     *
     * @param mimeType the mime-type of the document format to export to
     * @return the exported document serialized to a byte array
     * @throws OpenOfficeException if the document cannot be exported
     */
    public byte[] export(String mimeType) {
        boolean isPDF = mimeType.equals(DocFormats.PDF_TYPE);
        XTextDocument textDocument = (XTextDocument) UnoRuntime.queryInterface(
                XTextDocument.class, document);
        XOutputStreamToByteArrayAdapter stream
                = new XOutputStreamToByteArrayAdapter();

        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, textDocument);

        PropertyValue[] properties;
        PropertyValue outputStream = property("OutputStream", stream);
        PropertyValue overwrite = property("Overwrite", true);
        if (isPDF) {
            PropertyValue filter = property("FilterName", "writer_pdf_Export");
            properties = new PropertyValue[]{outputStream, overwrite, filter};
        } else {
            properties = new PropertyValue[]{outputStream, overwrite};
        }

        try {
            storable.storeToURL("private:stream", properties);
            stream.closeOutput();
        } catch (IOException exception) {
            throw new OpenOfficeException(exception, FailedToExportDoc,
                                          exception.getMessage());
        }
        return stream.getBuffer();
    }

    /**
     * Exports the document.
     *
     * @param mimeType the mime-type of the document format to export to
     * @param name     the document name
     * @return the exported document
     * @throws OpenOfficeException if the source document cannot be
     *                             exported
     * @throws DocumentException   if the target document cannot be
     *                             created
     */
    public Document export(String mimeType, String name) {
        boolean isPDF = mimeType.equals(DocFormats.PDF_TYPE);
        byte[] content = export(mimeType);
        if (isPDF) {
            name = name + "." + DocFormats.PDF_EXT;
        }
        try {
            DocumentHandler handler = handlers.get(name, "document.other",
                                                   mimeType);
            return handler.create(name, new ByteArrayInputStream(content),
                                  mimeType, content.length);
        } catch (OpenVPMSException exception) {
            throw new OpenOfficeException(exception, FailedToExportDoc,
                                          exception.getMessage());
        }
    }

    /**
     * Closes the document.
     */
    public void close() {
        try {
            XModel model = (XModel) UnoRuntime.queryInterface(XModel.class,
                                                              document);
            XCloseable closeable = (XCloseable) UnoRuntime.queryInterface(
                    XCloseable.class, model);
            closeable.close(false);
        } catch (CloseVetoException exception) {
            log.error("Failed to close document", exception);
        }
    }

    /**
     * Returns the text field supplier interface.
     *
     * @return the text field supplier interface
     */
    private XTextFieldsSupplier getTextFieldSupplier() {
        return (XTextFieldsSupplier) UnoRuntime.queryInterface(
                XTextFieldsSupplier.class, document);
    }

    /**
     * Returns a list of all text field masters.
     *
     * @return the text field masters
     */
    private XNameAccess getTextFieldMasters() {
        return getTextFieldSupplier().getTextFieldMasters();
    }

    /**
     * Helper to create a new <code>PropertyValue</code>.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new <code>PropertyValue</code>
     */
    private PropertyValue property(String name, Object value) {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }

}
