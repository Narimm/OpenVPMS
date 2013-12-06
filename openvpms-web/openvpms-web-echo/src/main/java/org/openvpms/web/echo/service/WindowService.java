/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.service;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webrender.BaseHtmlDocument;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.service.JavaScriptService;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * Replacement for the echo2 {@code WindowHtmlService}.
 * <p/>
 * This uses a custom ClientEngine.js to handle session expiry.
 *
 * @author Tim Anderson
 */
public class WindowService implements Service {

    /**
     * The singleton service instance.
     */
    public static final WindowService INSTANCE = new WindowService();

    /**
     * Root element identifier.
     */
    public static final String ROOT_ID = "c_root";

    /**
     * Client Engine JavaScript code.
     */
    public static final Service CLIENT_ENGINE = JavaScriptService.forResource(
            "Echo.ClientEngine", "/org/openvpms/web/echo/js/ClientEngine.js");

    /**
     * Default constructor.
     */
    private WindowService() {
        super();
    }

    /**
     * @see nextapp.echo2.webrender.Service#getId()
     */
    public String getId() {
        return WebRenderServlet.SERVICE_ID_DEFAULT;
    }

    /**
     * @see nextapp.echo2.webrender.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    /**
     * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
     */
    public void service(Connection conn) throws IOException {
        ContainerInstance ci = (ContainerInstance) conn.getUserInstance();
        conn.setContentType(ContentType.TEXT_HTML);

        boolean debug = !("false".equals(conn.getServlet().getInitParameter("echo2.debug")));

        BaseHtmlDocument baseDoc = new BaseHtmlDocument(ROOT_ID);
        baseDoc.setGenarator(ApplicationInstance.ID_STRING);
        baseDoc.addJavaScriptInclude(ci.getServiceUri(CLIENT_ENGINE));

        // Add initialization directive.
        baseDoc.getBodyElement().setAttribute("onload", "EchoClientEngine.init('" + ci.getServletUri() + "', "
                                                        + debug + ");");

        Element bodyElement = baseDoc.getBodyElement();

        // Set body element CSS style.
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "absolute");
        cssStyle.setAttribute("font-family", "verdana, arial, helvetica, sans-serif");
        cssStyle.setAttribute("font-size", "10pt");
        cssStyle.setAttribute("height", "100%");
        cssStyle.setAttribute("width", "100%");
        cssStyle.setAttribute("padding", "0px");
        cssStyle.setAttribute("margin", "0px");
        cssStyle.setAttribute("overflow", "hidden");
        bodyElement.setAttribute("style", cssStyle.renderInline());

        // Render.
        baseDoc.render(conn.getWriter());
    }

}
