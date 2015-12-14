package org.openvpms.web.webdav.milton;

import io.milton.http.http11.DefaultETagGenerator;
import io.milton.resource.Resource;
import org.openvpms.web.webdav.resource.IMObjectResource;

/**
 * An ETag generator for {@link IMObjectResource}.
 * <p>
 * This appends the version of the resource to the resource's unique identifier.
 *
 * @author Tim Anderson
 */
public class IMObjectResourceETagGenerator extends DefaultETagGenerator {

    /**
     * ETag's serve to identify a particular version of a particular resource.
     * <p>
     * If the resource changes, or is replaced, then this value should change
     *
     * @param resource - the resource to generate the ETag for
     * @return an ETag which uniquely identifies this version of this resource
     */
    @Override
    public String generateEtag(Resource resource) {
        String result;
        String uniqueId = resource.getUniqueId();
        if (resource instanceof IMObjectResource && uniqueId != null) {
            result = uniqueId + "-" + ((IMObjectResource) resource).getVersion();
        } else {
            result = super.generateEtag(resource);
        }
        return result;
    }
}
