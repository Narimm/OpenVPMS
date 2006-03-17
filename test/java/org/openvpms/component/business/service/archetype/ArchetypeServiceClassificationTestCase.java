/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

// spring-context
import java.util.Date;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// log4j
import org.apache.log4j.Logger;

/**
 * Test classification related functions through the 
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceClassificationTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceClassificationTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceClassificationTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceClassificationTestCase() {
    }

    /**
     * Test OVPMS-241 bug
     */
    public void testOVPMS241()
    throws Exception {
        // create and save a classification
        service.save(createContactPurpose("private"));
        
        Contact contact = createPhoneContact("03", "9763434");
        
        // add a purpose
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("contact.phoneNumber");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("purposes");
        
        List<IMObject> purposes = service.get(new String[]{"classification.contactPurpose"}, true);
        int acount = purposes.size(); 
        
        
        ndesc.addChildToCollection(contact, purposes.get(0));
        assertTrue(contact.getClassifications().size() == 1);
        service.save(contact);
        
        contact = (Contact)service.getById(contact.getArchetypeId(), contact.getUid());
        assertTrue(contact != null);
        assertTrue(contact.getClassifications().size() == 1);
        
        // remove a contact and save all
        contact.removeClassification(contact.getClassifications().iterator().next());
        service.save(contact);
        contact = (Contact)service.getById(contact.getArchetypeId(), contact.getUid());
        assertTrue(contact != null);
        assertTrue(contact.getClassifications().size() == 0);
        purposes = service.get(new String[]{"classification.contactPurpose"}, true);
        assertTrue(acount == purposes.size()); 
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml" 
                };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
    
    /**
     * Create and return a phone number
     * 
     * @param areaCode
     *            the area code, numeric only
     * @param telephoneNumber
     *            a telephone number, numeric only
     * @return Contact                        
     */
    private Contact createPhoneContact(String areaCode, String telephoneNumber) {
        Contact contact = (Contact)service.create("contact.phoneNumber");
        contact.getDetails().setAttribute("areaCode", areaCode);
        contact.getDetails().setAttribute("telephoneNumber", telephoneNumber);
        contact.setActiveStartTime(new Date());
        
        return contact;
    }
    
    /**
     * Create a contact purpose
     * 
     * @param purpose
     *            the contact purpose
     * @return Classification
     */
    private Classification createContactPurpose(String purpose) {
        Classification classification = (Classification)service.create(
                "classification.contactPurpose");
        classification.setName(purpose);
        
        return classification;
    }
}
