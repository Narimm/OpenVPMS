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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;

/**
 * Extend the default {@link JXPathCOntextReferenceImpl} to support 
 * BigDecimal and BigInteger maths for OpenVPMS.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVPMSContextReferenceImpl extends JXPathContextReferenceImpl {
    /**
     * A singleton tree compile.
     */
    private static final Compiler COMPILER = new OpenVPMSTreeCompiler();

    /**
     * Support for base class construction
     * 
     * @param parentContext
     * @param contextBean
     */
    public OpenVPMSContextReferenceImpl(JXPathContext parentContext,
            Object contextBean) {
        super(parentContext, contextBean);
        // TODO Auto-generated constructor stub
    }

    /**
     * Support for base class construction
     * @param parentContext
     * @param contextBean
     * @param contextPointer
     */
    public OpenVPMSContextReferenceImpl(JXPathContext parentContext,
            Object contextBean, Pointer contextPointer) {
        super(parentContext, contextBean, contextPointer);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.JXPathContextReferenceImpl#getCompiler()
     */
    @Override
    protected Compiler getCompiler() {
        return COMPILER;
    }

}
