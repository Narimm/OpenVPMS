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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.system.common.jxpath.JXPathHelper;


/**
 * Macro evaluator. Evaluates macros embedded in text.
 * Macros are xpath expressions,  loaded from one or more property files named
 * <em>Macros.properties</em> located in the classpath. E.g:
 * <code>@macro1 = 'simple text'</code>
 * <code>@bid = concat(openvpms:get(., 'product.entity.sellingUnits'), ' twice a day')</code>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroEvaluator {

    /**
     * The macros.
     */
    private static Map<String, String> macros;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MacroEvaluator.class);


    /**
     * Evaluates any macros in the supplied text.
     *
     * @param text    the text
     * @param context the macro context
     * @return the text with macros evaluated
     */
    public static String evaluate(String text, Object context) {
        StringTokenizer tokens = new StringTokenizer(text, " \t\n\r", true);
        StringBuffer result = new StringBuffer();
        JXPathContext ctx = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            String number = "";
            // If token starts with numbers strip numbers and create number
			// variable
			// and if any left pass token to test for macro.
			int index = 0;
			while (token.length() > index) {
				if (!Character.isDigit(token.charAt(index)))
					break;
				index++;
			}
			if (index == 0)
				number = "";
			else
				number = token.substring(0, index);
			token = token.substring(index);
            String macro = macros.get(token);
            if (macro != null) {
                try {
                	if (ctx == null) {
                		ctx = JXPathHelper.newContext(context);
                		ctx.setVariables(new MacroVariables(context));
                	}                	
                    ctx.getVariables().declareVariable("number", number);
                    Object value = ctx.getValue(macro);
                    if (value != null) {
                        result.append(value);
                    }
                } catch (Throwable exception) {
                    result.append(token);
                    log.debug(exception);
                }
            } else {
                result.append(token);
            }
        }
        return result.toString();
    }

    /**
     * Variables implementation that evaluates macros.
     *
     */
    private static class MacroVariables implements Variables {

    	/**
    	 * The context to evaluate macro based varibales with
    	 */
    	private Object _context;
    	
        /**
         * The variables.
         */
        private Map<String, Object> _variables;
    	
		/**
		 * Constructor
		 * @param _context
		 */
		public MacroVariables(Object _context) {
			this._context = _context;
			_variables = new HashMap<String, Object>();
		}

		public void declareVariable(String name, Object value) {
			_variables.put(name, value);
		}

		public Object getVariable(String name) {
			Object result;
			try {
				JXPathContext ctx = JXPathHelper.newContext(_context);
				String macro = macros.get(name);
				if (macro != null) {
					result = ctx.getValue(macro);
				}
				else {
					result = _variables.get(name);
				}

            } catch (Throwable exception) {
            	result = null;
                log.debug(exception);
            }
			
			return result;
		}

		public boolean isDeclaredVariable(String name) {
			if (macros.containsKey(name)) {
				return true;
			} else if (_variables.containsKey(name)){
				return true;
			}
			else
				return false;
		}

		public void undeclareVariable(String name) {
			_variables.remove(name);
		}		
		    	
    }
    
    private static class Parser extends PropertiesParser {

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         */
        protected void parse(String key, String value) {
            macros.put(key, value);
        }
    }

    static {
        macros = new HashMap<String, String>();
        Parser parser = new Parser();
        parser.parse("Macros.properties");
    }

}
