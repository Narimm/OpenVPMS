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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToCreateDoc;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToExportDoc;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetField;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetInputFields;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetUserFields;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToSetField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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
     * The user fields, keyed on name.
     */
    private Map<String, Field> userFields;

    /**
     * The input fields, keyed on name.
     */
    private Map<String, Field> inputFields;

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
     * Creates a new <tt>OpenOfficeDocument</tt>.
     *
     * @param document   the source document
     * @param connection the connection to the OpenOffice service
     * @param handlers   the document handlers
     * @throws OpenOfficeException for any error
     */
    public OpenOfficeDocument(Document document, OOConnection connection, DocumentHandlers handlers) {
        XComponentLoader loader = connection.getComponentLoader();

        InputStream input;
        byte[] content;
        String name = document.getName();
        try {
            DocumentHandler handler = handlers.get(document);
            input = handler.getContent(document);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);
            IOUtils.closeQuietly(input);
            content = output.toByteArray();
            IOUtils.closeQuietly(output);
        } catch (DocumentException exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc, name);
        } catch (java.io.IOException exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc, name);
        }
        XInputStream xstream = new ByteArrayToXInputStreamAdapter(content);

        PropertyValue[] properties;
        PropertyValue readOnly = property("ReadOnly", Boolean.TRUE);
        PropertyValue hidden = property("Hidden", Boolean.TRUE);
        PropertyValue asTemplate = property("AsTemplate", true);
        // AsTemplate tells office to create a new document from the given stream
        PropertyValue inputStream = property("InputStream", xstream);

        String extension = FilenameUtils.getExtension(name);
        if (DocFormats.RTF_EXT.equalsIgnoreCase(extension)) {
            // need to specify FilterName to avoid awful performance loading RTF. See REP-17
            PropertyValue filter = property("FilterName", "Rich Text Format");
            properties = new PropertyValue[]{filter, readOnly, hidden, asTemplate, inputStream};
        } else {
            properties = new PropertyValue[]{readOnly, hidden, asTemplate, inputStream};
        }
        //

        XComponent component;
        try {
            component = loader.loadComponentFromURL("private:stream", "_blank",
                                                    0, properties);
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToCreateDoc, name);
        }
        if (component == null) {
            throw new OpenOfficeException(FailedToCreateDoc, name);
        }
        this.document = component;
        this.handlers = handlers;

        initFields();
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
     * @throws OpenOfficeException if the fields cannot be accessed
     */
    public List<String> getUserFieldNames() {
        return new ArrayList<String>(userFields.keySet());
    }

    /**
     * Gets the value of a user field.
     *
     * @param name the field name
     * @return the value of the field
     * @throws OpenOfficeException if the field cannot be accessed
     */
    public String getUserField(String name) {
        Field field = userFields.get(name);
        if (field == null) {
            throw new OpenOfficeException(FailedToGetField, name);
        }
        return getContent(field);
    }

    /**
     * Sets the value of a user field.
     *
     * @param name  the field name
     * @param value the value of the field
     * @throws OpenOfficeException if the field cannot be updated
     */
    public void setUserField(String name, String value) {
        Field field = userFields.get(name);
        if (field == null) {
            throw new OpenOfficeException(FailedToGetField, name);
        }
        setContent(field, value);
    }

    /**
     * Returns the set of input fields.
     * <p/>
     * These refer to fields whose values should be prompted for.
     *
     * @return the list of input field names.
     * @throws OpenOfficeException if the fields cannot be accessed
     */
    public Map<String, ParameterType> getInputFields() {
        Map<String, ParameterType> result = new LinkedHashMap<String, ParameterType>();
        for (Field field : inputFields.values()) {
            ParameterType param = new ParameterType(field.getName(), String.class, field.getValue());
            result.put(field.getName(), param);
        }
        return result;
    }

    /**
     * Determines if an input field exists with the specified name.
     *
     * @param name the input field name
     * @return <tt>true</tt> if the input field exists, otherwise <tt>false</tt>
     */
    public boolean hasInputField(String name) {
        return inputFields.containsKey(name);
    }

    /**
     * Determines if a user field exists with the specified name.
     *
     * @param name the user field name
     * @return <tt>true</tt> if the user field exists, otherwise <tt>false</tt>
     */
    public boolean hasUserField(String name) {
        return userFields.containsKey(name);
    }

    /**
     * Returns the value of an input field.
     *
     * @param name the input field name
     * @return the input field value. May be <tt>null</tt>
     */
    public String getInputField(String name) {
        Field field = inputFields.get(name);
        if (field == null) {
            throw new OpenOfficeException(FailedToGetField, name);
        }
        return getContent(field);
    }

    /**
     * Sets the value of an input field.
     *
     * @param name  the input field name
     * @param value the input field value. May be <tt>null</tt>
     */
    public void setInputField(String name, String value) {
        Field field = inputFields.get(name);
        if (field == null) {
            throw new OpenOfficeException(FailedToGetField, name);
        }
        // TODO - add support for InputUser fields
        setContent(field, value);
    }

    /**
     * Refreshes the document fields.
     */
    public void refresh() {
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, fields);
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
        XTextDocument textDocument = UnoRuntime.queryInterface(XTextDocument.class, document);
        XOutputStreamToByteArrayAdapter stream = new XOutputStreamToByteArrayAdapter();

        XStorable storable = UnoRuntime.queryInterface(XStorable.class, textDocument);

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
        } else if (mimeType.equals(DocFormats.DOC_TYPE)) {
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
            XModel model = UnoRuntime.queryInterface(XModel.class, document);
            XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, model);
            closeable.close(false);
        } catch (CloseVetoException exception) {
            log.error("Failed to close document", exception);
        }
    }

    /**
     * Returns the user text fields.
     *
     * @return the user text fields, keyed on name
     * @throws OpenOfficeException if the fields can't be accessed
     */
    protected Map<String, Field> getUserTextFields() {
        Map<String, Field> result = new LinkedHashMap<String, Field>();
        XNameAccess fields = getTextFieldMasters();
        for (String elementName : fields.getElementNames()) {
            try {
                // need to do a case insensitive comparison for OVPMS-749
                if (elementName.regionMatches(true, 0, USER_FIELD_PREFIX, 0, USER_FIELD_PREFIX.length())) {
                    String name = elementName.substring(USER_FIELD_PREFIX.length());
                    Object fieldMaster = fields.getByName(elementName);
                    XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, fieldMaster);
                    String value = (String) propertySet.getPropertyValue("Content");
                    Field field = new Field(name, value, propertySet);
                    result.put(name, field);
                }
            } catch (Exception exception) {
                throw new OpenOfficeException(exception, FailedToGetUserFields);
            }
        }
        return result;
    }

    /**
     * Returns the input text fields.
     *
     * @return the input text fields, keyed on name
     * @throws OpenOfficeException if the fields can't be accessed
     */
    protected Map<String, Field> getInputTextFields() {
        Map<String, Field> result = new LinkedHashMap<String, Field>();
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        int seed = 0;
        try {
            while (en.hasMoreElements()) {
                XPropertySet set = getInputFieldPropertySet(en.nextElement());
                if (set != null) {
                    String name = "inputField" + (++seed);
                    String value = (String) set.getPropertyValue("Hint");
                    if (StringUtils.isEmpty(value)) {
                        value = (String) set.getPropertyValue("Content");
                    }
                    result.put(name, new Field(name, value, set));
                }
            }
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetInputFields);
        }
        return result;
    }

    /**
     * Sets the content of a field.
     *
     * @param field the field
     * @param value the new value. May be <tt>null</tt>
     * @throws OpenOfficeException if the field cannot be updated
     */
    protected void setContent(Field field, String value) {
        try {
            field.getPropertySet().setPropertyValue("Content", value);
            field.setChanged(true);
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToSetField, field.getName());
        }
    }

    /**
     * Returns the content of a field.
     *
     * @param field the field
     * @return the field content. May be <tt>null</tt>
     * @throws OpenOfficeException if the field cannot be accessed
     */
    protected String getContent(Field field) {
        Object content;
        try {
            content = field.getPropertySet().getPropertyValue("Content");
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetField,
                                          field.getName());
        }
        return content != null ? content.toString() : null;
    }

    /**
     * Returns the text field supplier interface.
     *
     * @return the text field supplier interface
     */
    protected XTextFieldsSupplier getTextFieldSupplier() {
        return UnoRuntime.queryInterface(XTextFieldsSupplier.class, document);
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
     * Determines if a field is an input text field.
     *
     * @param field the field
     * @return <tt>true</tt> if the field is an input text field
     */
    protected boolean isInputField(Object field) {
        XServiceInfo info = UnoRuntime.queryInterface(XServiceInfo.class, field);
        return info != null && info.supportsService("com.sun.star.text.TextField.Input");
    }

    /**
     * Determines if a field is an input user text field.
     * <p/>
     * These fields update the user field identified by their <em>Content</em>
     * property.
     *
     * @param field the field
     * @return <tt>true</tt> if the field is an input text field
     */
    private boolean isInputUserField(Object field) {
        XServiceInfo info = UnoRuntime.queryInterface(XServiceInfo.class, field);
        return info != null && info.supportsService("com.sun.star.text.TextField.InputUser");
    }

    /**
     * Initialises the user and input fields.
     */
    private void initFields() {
        userFields = getUserTextFields();
        inputFields = getInputTextFields();
    }

    /**
     * Helper to create a new <tt>PropertyValue</tt>.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new <tt>PropertyValue</tt>
     */
    private PropertyValue property(String name, Object value) {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }

    /**
     * Helper to return the property set of a field, if the supplied field is
     * an input field.
     *
     * @param field the field
     * @return the input field's property set, or <tt>null</tt> if it isn't an
     *         input field
     */
    private XPropertySet getInputFieldPropertySet(Object field) {
        XTextField text = UnoRuntime.queryInterface(XTextField.class, field);
        if (isInputField(text) || isInputUserField(text)) {
            return UnoRuntime.queryInterface(XPropertySet.class, text);
        }
        return null;
    }

    /**
     * Field wrapper.
     */
    protected static class Field {

        private final String name;

        private final String value;

        private XPropertySet field;

        private boolean changed;

        /**
         * Creates a new <tt>Field</tt>.
         *
         * @param name       the field name
         * @param value      the field value
         * @param properties the field's properties
         */
        public Field(String name, String value, XPropertySet properties) {
            this.name = name;
            this.field = properties;
            this.value = value;
        }

        /**
         * Returns the field name.
         *
         * @return the field name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the field value.
         *
         * @return the field value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the field's properties.
         *
         * @return the properties
         */
        public XPropertySet getPropertySet() {
            return field;
        }

        /**
         * Determines if the field has changed..
         *
         * @return <tt>true</tt> if the field has changed
         */
        public boolean isChanged() {
            return changed;
        }

        /**
         * Determines if the field has been changed.
         *
         * @param changed if <tt>true</tt>, indicates that the field has changed
         */
        public void setChanged(boolean changed) {
            this.changed = changed;
        }
    }
}
