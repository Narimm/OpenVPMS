package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ArchetypeQueryHelper} class
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class ArchetypeQueryHelperTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link ArchetypeQueryHelper#getName(IMObjectReference, IArchetypeService)}.
     */
    @Test
    public void testGetName() {
        assertNull(ArchetypeQueryHelper.getName(null, getArchetypeService()));
        Party pet = (Party) create("party.patientpet");
        pet.setName("Fido");
        pet.getDetails().put("species", "CANINE");
        save(pet);

        assertEquals("Fido", ArchetypeQueryHelper.getName(pet.getObjectReference(), getArchetypeService()));
    }
}
