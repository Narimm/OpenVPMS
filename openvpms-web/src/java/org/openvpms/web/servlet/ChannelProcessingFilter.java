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
package org.openvpms.web.servlet;

import org.springframework.security.web.access.channel.ChannelEntryPoint;
import org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * A filter that can optionally require all requests to go over https.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ChannelProcessingFilter extends GenericFilterBean {

    /**
     * Entry point to retry the original request using HTTPS.
     */
    private ChannelEntryPoint entryPoint = new RetryWithHttpsEntryPoint();

    /**
     * Determines if secure requests are required.
     */
    private boolean secure = true;


    /**
     * Determines if secure requests are required.
     * <p/>
     * Defaults to <tt>true</tt>.
     *
     * @param secure if <tt>true</tt> require all requests to go over https.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.isSecure() || !secure) {
            chain.doFilter(request, response);
        } else {
            entryPoint.commence((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }
}
