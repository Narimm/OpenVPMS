/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.print;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Document print interface. Provides support for interactive and background
 * printing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface Printer {

    /**
     * Prints the object to the default printer.
     *
     * @throws OpenVPMSException for any error
     */
    void print();

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    void print(String printer);

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or <tt>null</tt> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    String getDefaultPrinter();

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    Document getDocument();

    /**
     * Returns a document corresponding to that which would be printed.
     * <p/>
     * If the document cannot be converted to the specified mime-type, it will be returned unchanged.
     *
     * @param mimeType the mime type. If <tt>null</tt> the default mime type associated with the report will be used.
     * @param email    if <tt>true</tt> indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document
     * @throws OpenVPMSException for any error
     */
    Document getDocument(String mimeType, boolean email);

    /**
     * Determines if printing should occur interactively.
     *
     * @return <tt>true</tt> if printing should occur interactively,
     *         <tt>false</tt> if it can be performed non-interactively
     * @throws OpenVPMSException for any error
     */
    boolean getInteractive();

    /**
     * Sets the number of copies to print.
     *
     * @param copies the no. of copies to print
     */
    void setCopies(int copies);

    /**
     * Returns the number of copies to print.
     *
     * @return the no. of copies to print
     */
    int getCopies();

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    String getDisplayName();

}
