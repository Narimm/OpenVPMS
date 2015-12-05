package org.openvpms.web.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.LockManager;
import io.milton.http.fs.SimpleLockManager;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.webdav.resource.WebDAVResourceFactory;

import javax.servlet.ServletContext;

/**
 * A factory for {@code HttManager} instances.
 *
 * @author Tim Anderson
 */
public class HttpManagerFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Constructs a {@link HttpManagerFactory}.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public HttpManagerFactory(IArchetypeService service, DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
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
            setEnabledJson(false);
            LockManager lockManager = new SimpleLockManager(getCacheManager());
            setResourceFactory(new WebDAVResourceFactory(contextPath, service, handlers, lockManager));
        }

        /**
         * Initialises the WebDAV protocol.
         */
        @Override
        protected void initWebdavProtocol() {
            setWebDavProtocol(new WebDAVProtocol(handlerHelper, resourceTypeHelper, webdavResponseHandler, propertySources,
                                                 quotaDataAccessor, buildPatchSetter(), initPropertyAuthoriser(),
                                                 eTagGenerator, urlAdapter, resourceHandlerHelper, userAgentHelper(),
                                                 propFindRequestFieldParser(), propFindPropertyBuilder(),
                                                 displayNameFormatter, enableTextContentProperty));
        }
    }
}
