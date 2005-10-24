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


package org.openvpms.component.presentation.tapestry.binding;

import org.apache.hivemind.Location;
import org.apache.log4j.Logger;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.binding.AbstractBindingFactory;

/**
 * This class is used to support JXPath style bindings in Tapestry
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JXPathBindingFactory extends AbstractBindingFactory {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(JXPathBindingFactory.class);

    /* (non-Javadoc)
     * @see org.apache.tapestry.binding.BindingFactory#createBinding(org.apache.tapestry.IComponent, java.lang.String, java.lang.String, org.apache.hivemind.Location)
     */
    public IBinding createBinding(IComponent root, String description, 
            String path, Location location) {
        if (logger.isDebugEnabled()) {
            logger.debug("createBinding for type " + root.getClass().getName()
                    + " and path " + path);
        }
        return new JXPathBinding(description, getValueConverter(), location, 
                root, path);
    }

}
