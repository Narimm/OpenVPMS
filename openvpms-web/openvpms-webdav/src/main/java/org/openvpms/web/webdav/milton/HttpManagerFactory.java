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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.values.SupportedLocksValueWriter;
import io.milton.http.values.ValueWriter;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.webdav.resource.ResourceLockManager;
import org.openvpms.web.webdav.resource.WebDAVResourceFactory;
import org.openvpms.web.webdav.session.SessionManager;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * A factory for {@code HttManager} instances.
 *
 * @author Tim Anderson
 */
public class HttpManagerFactory {

    /**
     * The sessions.
     */
    private final SessionManager sessions;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The WebDAV lock manager.
     */
    private final ResourceLockManager lockManager;

    /**
     * Constructs a {@link HttpManagerFactory}.
     *
     * @param sessions    the session manager
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the WebDAV lock manager
     */
    public HttpManagerFactory(SessionManager sessions, IArchetypeService service, DocumentHandlers handlers,
                              ResourceLockManager lockManager) {
        this.sessions = sessions;
        this.service = service;
        this.handlers = handlers;
        this.lockManager = lockManager;
    }

    /**
     * Creates a new {@code HttpManager}.
     *
     * @param context the servlet context
     * @return a new {@code HttpManager}
     */
    public HttpManager create(ServletContext context) {
        Builder builder = new Builder(context.getContextPath());

        return builder.buildHttpManager();
    }

    private class Builder extends HttpManagerBuilder {

        public Builder(String contextPath) {
            setEnableCookieAuth(false);   // cookies aren't supported by OpenOffice
            setEnabledJson(false);
            setResourceFactory(new WebDAVResourceFactory(contextPath, sessions, service, handlers, lockManager));
            seteTagGenerator(new IMObjectResourceETagGenerator());

            // insert the various lock property writers. These need to go before the ToStringValueWriter which is the
            // last in the list
            List<ValueWriter> valueWriters = getValueWriters().getValueWriters();
            int size = valueWriters.size();
            valueWriters.add(size - 1, new LockDiscoveryPropertyWriter());
            valueWriters.add(size, new SupportedLocksValueWriter());
        }

        /**
         * Initialises the WebDAV protocol.
         */
        @Override
        protected void initWebdavProtocol() {
            setWebDavProtocol(new WebDAVProtocol(handlerHelper, resourceTypeHelper, webdavResponseHandler,
                                                 propertySources, quotaDataAccessor, buildPatchSetter(),
                                                 initPropertyAuthoriser(), eTagGenerator, urlAdapter,
                                                 resourceHandlerHelper, userAgentHelper(),
                                                 propFindRequestFieldParser(), propFindPropertyBuilder(),
                                                 displayNameFormatter, enableTextContentProperty));
        }
    }
}
