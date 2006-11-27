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
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test that ability to create and query on Documentss.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceDocumentTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceDocumentTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceDocumentTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceDocumentTestCase() {
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
     * Test the creation of a Document using the {@link NodeDescriptor}s
     */
    public void testDocumentCreationThruNodeDescriptors()
    throws Exception {
        Document document = (Document)service.create("document.common");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("document.common");
        NodeDescriptor ndesc;
        
        // set the name
        ndesc = adesc.getNodeDescriptor("name");
        assertTrue(ndesc != null);
        ndesc.setValue(document, "name.doc");
        assertTrue(document.getName().equals("name.doc"));
        
        // set the mime type
        ndesc = adesc.getNodeDescriptor("mimeType");
        assertTrue(ndesc != null);
        ndesc.setValue(document, "test/palin");
        assertTrue(document.getMimeType().equals("test/palin"));
        
        // set the doc size
        ndesc = adesc.getNodeDescriptor("size");
        assertTrue(ndesc != null);
        ndesc.setValue(document, "1000");
        assertTrue(document.getDocSize()== 1000);
        
        // set the checksum
        ndesc = adesc.getNodeDescriptor("checksum");
        assertTrue(ndesc != null);
        ndesc.setValue(document, "1234");
        assertTrue(document.getChecksum() == 1234);
        
        // set the contents
        ndesc = adesc.getNodeDescriptor("contents");
        assertTrue(ndesc != null);
        ndesc.setValue(document, "Jim Alateras".getBytes());
        assertTrue(document.getContents().length == "Jim Alateras".length());

        // save the document
        service.save(document);
    }
    
    /**
     * Test the creation of a simple document
     */
    public void testSimpleDocumentCreation()
    throws Exception {
        Document document = createDocument("jima.doc", "text/plain", 12, 12343, 
                "Jim Alateras".getBytes());
        service.save(document);
    }
    
    /**
     * Test the creation of multiple document and the retrievakl of some 
     * documents
     */
    public void testMultipleDocumentCreation()
    throws Exception {
        Document doc;
        IMObjectReference ref = null;
        
        for (int index = 0; index  < 10; index++) {
            doc = createDocument("jima" + index + ".doc", 
                    "text/plain", 12 + index, 12343 + index, 
                    ("Jim Alateras" + index).getBytes());
            ref = doc.getObjectReference();
            service.save(doc);
        }
        
        // retrieve the last document
        assertTrue(service.get(new ArchetypeQuery(
                new ObjectRefConstraint(ref))).getTotalResults() == 1);
    }
    
    /**
     * Test the creation of a simple document act
     */
    public void testDocumentActCreation()
    throws Exception {
        Document document = createDocument("jima.doc", "text/plain", 12, 12343, 
                "Jim Alateras".getBytes());
        service.save(document);
        DocumentAct docAct = createDocumentAct(document);
        service.save(docAct);
    }
    
    /** 
     * Test creation and retrieval of a document act
     */
    public void testDocumentActRetrieval()
    throws Exception {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, 
                "Jim Alateras".getBytes());
        service.save(document);
        DocumentAct docAct = createDocumentAct(document);
        service.save(docAct);
        
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefConstraint(
                docAct.getObjectReference())));
        assertTrue(page.getTotalResults() == 1);
        
        docAct = (DocumentAct)page.getResults().iterator().next();
        assertTrue(docAct.getDocReference().equals(document.getObjectReference()));
    }
    
    /** 
     * Test the modification of a document act
     */
    public void testDocumentActModification()
    throws Exception {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, 
                "Jim Alateras".getBytes());
        service.save(document);
        DocumentAct docAct = createDocumentAct(document);
        service.save(docAct);
        
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefConstraint(
                docAct.getObjectReference())));
        assertTrue(page.getTotalResults() == 1);
        
        docAct = (DocumentAct)page.getResults().iterator().next();
        assertTrue(docAct.getDocReference().equals(document.getObjectReference()));
        
        // create a new document reference
        document = createDocument("tima.doc", "text/plain", 234, 12343, 
                "Tim Anderson".getBytes());
        service.save(document);
        docAct.setDocReference(document.getObjectReference());
        service.save(docAct);
        
        // retrieve and check again
        page = service.get(new ArchetypeQuery(new ObjectRefConstraint(
                docAct.getObjectReference())));
        assertTrue(page.getTotalResults() == 1);
        docAct = (DocumentAct)page.getResults().iterator().next();
        assertTrue(docAct.getDocReference().equals(document.getObjectReference()));
    }
    
    /**
     * Test the deletion of a document act
     */
    public void testDocumentActDeletion()
    throws Exception {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, 
                "Jim Alateras".getBytes());
        service.save(document);
        DocumentAct docAct = createDocumentAct(document);
        service.save(docAct);
        
        // delete the document act
        service.remove(docAct);
        
        // ensure that we can still retrieve the document
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefConstraint(
                document.getObjectReference())));
        assertTrue(page.getTotalResults() == 1);
        
        // check that we can't retrieve the document act
        page = service.get(new ArchetypeQuery(new ObjectRefConstraint(
                docAct.getObjectReference())));
        assertTrue(page.getTotalResults() == 0);
    }
    
    /**
     * Create a document with the specified parameters
     * 
     * @param name
     *            the document name
     * @param mime
     *            the mime type
     * @param size
     *            the size of the doc
     * @param checksum
     *            the associated checksum
     * @param contents
     *            the contents
     * @return Document                                                            
     */
    public Document createDocument(String name, String mime, int size, long checksum, byte[] contents) {
        Document document = (Document)service.create("document.common");
        document.setName(name);
        document.setMimeType(mime);
        document.setDocSize(size);
        document.setChecksum(checksum);
        document.setContents(contents);
        
        return document;
    }
    
    /**
     * Create a document act given the specified document
     * 
     * @param document
     *            the document to associate with the act
     * @return DocumentAct            
     */
    public DocumentAct createDocumentAct(Document document) {
        DocumentAct act = (DocumentAct)service.create("document.act");
        act.setName("documentAct1");
        act.setDescription("This is the first document act");
        act.setDocVersion("ver1");
        act.setDocReference(document.getObjectReference());
        
        return act;
    }
}
