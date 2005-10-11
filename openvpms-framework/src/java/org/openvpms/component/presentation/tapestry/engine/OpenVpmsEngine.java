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

package org.openvpms.component.presentation.tapestry.engine;

import org.apache.log4j.Logger;
import org.apache.tapestry.engine.BaseEngine;

/**
 * 
 * The custom application engine. Main purpose of this application
 * specific engine  is to provide the Springframework ApplicationContext.
 * 
 * BaseEngine will be depreceated in Tapestry 3.1, so this will certainly
 * go away.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVpmsEngine extends BaseEngine {
    private static final long serialVersionUID = 3257288032683177524L;
    
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(OpenVpmsEngine.class);
    
}