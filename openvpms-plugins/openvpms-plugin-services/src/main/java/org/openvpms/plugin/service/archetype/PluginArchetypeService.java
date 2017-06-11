package org.openvpms.plugin.service.archetype;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Collection;

/**
 * .
 *
 * @author Tim Anderson
 */
public interface PluginArchetypeService {

    /**
     * Returns the {@link ArchetypeDescriptor} for the given archetype.
     *
     * @param archetype the archetype
     * @return the descriptor corresponding to the archetype, or {@code null} if none is found
     */
    ArchetypeDescriptor getArchetypeDescriptor(String archetype);

    /**
     * Returns a bean for an object.
     *
     * @param object the object
     * @return the bean
     */
    IMObjectBean getBean(IMObject object);

    /**
     * Returns a bean for an act.
     *
     * @param object the object
     * @return the bean
     */
    ActBean getBean(Act object);

    /**
     * Returns a bean for an entity.
     *
     * @param object the object
     * @return the bean
     */
    EntityBean getBean(Entity object);

    /**
     * Create a domain object given its archetype.
     *
     * @param archetype the archetype name
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     */
    IMObject create(String archetype);

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     */
    void save(IMObject object);

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     */
    void save(Collection<? extends IMObject> objects);

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     */
    void remove(IMObject object);

    /**
     * Validates an object.
     *
     * @param object the object to validate
     */
    void validate(IMObject object);
}
