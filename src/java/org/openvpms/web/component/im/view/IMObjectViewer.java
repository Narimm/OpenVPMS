package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.spring.ServiceHelper;


/**
 * {@link IMObject} viewer.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectViewer extends AbstractIMObjectView {


    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to view.
     */
    public IMObjectViewer(IMObject object) {
        this(object, new DefaultLayoutStrategyFactory(false).create(
                object, true));
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to view.
     * @param layout the layout strategy
     */
    public IMObjectViewer(IMObject object, IMObjectLayoutStrategy layout) {
        super(object, layout);
    }

    /**
     * Returns a title for the viewer.
     *
     * @return a title for the viewer
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
        return new ReadOnlyComponentFactory();
    }

}
