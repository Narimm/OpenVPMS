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

package org.openvpms.component.presentation.tapestry;

import java.io.Serializable;
import java.util.Stack;

import org.apache.tapestry.callback.ICallback;

/**
 *
 *  The Web application's Visit object.
 *  The Visit object is stored as a HttpSession attribute by the framework.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Visit implements Serializable {
    
  private static final long serialVersionUID = 37882478792384798L;
  
  private static final String appName = "OpenVPMS";
  
  private Stack<ICallback> callbackStack = new Stack<ICallback>();
  
  /**
   * @return Returns the callbackStack.
   */
  public Stack<ICallback> getCallbackStack()
  {
      return callbackStack;
  }
  /**
   * @param callbackStack The callbackStack to set.
   */
  public void setCallbackStack(Stack<ICallback> callbackStack)
  {
      this.callbackStack = callbackStack;
  }
  
  /**
   * @return Returns the appName.
   */
//  public String getAppName() {
//	  return appName;
//  }
  
//  public String getUserName() {
	  
//	  return appName;
//  }
}
