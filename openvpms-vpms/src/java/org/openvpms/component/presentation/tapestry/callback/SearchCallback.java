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

package org.openvpms.component.presentation.tapestry.callback;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.callback.ICallback;
import org.openvpms.component.presentation.tapestry.page.SearchPage;

/**
 *  This class provides an implementation of the {@link ICallBack} interface 
 *  for Search Pages.
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class SearchCallback implements ICallback {
    private static final long serialVersionUID = 1L;

    /**
     * Holds a refernce to the page name
     */
    private String pageName;

    /**
     * Constructor
     * @param pageName
     */
    public SearchCallback(String pageName) {
        this.pageName = pageName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.callback.ICallback#performCallback(org.apache.tapestry.IRequestCycle)
     */
    public void performCallback(IRequestCycle cycle) {
        // Find the page from the page name
        SearchPage searchPage = (SearchPage) cycle.getPage(pageName);
        //Activate the page
        cycle.activate(searchPage);
    }
}
