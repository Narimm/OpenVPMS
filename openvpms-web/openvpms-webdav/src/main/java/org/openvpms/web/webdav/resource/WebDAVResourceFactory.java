package org.openvpms.web.webdav.resource;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.Date;

/**
 * A {@code ResourceFactory} for {@link DocumentActResource} and {@link DocumentResource}.
 *
 * @author Tim Anderson
 */
public class WebDAVResourceFactory implements ResourceFactory {

    /**
     * The root path. This consists of the servlet context path + "/document".
     */
    private final String root;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The lock manager.
     */
    private final ResourceLockManager lockManager;

    /**
     * The document archetypes that may be edited.
     */
    private final EditableDocuments documents;

    /**
     *
     */
    private Date baseDate = new Date();

    /**
     * Constructs a {@link WebDAVResourceFactory}.
     *
     * @param contextPath the servlet context path
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the lock manager
     */
    public WebDAVResourceFactory(String contextPath, IArchetypeService service, DocumentHandlers handlers,
                                 ResourceLockManager lockManager, EditableDocuments documents) {
        this.service = service;
        this.handlers = handlers;
        this.lockManager = lockManager;
        this.documents = documents;

        if (!contextPath.endsWith("/")) {
            contextPath += "/";
        }
        root = contextPath + "document";
    }

    /**
     * Locate an instance of a resource at the given url and on the given host.
     * <p>
     * The host argument can be used for applications which implement virtual
     * domain hosting. But portable applications (ie those which do not depend on the host
     * name) should ignore the host argument.
     * <p>
     * Note that the host will include the port number if it was specified in
     * the request
     * <p>
     * The path argument is just the part of the request url with protocol, host, port
     * number, and request parameters removed
     * <p>
     * E.g. for a request <PRE>http://milton.ettrema.com:80/downloads/index.html?ABC=123</PRE>
     * the corresponding arguments will be:
     * <PRE>
     * host: milton.ettrema.com:80
     * path: /downloads/index.html
     * </PRE>
     * Note that your implementation should not be sensitive to trailing slashes
     * E.g. these paths should return the same resource /apath and /apath/
     * <p>
     * Return null if there is no associated {@see Resource} object.
     * <p>
     * You should generally avoid using any request information other then that
     * provided in the method arguments. But if you find you need to you can access the
     * request and response objects from HttpManager.request() and HttpManager.response()
     *
     * @param host Full host name with port number, e.g. milton.ettrema.com:80
     * @param path Relative path on server, e.g. /downloads/index.html
     * @return the associated Resource object, or null if there is none
     */
    public Resource getResource(String host, String path) {
        Resource resource = null;
        String[] elements = getPathParts(path);
        if (elements.length == 1) {
            resource = getDocumentAct(elements[0]);
        } else if (elements.length == 2) {
            resource = getDocument(elements[0], elements[1]);
        }
        return resource;
    }

    /**
     * Returns a {@link DocumentActResource} given its id.
     *
     * @param id the document act id
     * @return the corresponding document act resource, or {@code null} if none is found
     */
    private DocumentActResource getDocumentAct(String id) {
        DocumentActResource result = null;
        try {
            if (!StringUtils.isEmpty(id)) {
                ArchetypeQuery query = new ArchetypeQuery(documents.getArchetypes(), true, true);
                query.add(Constraints.eq("id", Long.valueOf(id)));
                IMObjectQueryIterator<DocumentAct> iter = new IMObjectQueryIterator<>(service, query);
                if (iter.hasNext()) {
                    result = new DocumentActResource(iter.next(), service, handlers, lockManager, baseDate);
                }
            }
        } catch (NumberFormatException ignore) {
        }
        return result;
    }

    /**
     * Returns a {@link DocumentResource} given the parent id and the document name.
     *
     * @param id   the document act id
     * @param name the document name
     * @return the corresponding document resource, or {@code null} if none is found
     */
    private Resource getDocument(String id, String name) {
        Resource result = null;
        DocumentActResource act = getDocumentAct(id);
        if (act != null) {
            result = act.child(name);
        }
        return result;
    }

    /**
     * Splits a path after the root into parts.
     *
     * @param path the path
     * @return the remaining parts after the root
     */
    private String[] getPathParts(String path) {
        path = StringUtils.removeEnd(path, "/.");
        path = StringUtils.removeStart(path, root);
        path = StringUtils.removeEnd(path, "/");
        return Path.path(path).getParts();
    }

}
