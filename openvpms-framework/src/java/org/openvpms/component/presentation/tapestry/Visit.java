/*
 * Created on 06.04.2005
 * 
 */
package org.openvpms.component.presentation.tapestry;

import java.io.Serializable;
import java.util.Stack;

/**
 *
 *  The Web application's Visit object.
 *  The Visit object is stored as a HttpSession attribute by the framework.
 * 
 *  @author andyman
 *  @version  $Id: Visit.java,v 1.2 2005/05/03 11:05:49 andyman232323 Exp $
 * 
 */

public class Visit implements Serializable {
    
  private static final long serialVersionUID = 37882478792384798L;
  
  private static final String appName = "OpenVPMS";
  
  private Stack callbackStack = new Stack();
  
  /**
   * @return Returns the callbackStack.
   */
  public Stack getCallbackStack()
  {
      return callbackStack;
  }
  /**
   * @param callbackStack The callbackStack to set.
   */
  public void setCallbackStack(Stack callbackStack)
  {
      this.callbackStack = callbackStack;
  }
  
  /**
   * @return Returns the appName.
   */
  public String getAppName() {
	  return appName;
  }
  
  public String getUserName() {
	  
	  return appName;
  }
  


}
