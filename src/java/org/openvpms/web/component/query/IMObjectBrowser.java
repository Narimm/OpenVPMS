package org.openvpms.web.component.query;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.IMObjectViewer;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.view.DefaultLayoutStrategyFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * {@link IMObject} browser.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectBrowser extends IMObjectViewer {


    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param object the object to browse.
     */
    public IMObjectBrowser(IMObject object) {
        this(object, new DefaultLayoutStrategyFactory().create(object, true));
    }

    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param object the object to browse.
     * @param layout the layout strategy
     */
    public IMObjectBrowser(IMObject object, IMObjectLayoutStrategy layout) {
        super(object, layout);
    }

    /**
     * Returns a title for the browser.
     *
     * @return a title for the browser
     */
    public String getTitle() {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                getObject().getArchetypeId());
        return descriptor.getDisplayName();
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getComponentFactory() {
        return new NodeBrowserFactory();
    }

}
