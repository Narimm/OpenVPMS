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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Prints an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectPrinter<T extends IMObject> {

    /**
     * Returns the object being printed.
     *
     * @return the object being printed
     */
    T getObject();

    /**
     * Prints the object to the default printer.
     *
     * @throws OpenVPMSException for any error
     */
    void print();

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <code>null</code>
     * @throws OpenVPMSException for any error
     */
    void print(String printer);

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or <code>null</code> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    String getDefaultPrinter();

    /**
     * Returns a document for the object, corresponding to that which would be
     * printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    Document getDocument();
}
