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

package org.openvpms.component.presentation.tapestry.component;

// tapestry hivemind
import java.util.StringTokenizer;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.openvpms.component.presentation.tapestry.page.OpenVpmsPage;
import org.openvpms.component.presentation.tapestry.page.SearchPage;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class SearchLink extends Link {
    public abstract String getArchetypeRange();
	public abstract void setArchetypeRange(String archetypeRange);

	public void click(IRequestCycle cycle) {
	    // First extract the reference model name from the passed parameter.
        StringTokenizer tokens = new StringTokenizer(getArchetypeRange(),  ".");
        if (tokens.countTokens() != 3)
            return;
        String rmName = tokens.nextToken();

        // First check we have a refernce model set
        if (rmName == null || rmName == "")
            return;
      
        try {
            // try and find the page for this reference Model otherwise use DefaultSearch
            SearchPage page = (SearchPage) findPage(cycle, rmName,"Search");
            
            // Push a callback
            ((OpenVpmsPage)getPage()).pushCallback();

            // Set the properties on the search page
            page.setArchetypeRange(getArchetypeRange());
            
            // Activate the Page
            cycle.activate(page);
            
		} catch (Exception ex) {
			throw new ApplicationRuntimeException(ex);
		}
	}

	/**
	 * @return
	 */
	public String getLinkText() {
        String range = getArchetypeRange();
        StringTokenizer tokens = new StringTokenizer(range,  ".");
        if (tokens.countTokens() != 3)
            return "";
        String rmName = tokens.nextToken();
		return "Search " + rmName;
	}
}
