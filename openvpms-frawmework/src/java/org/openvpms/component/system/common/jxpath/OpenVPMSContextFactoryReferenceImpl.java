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


package org.openvpms.component.system.common.jxpath;

// commons-jxpath
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathContextFactoryConfigurationError;
import org.apache.log4j.Logger;

/**
 * Part of the JXPath extensions
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVPMSContextFactoryReferenceImpl extends JXPathContextFactory {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OpenVPMSContextFactoryReferenceImpl.class);

    /**
     * 
     */
    public OpenVPMSContextFactoryReferenceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.JXPathContextFactory#newContext(org.apache.commons.jxpath.JXPathContext, java.lang.Object)
     */
    @Override
    public JXPathContext newContext(JXPathContext parentContext,
            Object contextBean) throws JXPathContextFactoryConfigurationError {
        return new OpenVPMSContextReferenceImpl(parentContext, contextBean);
    }

}
