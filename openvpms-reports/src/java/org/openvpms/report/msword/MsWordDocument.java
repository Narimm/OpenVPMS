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

import java.util.ArrayList;
import java.util.List;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeException;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;

/**
 * Thin wrapper around an Microsft Word document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class MsWordDocument extends OpenOfficeDocument {

    /**
     * Prefix for Merge code fields.
     */
    private static final String FIELD_CODE_PREFIX
            = "MERGEFIELD";

    /**
     * Constructs a new <code>MsWordDocument</code>.
     *
     * @param document the document
     */
	public MsWordDocument(XComponent document, DocumentHandlers handlers) {
		super(document, handlers);
	}

    /**
     * Constructs a new <code>MsWordDocument</code>.
     *
     * @param document   the source document
     * @param connection the connection to the OpenOffice service
     * @throws OpenOfficeException for any error
     */
	public MsWordDocument(Document document, OOConnection connection,
			DocumentHandlers handlers) {
		super(document, connection, handlers);
	}

    /**
     * Gets the value of a user field.
     *
     * @param name the field name
     * @return the value of the field
     * @throws OpenOfficeException if the field cannot be accessed
     */
	@Override
	public String getUserField(String name) {
		return name;
	}

    /**
     * Returns the set of user field names.
     *
     * @return the list of user field names.
     */
	@Override
	public List<String> getUserFieldNames() {
        List<String> result = new ArrayList<String>();
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        while (en.hasMoreElements()){
        	try {
	        	Object field = en.nextElement();
	            XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, field);
	            String fieldCode = (String)xPropertySet.getPropertyValue("FieldCode");
	            String fieldName = fieldCode.trim().substring(FIELD_CODE_PREFIX.length()).trim();
	            result.add(fieldName);
        	} catch (Exception e) { 
        	}
        }
        return result;
	}

	/**
     * Sets the value of a user field.
     * 
     * @param name  the field name
     * @param value the value of the field
     * @throws OpenOfficeException if the field cannot be updated
     */
	@Override
	public void setUserField(String name, String value) {
        XEnumerationAccess fields = getTextFieldSupplier().getTextFields();
        XEnumeration en = fields.createEnumeration();
        while (en.hasMoreElements()){
        	try {
	        	Object field = en.nextElement();
	            XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, field);
	            String fieldCode = (String)xPropertySet.getPropertyValue("FieldCode");
	            String fieldName = fieldCode.trim().substring(FIELD_CODE_PREFIX.length()).trim();
	            if (fieldName.equalsIgnoreCase(name)) {
	            	xPropertySet.setPropertyValue("Content", value);
	            }
        	} catch (Exception exception) { 
        	}
        }
	}
}
