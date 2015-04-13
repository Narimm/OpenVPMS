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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;


/**
 * Document loader.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
interface Loader {

    /**
     * Registers a listener.
     *
     * @param listener the listener to register. May be <tt>null</tt>
     */
    void setListener(LoaderListener listener);

    /**
     * Returns the listener.
     *
     * @return the listener. May be <tt>null</tt>
     */
    LoaderListener getListener();

    /**
     * Determines if there is a document to load.
     *
     * @return <tt>true</tt> if there is a document to load, otherwise
     *         <tt>false</tt>
     */
    boolean hasNext();

    /**
     * Loads the next document.
     *
     * @return <tt>true</tt> if the document was loaded successfully
     */
    boolean loadNext();
}
