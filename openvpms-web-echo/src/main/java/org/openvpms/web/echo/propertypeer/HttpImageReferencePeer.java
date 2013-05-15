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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.echo.propertypeer;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.HttpImageReference;
import nextapp.echo2.app.componentxml.InvalidPropertyException;
import nextapp.echo2.app.componentxml.PropertyXmlPeer;
import nextapp.echo2.app.componentxml.propertypeer.ExtentPeer;
import nextapp.echo2.app.util.DomUtil;
import org.w3c.dom.Element;


/**
 * A <tt>PropertyXmlPeer</tt> for <tt>nextapp.echo2.app.ResourceImageReference</tt> properties.
 * <p/>
 * This really should be part of the nextapp distribution - not sure why it was omitted.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class HttpImageReferencePeer implements PropertyXmlPeer {

    /**
     * @see nextapp.echo2.app.componentxml.PropertyXmlPeer#getValue(java.lang.ClassLoader,
     *      java.lang.Class, org.w3c.dom.Element)
     */
    public Object getValue(ClassLoader classLoader, Class objectClass, Element propertyElement)
        throws InvalidPropertyException {
        if (propertyElement.hasAttribute("value")) {
            return new HttpImageReference(propertyElement.getAttribute("value"));
        } else {
            Element element = DomUtil.getChildElementByTagName(propertyElement, "http-image-reference");
            if (!element.hasAttribute("url")) {
                throw new InvalidPropertyException("Invalid HttpImageReference property (url not specified).", null);
            }
            String url = element.getAttribute("url");
            Extent width = null;
            if (element.hasAttribute("width")) {
                width = ExtentPeer.toExtent(element.getAttribute("width"));
            }
            Extent height = null;
            if (element.hasAttribute("height")) {
                height = ExtentPeer.toExtent(element.getAttribute("height"));
            }
            return new HttpImageReference(url, width, height);
        }
    }
}

