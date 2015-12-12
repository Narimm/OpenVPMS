package org.openvpms.web.webdav.milton;

import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.UrlAdapter;
import io.milton.http.http11.ETagGenerator;
import io.milton.http.quota.QuotaDataAccessor;
import io.milton.http.webdav.DisplayNameFormatter;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropFindRequestFieldParser;
import io.milton.http.webdav.PropPatchSetter;
import io.milton.http.webdav.PropertyMap;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.UserAgentHelper;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends the Milton {@code WebDavProtocol} by adding LOCK and UNLOCK support.
 *
 * @author Tim Anderson
 */
public class WebDAVProtocol extends WebDavProtocol {

    /**
     * The lock handler.
     */
    private final LockHandler lockHandler;

    /**
     * The unlock handler.
     */
    private final UnlockHandler unlockHandler;


    /**
     * Constructs a {@link WebDAVProtocol}.
     *
     * @param handlerHelper             the handler helper
     * @param resourceTypeHelper        the resource type helper
     * @param responseHandler           the response handler
     * @param propertySources           the property sources
     * @param quotaDataAccessor         the quota data accessor
     * @param patchSetter               the patch setter
     * @param propertyAuthoriser        the property authoriser
     * @param eTagGenerator             the e-tag generator
     * @param urlAdapter                the URL adapter
     * @param resourceHandlerHelper     the resource handler helper
     * @param userAgentHelper           the user agent helper
     * @param requestFieldParser        the request field parser
     * @param propertyBuilder           the property builder
     * @param displayNameFormatter      the display name formatter
     * @param enableTextContentProperty determines if the text context property should be enabled
     */
    public WebDAVProtocol(HandlerHelper handlerHelper, ResourceTypeHelper resourceTypeHelper,
                          WebDavResponseHandler responseHandler, List<PropertySource> propertySources,
                          QuotaDataAccessor quotaDataAccessor, PropPatchSetter patchSetter,
                          PropertyAuthoriser propertyAuthoriser, ETagGenerator eTagGenerator, UrlAdapter urlAdapter,
                          ResourceHandlerHelper resourceHandlerHelper, UserAgentHelper userAgentHelper,
                          PropFindRequestFieldParser requestFieldParser, PropFindPropertyBuilder propertyBuilder,
                          DisplayNameFormatter displayNameFormatter, boolean enableTextContentProperty) {
        super(handlerHelper, resourceTypeHelper, responseHandler, propertySources, quotaDataAccessor, patchSetter,
              propertyAuthoriser, eTagGenerator, urlAdapter, resourceHandlerHelper, userAgentHelper, requestFieldParser,
              propertyBuilder, displayNameFormatter, enableTextContentProperty);
        lockHandler = new LockHandler(responseHandler, resourceHandlerHelper);
        unlockHandler = new UnlockHandler(responseHandler, resourceHandlerHelper);
        PropertyMap properties = getPropertyMap();
        properties.add(new LockDiscoveryProperty());
        properties.add(new SupportedLockProperty());
    }

    /**
     * Returns the protocol handlers.
     *
     * @return the protocol handlers
     */
    @Override
    public Set<Handler> getHandlers() {
        Set<Handler> result = new HashSet<>();
        result.add(lockHandler);
        result.add(unlockHandler);
        result.addAll(super.getHandlers());
        return result;
    }

}
