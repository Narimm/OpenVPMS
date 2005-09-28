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
 *  $Id: PropertyEditor.java 118 2005-09-21 09:36:09Z tony $
 */

package org.openvpms.component.presentation.tapestry.callback;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.callback.ICallback;
import org.openvpms.component.presentation.tapestry.page.SearchPage;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class SearchCallback implements ICallback {
    private static final long serialVersionUID = 1L;

    private String pageName;

    public SearchCallback(String pageName) {
        this.pageName = pageName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.callback.ICallback#performCallback(org.apache.tapestry.IRequestCycle)
     */
    public void performCallback(IRequestCycle cycle) {
        SearchPage searchPage = (SearchPage) cycle.getPage(pageName);
        cycle.activate(searchPage);
    }

    /**
     * @return Returns the pageName.
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * @param pageName
     *            The pageName to set.
     */
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
