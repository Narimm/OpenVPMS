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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Download servlet. Downloads {@link Document}s to clients.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DownloadServlet extends HttpServlet {

    /**
     * The archetype service.
     */
    private IArchetypeService _service;

    @Override
    public void init() throws ServletException {
        super.init();
        WebApplicationContext context
                = WebApplicationContextUtils.getWebApplicationContext(
                getServletContext());
        _service = (IArchetypeService) context.getBean("archetypeService");
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
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        String qname = request.getParameter("qname");
        String linkId = request.getParameter("linkId");
        if (StringUtils.isEmpty(qname) || StringUtils.isEmpty(linkId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            ArchetypeId id = new ArchetypeId(qname);
            IMObjectReference ref = new IMObjectReference(id, linkId);
            IMObject object = ArchetypeQueryHelper.getByObjectReference(
                    _service,
                    ref);
            if (!(object instanceof Document)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Document doc = (Document) object;
                response.setHeader("Content-Disposition",
                                   "inline; filename=\"" + doc.getName()
                                           + "\"");
                response.setContentType(doc.getMimeType());
                response.setContentLength((int) doc.getDocSize());
                response.getOutputStream().write(doc.getContents());
            }
        }
    }
}
