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

package org.openvpms.web.servlet;

import nextapp.echo2.app.Command;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.app.OpenVPMSApp;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Download servlet. Downloads {@link Document}s to clients.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DownloadServlet extends HttpServlet {

    /**
     * The set of temporary documents. These are deleted after being served.
     */
    private static Set<IMObjectReference> tempDocs
        = Collections.synchronizedSet(new HashSet<IMObjectReference>());

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DownloadServlet.class);

    /**
     * Start a download.
     *
     * @param document the document to download
     */
    public static void startDownload(Document document) {
        boolean isNew = document.isNew();
        if (isNew) {
            // need to save the document in order for it to be served.
            IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
            service.save(document);
            tempDocs.add(document.getObjectReference());
        }
        String qname = document.getArchetypeId().getQualifiedName();
        StringBuffer uri = new StringBuffer();
        uri.append(ServletHelper.getRedirectURI("download"));
        uri.append("?qname=");
        uri.append(qname);
        uri.append("&id=");
        uri.append(document.getId());
        Command command = new BrowserOpenWindowCommand(
            uri.toString(), null,
            "width=800,height=600,menubar=yes,toolbar=yes,location=yes,resizable=yes,scrollbars=yes");
        OpenVPMSApp.getInstance().enqueueCommand(command);
    }

    /**
     * Initialises the servlet.
     *
     * @throws ServletException for any error
     */
    @Override
    public void init() throws ServletException {
        super.init();
        ApplicationContext context
            = WebApplicationContextUtils.getRequiredWebApplicationContext(
            getServletContext());
        handlers = (DocumentHandlers) context.getBean("documentHandlers");
    }

    /**
     * Handles a GET request.
     * <p/>
     * If the request is incorrectly formatted, returns an HTTP "Bad Request"
     * message.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException      if an input or output error is detected when the
     *                          servlet handles the GET request
     * @throws ServletException if the request for the GET could not be handled
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException {
        IArchetypeService service
            = ArchetypeServiceHelper.getArchetypeService();
        String qname = request.getParameter("qname");
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(qname) || StringUtils.isEmpty(id)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            IMObjectReference ref = new IMObjectReference(
                new ArchetypeId(qname), Integer.valueOf(id));
            IMObject object = service.get(ref);
            if (!(object instanceof Document)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                serveDocument((Document) object, response, service);
            }
        }
    }

    /**
     * Serves a document
     *
     * @param doc      the document
     * @param response the response
     * @param service  the archetype service
     * @throws IOException for any I/O error
     */
    private void serveDocument(Document doc, HttpServletResponse response,
                               IArchetypeService service) throws IOException {
        try {
            DocumentHandler handler = handlers.get(
                doc.getName(), doc.getArchetypeId().getShortName(),
                doc.getMimeType());
            if (DocFormats.XML_TYPE.equals(doc.getMimeType())) {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.getName() + "\"");
            } else {
                response.setHeader("Content-Disposition", "inline; filename=\"" + doc.getName() + "\"");
            }
            response.setContentType(doc.getMimeType());
            response.setContentLength(doc.getDocSize());
            InputStream stream = null;
            try {
                stream = handler.getContent(doc);
                IOUtils.copy(stream, response.getOutputStream());
            } finally {
                IOUtils.closeQuietly(stream);
            }

            // todo - need to support case where two users download the same
            // document simultaneously
            if (tempDocs.remove(doc.getObjectReference())) {
                service.remove(doc);
            }
        } catch (OpenVPMSException exception) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Failed to serve document: name=" + doc.getName()
                      + ", shortName="
                      + doc.getArchetypeId().getShortName()
                      + ", mimeType=" + doc.getMimeType(),
                      exception);
        }
    }
}
