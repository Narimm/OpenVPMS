/*
 * Copyright 2004 Chris Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.openvpms.component.presentation.tapestry.component;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;


/**
 * @author fus8882
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utils
{
    public static String DEFAULT = "Default";

    public static Class getClassForName(String className)
    {
        try
        {
            return Class.forName(className);
        }catch (ClassNotFoundException e)
        {
            throw new ApplicationRuntimeException(e);
        }
    }

    public static String unqualify(String className)
    {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static IPage findPage(IRequestCycle cycle, String pageName,
        String postfix)
    {
        IPage page = null;
        // Ask howard how to do this right!!
        try
        {
            page = cycle.getPage(pageName);
        }catch (ApplicationRuntimeException ae)
        {
            if (ae.getMessage().startsWith("Page"))
            {
                page = cycle.getPage(DEFAULT + postfix);
            }
            else
            {
                throw ae;
            }
        }

        return page;
    }

    /**
     * Thank you, AndroMDA project...
     * Linguistically pluralizes a singular noun. <p/>
     * <ul>
     * <li><code>noun</code> becomes <code>nouns</code></li>
     * <li><code>key</code> becomes <code>keys</code></li>
     * <li><code>word</code> becomes <code>words</code></li>
     * <li><code>property</code> becomes <code>properties</code></li>
     * <li><code>bus</code> becomes <code>busses</code></li>
     * <li><code>boss</code> becomes <code>bosses</code></li>
     * </ul>
     * <p>
     * Whitespace as well as <code>null></code> arguments will return an empty
     * String.
     * </p>
     * 
     * @param singularNoun A singularNoun to pluralize
     * @return The plural of the argument singularNoun
     */
    public static String pluralize(String singularNoun)
    {
        String pluralNoun = singularNoun;

        int nounLength = pluralNoun.length();

        if (nounLength == 1)
        {
            pluralNoun = pluralNoun + 's';
        }
        else if (nounLength > 1)
        {
            char secondToLastChar = pluralNoun.charAt(nounLength - 2);

            if (pluralNoun.endsWith("y"))
            {
                switch (secondToLastChar)
                {
                    case 'a' : // fall-through
                    case 'e' : // fall-through
                    case 'i' : // fall-through
                    case 'o' : // fall-through
                    case 'u' :
                        pluralNoun = pluralNoun + 's';
                        break;
                    default :
                        pluralNoun = pluralNoun.substring(0, nounLength - 1)
                            + "ies";
                }
            }
            else if (pluralNoun.endsWith("s"))
            {
                switch (secondToLastChar)
                {
                    case 's' :
                        pluralNoun = pluralNoun + "es";
                        break;
                    default :
                        pluralNoun = pluralNoun + "ses";
                }
            }
            else
            {
                pluralNoun = pluralNoun + 's';
            }
        }
        return pluralNoun;
    }
    
    public static String unCamelCase(String name)
    {
        ArrayList words = new ArrayList();
        Perl5Util perl = new Perl5Util();
    
        while (perl.match("/(\\w+?)([A-Z].*)/", name))
        {
            String word = perl.group(1);
            name = perl.group(2);
            words.add(StringUtils.capitalise(word));
        }
    
        words.add(StringUtils.capitalise(name));
    
        return StringUtils.join(words.iterator(), " ");
    }

}
