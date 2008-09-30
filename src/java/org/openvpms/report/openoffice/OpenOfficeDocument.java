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

import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToCreateDoc;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToExportDoc;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetField;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToSetField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextField;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XRefreshable;


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
     * NOTE: in OO 2.1, <em>fieldmaster</em> was named <em>FieldMaster</em>.
     */
    private static final String USER_FIELD_PREFIX
            = "com.sun.star.text.fieldmaster.User.";

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
     * @param document   the source document
     * @param connection the connection to the OpenOffice service
     * @throws OpenOfficeException for any error
     */
    public OpenOfficeDocument(Document document, OOConnection connection,
                              DocumentHandlers handlers) {
        XComponentLoader loader = connection.getComponentLoader();

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
            // need to do a case insensitive comparison for OVPMS-749
            if (name.regionMatches(true, 0, USER_FIELD_PREFIX, 0,
                                   USER_FIELD_PREFIX.length())) {
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
            String fieldName = getFieldName(fields, name);
            if (fieldName == null) {
                throw new OpenOfficeException(FailedToSetField, name);
            }
            Object fieldMaster = fields.getByName(fieldName);
            XPropertySet propertySet
                    = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, fieldMaster);
            content = propertySet.getPropertyValue("Content");
        } catch (OpenOfficeException exception) {
            throw exception;
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
            String fieldName = getFieldName(fields, name);
            if (fieldName == null) {
                throw new OpenOfficeException(FailedToSetField, name);
            }
            Object fieldMaster = fields.getByName(fieldName);
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, fieldMaster);
            propertySet.setPropertyValue("Content", value);
        } catch (OpenOfficeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToSetField, name);
        }
    }

    /**
     * Returns the set of input field names.
     * Input Field Names are the hint property on the input Field.
     *
     * @return the list of input field names.
     */
    public List<String> getInputFieldNames() {
        List<String> result = new ArrayList<String>();
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        while (en.hasMoreElements()){
        	try {
	        	Object field = en.nextElement();
                XTextField texert = (XTextField)UnoRuntime.queryInterface(XTextField.class, field);
                XServiceInfo xServiceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, texert);
                if(xServiceInfo.supportsService("com.sun.star.text.TextField.Input"))
                {
                    // query the XPropertySet interface, we need to get the Content property
                    XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, texert);
                    // get the field name and add to result
                    String fieldName = (String)xPropertySet.getPropertyValue("Hint");
    	            result.add(fieldName);
                }
        	} catch (Exception e) { 
        	}
        }
        return result;
    }

    /**
     * Gets the value of a input field.
     * The value of a Input Field is it's content.
     *
     * @param name the field name
     * @return the value of the field
     * @throws OpenOfficeException if the field cannot be accessed
     */
    public String getInputField(String name) {
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        while (en.hasMoreElements()){
        	try {
	        	Object field = en.nextElement();
                XTextField texert = (XTextField)UnoRuntime.queryInterface(XTextField.class, field);
                XServiceInfo xServiceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, texert);
                if(xServiceInfo.supportsService("com.sun.star.text.TextField.Input"))
                {
                    // query the XPropertySet interface, we need to get the Content property
                    XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, texert);
                    // get the field name and add to result
                    String fieldName = (String)xPropertySet.getPropertyValue("Hint");
		            if (fieldName.equalsIgnoreCase(name)) {
		            	return (String)xPropertySet.getPropertyValue("Content");
		            }
                }
        	} catch (Exception exception) { 
        	}
        }

		return name;
    }

    /**
     * Sets the value of a input field.
     *
     * @param name  the field name
     * @param value the value of the field
     */
    public void setInputField(String name, String value) {
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        while (en.hasMoreElements()){
        	try {
	        	Object field = en.nextElement();
                XTextField texert = (XTextField)UnoRuntime.queryInterface(XTextField.class, field);
                XServiceInfo xServiceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, texert);
                if(xServiceInfo.supportsService("com.sun.star.text.TextField.Input"))
                {
                    // query the XPropertySet interface, we need to get the Content property
                    XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, texert);
                    // get the field name and add to result
                    String fieldName = (String)xPropertySet.getPropertyValue("Hint");
		            if (fieldName.equalsIgnoreCase(name)) {
		            	xPropertySet.setPropertyValue("Content",value);
		            }
                }
        	} catch (Exception exception) { 
        	}
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
        XTextDocument textDocument = (XTextDocument) UnoRuntime.queryInterface(
                XTextDocument.class, document);
        XOutputStreamToByteArrayAdapter stream
                = new XOutputStreamToByteArrayAdapter();

        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, textDocument);

        PropertyValue[] properties;
        PropertyValue outputStream = property("OutputStream", stream);
        PropertyValue overwrite = property("Overwrite", true);
        if (mimeType.equals(DocFormats.PDF_TYPE)) {
            PropertyValue filter = property("FilterName", "writer_pdf_Export");
            properties = new PropertyValue[]{outputStream, overwrite, filter};
        } else if (mimeType.equals(DocFormats.DOC_TYPE)) {
            PropertyValue filter = property("FilterName", "MS Word 97");
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
        byte[] content = export(mimeType);
        if (mimeType.equals(DocFormats.PDF_TYPE)) {
            name = name + "." + DocFormats.PDF_EXT;
        }
        else if (mimeType.equals(DocFormats.DOC_TYPE)) {
            name = name + "." + DocFormats.DOC_EXT;
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
    protected XTextFieldsSupplier getTextFieldSupplier() {
        return (XTextFieldsSupplier) UnoRuntime.queryInterface(
                XTextFieldsSupplier.class, document);
    }

    /**
     * Returns a list of all text field masters.
     *
     * @return the text field masters
     */
    protected XNameAccess getTextFieldMasters() {
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

    /**
     * Returns the field name for the specified name.
     *
     * @param fields the document fields
     * @param name   the name
     * @return the field name, or <tt>null</tt> if it doesn't exist
     */
    private String getFieldName(XNameAccess fields, String name) {
        String cmp = USER_FIELD_PREFIX + name;
        for (String elementName : fields.getElementNames()) {
            if (elementName.equalsIgnoreCase(cmp)) {
                return elementName;
            }
        }
        return null;
    }

}
