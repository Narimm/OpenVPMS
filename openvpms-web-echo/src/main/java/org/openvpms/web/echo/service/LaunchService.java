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

import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;

import java.io.IOException;

/**
 * Launches a new instance of an Echo application.
 * <p/>
 * This is a replacement of the {@code NewInstanceService}, in order to use the {@link WindowService} instead
 * of the {@code WindowHtmlService}.
 *
 * @author Tim Anderson
 */
public class LaunchService implements Service {

    /**
     * Singleton instance.
     */
    public static final LaunchService INSTANCE = new LaunchService();

    /**
     * Default constructor.
     */
    private LaunchService() {
        super();
    }

    /**
     * @see nextapp.echo2.webrender.Service#getId()
     */
    public String getId() {
        return WebRenderServlet.SERVICE_ID_NEW_INSTANCE;
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
        ContainerInstance.newInstance(conn);
        WindowService.INSTANCE.service(conn);
    }
}
