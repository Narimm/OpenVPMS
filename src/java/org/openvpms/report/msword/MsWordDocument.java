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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.msword;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeException;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetField;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetUserFields;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Thin wrapper around a Microsft Word document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class MsWordDocument extends OpenOfficeDocument {

    /**
     * Prefix for Merge code fields.
     */
    private static final String FIELD_CODE_PREFIX = "MERGEFIELD";


    /**
     * Creates a new <tt>MsWordDocument</tt>.
     *
     * @param document the document
     * @param handlers the document handlers
     */
    public MsWordDocument(XComponent document, DocumentHandlers handlers) {
        super(document, handlers);
    }

    /**
     * Constructs a new <tt>MsWordDocument</tt>.
     *
     * @param document   the source document
     * @param connection the connection to the OpenOffice service
     * @param handlers   the document handlers
     * @throws OpenOfficeException for any error
     */
    public MsWordDocument(Document document, OOConnection connection,
                          DocumentHandlers handlers) {
        super(document, connection, handlers);
    }


    /**
     * Returns the content of a field.
     * <p/>
     * This implementation uses the 'FieldCode' property value if the field
     * is a user field that hasn't been changed.
     *
     * @param field the field
     * @return the field content
     * @throws OpenOfficeException if the field cannot be accessed
     */
    @Override
    protected String getContent(Field field) {
        if (!isInputField(field.getPropertySet()) && !field.isChanged()) {
            // use the original value derived from the FieldCode property
            return field.getValue();
        }
        return super.getContent(field);
    }

    /**
     * Returns the user text fields.
     * <p/>
     * This implementation uses the 'FieldCode' property as the field value.
     *
     * @return the user text fields, keyed on name
     * @throws OpenOfficeException if the fields can't be accessed
     */
    @Override
    protected Map<String, Field> getUserTextFields() {
        Map<String, Field> result = new LinkedHashMap<String, Field>();
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        int seed = 0;
        while (en.hasMoreElements()) {
            Object field;
            try {
                field = en.nextElement();
            } catch (Exception exception) {
                throw new OpenOfficeException(exception, FailedToGetUserFields);
            }
            if (isDatabaseField(field)) {
                XPropertySet set = (XPropertySet) UnoRuntime.queryInterface(
                        XPropertySet.class, field);
                String name = "userField" + (++seed);
                String value = getFieldCode(name, set);
                result.put(name, new Field(name, value, set));
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
    @Override
    protected Map<String, Field> getInputTextFields() {
        Map<String, Field> result = new LinkedHashMap<String, Field>();
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        int seed = 0;
        try {
            while (en.hasMoreElements()) {
                Object field = en.nextElement();
                if (isInputField(field)) {
                    XPropertySet set = (XPropertySet) UnoRuntime.queryInterface(
                            XPropertySet.class, field);
                    String name = "inputField" + (++seed);
                    String value = (String) set.getPropertyValue("Hint");
                    result.put(name, new Field(name, value, set));
                }
            }
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetUserFields);
        }
        return result;
    }

    /**
     * Returns the value of the FieldCode property.
     *
     * @param name the field name
     * @param set  the property set
     * @return the value of the FieldCode property
     */
    protected String getFieldCode(String name, XPropertySet set) {
        try {
            String value = (String) set.getPropertyValue("FieldCode");
            value = value.trim().substring(FIELD_CODE_PREFIX.length()).trim();
            return value;
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetField, name);
        }
    }

    /**
     * Determines if a field is a database (or merge) field .
     *
     * @param field the field
     * @return <tt>true</tt> if the field is a database field
     */
    private boolean isDatabaseField(Object field) {
        XServiceInfo info = (XServiceInfo) UnoRuntime.queryInterface(
                XServiceInfo.class, field);
        if (info != null) {
            return info.supportsService("com.sun.star.text.TextField.Database");
        }
        return false;
    }

}
