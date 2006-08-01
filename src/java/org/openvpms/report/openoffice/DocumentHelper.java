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
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XModel;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.document.Document;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToCreateDoc;


/**
 * Helper to manipulate {@link Document}s with OpenOffice.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DocumentHelper {
    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(DocumentHelper.class);


    /**
     * Creates a new document component from a template.
     *
     * @param template the document template
     * @param service  the OpenOffice service
     * @return the new document component
     * @throws OpenOfficeException if the document can't be created
     */
    public static XComponent newDocComponentFromTemplate(
            Document template, OpenOfficeService service) {
        XComponentLoader loader = service.getComponentLoader();

        byte[] content = template.getContents();
        XInputStream stream = new ByteArrayToXInputStreamAdapter(content);
        PropertyValue[] properties = new PropertyValue[]{
                newProperty("ReadOnly", Boolean.TRUE),
                newProperty("Hidden", Boolean.TRUE),
                newProperty("AsTemplate", true),
                newProperty("InputStream", stream)
        };
        // AsTemplate tells office to create a new document from the given
        // stream

        XComponent component;
        try {
            component = loader.loadComponentFromURL("private:stream", "_blank",
                                                    0, properties);
        } catch (Exception exception) {
            throw new OpenOfficeException(FailedToCreateDoc, exception,
                                          template.getName());
        }
        if (component == null) {
            throw new OpenOfficeException(FailedToCreateDoc,
                                          template.getName());
        }
        return component;
    }

    /**
     * Helper to close a document.
     *
     * @param document the document to close
     */
    public static void close(XComponent document) {
        try {
            XModel model = (XModel) UnoRuntime.queryInterface(XModel.class,
                                                              document);
            XCloseable closeable = (XCloseable) UnoRuntime.queryInterface(
                    XCloseable.class, model);
            closeable.close(false);
        } catch (CloseVetoException exception) {
            _log.error("Failed to close document", exception);
        }
    }

    /**
     * Helper to create a new <code>PropertyValue</code>.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new <code>PropertyValue</code>
     */
    public static PropertyValue newProperty(String name, Object value) {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }
}
