package org.openvpms.web.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.values.SupportedLocksValueWriter;
import io.milton.http.values.ValueWriter;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.webdav.resource.EditableDocuments;
import org.openvpms.web.webdav.resource.ResourceLockManager;
import org.openvpms.web.webdav.resource.WebDAVResourceFactory;

import javax.servlet.ServletContext;
import java.util.List;

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
     * The WebDAV lock manager.
     */
    private final ResourceLockManager lockManager;

    /**
     * The document archetypes that may be edited.
     */
    private final EditableDocuments documents;

    /**
     * Constructs a {@link HttpManagerFactory}.
     *
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the WebDAV lock manager
     * @param documents   the document archetypes that may be edited
     */
    public HttpManagerFactory(IArchetypeService service, DocumentHandlers handlers, ResourceLockManager lockManager,
                              EditableDocuments documents) {
        this.service = service;
        this.handlers = handlers;
        this.lockManager = lockManager;
        this.documents = documents;
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
            setResourceFactory(new WebDAVResourceFactory(contextPath, service, handlers, lockManager, documents));
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
