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

package org.openvpms.component.system.service.jxpath;

// java
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

// commons-beanutils
import org.apache.commons.beanutils.MethodUtils;

// ognl
import ognl.Ognl;
import ognl.OgnlContext;

// commons-jxpath
import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the JXPath expressions on etity objects and descriptors.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JXPathTestCase extends BaseTestCase {

    /**
     * Cache a reference to the Archetype service
     */
    private ArchetypeService service;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JXPathTestCase.class);
    }

    /**
     * Constructor for JXPathTestCase.
     * 
     * @param arg0
     */
    public JXPathTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        Hashtable gparams = getTestData().getGlobalParams();
        String afile = (String) gparams.get("assertionFile");
        String dir = (String) gparams.get("dir");
        String extension = (String) gparams.get("extension");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(dir,
                new String[] { extension }, afile);
        service = new ArchetypeService(cache);
        assertTrue(service != null);
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test JXPath on node descriptors
     */
    public void testPersonNodeDescriptors() throws Exception {

        // retrieve the node descriptor for animal.pet
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("person.person");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("firstName");
        assertTrue(ndesc != null);
        assertTrue(((Boolean) getValue(adesc,
                "nodeDescriptors/firstName/string")).booleanValue());
        assertTrue(((Boolean) getValue(adesc,
                "nodeDescriptors/lastName/string")).booleanValue());
        assertTrue(((Boolean) getValue(adesc,
                "nodeDescriptors/lastName/identifier")).booleanValue() == false);
        assertTrue(((Boolean) getValue(adesc,
                "boolean(nodeDescriptors/lastName/string)"))
                .booleanValue());
        assertTrue(getValue(adesc, "nodeDescriptors/jimbo") == null);
    }

    /**
     * Test JXPath using the TestPage
     */
    public void testPageWithNodeDescriptorAndValue() throws Exception {
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("person.person");

        Person person = (Person) service.create("person.person");
        person.setFirstName("jim");
        person.setLastName("alateras");
        
        TestPage page = new TestPage(person, adesc);
        assertTrue(page != null);

        assertTrue(getValue(page,
                "pathToObject(model,  node/nodeDescriptors/lastName/path)")
                .equals("alateras"));
        assertTrue(getValue(page,
                "pathToObject(model,  node/nodeDescriptors/firstName/path)")
                .equals("jim"));

        setValue(
                page,
                "pathToObject(model,  node/nodeDescriptors/firstName/path)",
                "Bernie");
        assertTrue(getValue(page,
                "pathToObject(model,  node/nodeDescriptors/firstName/path)")
                .equals("Bernie"));
    }
    
    /**
     * Test the path to collection bug ovpms-131
     */
    public void testOVPMS131()
    throws Exception {
        Person person = new Person();
        person.setFirstName("Jim");
        person.setLastName("Alateras");
        EntityIdentity id1 = new EntityIdentity();
        id1.setName("jimbo");
        EntityIdentity id2 = new EntityIdentity();
        id2.setName("jimmya");
        person.addIdentity(id1);
        person.addIdentity(id2);
        assertTrue(person.getIdentities().size() == 2);
        assertTrue(person.getIdentities() != null);
         
    }
    
    /**
     * Test that the bug to ovpms-135 is resolved
     */
    public void testNonMandatoryNodes()
    throws Exception {
        IMObject object = service.create("classification.staff");
        assertTrue(object != null);
        
        // now attempt to retrieve the value of alias 
        NodeDescriptor ndesc = service.getArchetypeDescriptor(
                object.getArchetypeId()).getNodeDescriptor("alias");
        assertTrue(ndesc != null);
        assertTrue(ndesc.getValue(object) == null);
    }

    /**
     * Test that JXPath can evaulate complex boolean expressions
     */
    public void testBooleanExpressionEvaulation() throws Exception {
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("person.person");
        assertTrue(((Boolean) getValue(
                adesc,
                "nodeDescriptors/firstName/string and nodeDescriptors/lastName/string"))
                .booleanValue());
        assertTrue(((Boolean) getValue(
                adesc,
                "nodeDescriptors/firstName/string and not(nodeDescriptors/lastName/string)"))
                .booleanValue() == false);
        assertTrue(((Boolean) getValue(
                adesc,
                "nodeDescriptors/firstName/string and not(nodeDescriptors/firstName/number)"))
                .booleanValue());
    }

    /**
     * Test that jxpath derive values atually work
     */
    public void testDerivedValueNodes() throws Exception {
        // we know that both name and description are derived nodes
        // for person.person
        Person person = (Person) service.create("person.person");

        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        
        try {
            service.validateObject(person);
        } catch (ValidationException exception) {
            fail("Validation of person failed");
        }
        assertTrue(StringUtils.isEmpty(person.getName()) == false);
        assertTrue(person.getName().equals("Jim Alateras"));
        assertTrue(person.getDescription().equals(
                person.getArchetypeId().getConcept()));
    }

    /**
     * Test that the entityRelationship.animalCarer create default object
     * initializes the date correctly using the jxpath expression
     */
    public void testJXPathDateFunction() throws Exception {
        // create a default animalCarer object
        Date start = new Date();
        EntityRelationship er = (EntityRelationship)service
            .create("entityRelationship.animalCarer");
        assertTrue(er.getActiveStartTime() != null);
        assertTrue(er.getActiveStartTime().getTime() >= start.getTime());
        assertTrue(er.getActiveStartTime().getTime() <= new Date().getTime());
    }
    
    /**
     * Test that we can set an entity identity on a set
     */
    @SuppressWarnings("unchecked")
    public void testSetEntityIdentityOnEntity() throws Exception {
        Person person = (Person) service.create("person.person");
        assertTrue(person != null);
        EntityIdentity eidentity = (EntityIdentity) service
                .create("entityIdentity.personAlias");
        assertTrue(eidentity != null);

        // get the descriptor for the person node
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(person
                .getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertTrue(ndesc != null);
        
        ValueHolder holder = new ValueHolder(eidentity, person);
        OgnlContext context =(OgnlContext)Ognl.createDefaultContext(null);
        
        StringBuffer buf = new StringBuffer("target.add");
        buf.append(StringUtils.capitalize(ndesc.getBaseName()));
        buf.append("(source)");
        Ognl.getValue(buf.toString(), context, holder);
        
        assertTrue(person.getIdentities().size() == 1);
        assertTrue(person.getIdentities().iterator().next().getEntity() != null);
    }

    /**
     * Test that packaged functions work as expects
     */
    public void testPackagedFunctions() 
    throws Exception {
        JXPathContext ctx = JXPathContext.newContext(this);
        assertTrue(ctx.getValue("java.util.Date.new()") instanceof Date);
        assertTrue(ctx.getValue("'jimbo'").equals("jimbo"));
    }
    
    /**
     * Test the JXPath expressions into collections
     */
    public void testJXPathCollectionExpressions()
    throws Exception {
        List<Person> list = new ArrayList<Person>();
        list.add(new Person(new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Mr", "Jim", "Alateras", null));
        list.add(new Person(new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Ms", "Bernadette", "Feeney", null));
        list.add(new Person(new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Ms", "Grace", "Alateras", null));
        
        JXPathContext ctx = JXPathContext.newContext(list);
        // NOTE: Index starts at 1 not 0.
        assertTrue(ctx.getValue(".[1]/firstName").equals("Jim"));
        assertTrue(ctx.getValue(".[2]/lastName").equals("Feeney"));
        assertTrue(ctx.getValue(".[3]/title").equals("Ms"));
    }
    
    /**
     * Test that path to collection works
     */
    public void testPathToCollection() throws Exception {

        // retrieve the node descriptor for animal.pet
        ArchetypeDescriptor adesc = (ArchetypeDescriptor)service
            .create("descriptor.archetype");
        assertTrue(adesc != null);
        ArchetypeDescriptor metaDesc = service.getArchetypeDescriptor(
                adesc.getArchetypeId());
        TestPage page = new TestPage(adesc, metaDesc);
        assertTrue(page != null);
        assertTrue(getValue(page, "pathToCollection(model,  node/nodeDescriptors/nodeDescriptors/path)") 
                instanceof Collection);
    }
    /**
     * Test the JXPath expressions for retrieving an object with a uid 
     * from a collection
     */
    public void testJXPathSearchCollectionForMatchingUid()
    throws Exception {
        List<Person> list = new ArrayList<Person>();
        Person person = new Person(
                new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Mr", "Jim", "Alateras", null);
        person.setUid(1);
        list.add(person);

        person = new Person(
                new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Ms", "Bernadette", "Feeney", null);
        person.setUid(2);
        list.add(person);

        person = new Person(
                new ArchetypeId("openvpms-party-person.person.1.0"), 
                null, "Ms", "Grace", "Alateras", null);
        person.setUid(3);
        list.add(person);
        
        JXPathContext ctx = JXPathContext.newContext(list);
        // NOTE: Using a extension function to do the work.
        assertTrue(ctx.getValue("org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 1)") != null);
        assertTrue(ctx.getValue("org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 3)") != null);
        assertTrue(ctx.getValue("org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 4)") == null);
        assertTrue(ctx.getValue("org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 0)") == null);
        
        // execute the same test using function namespaces
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ClassFunctions(TestFunctions.class, "collfunc"));
        ctx.setFunctions(lib);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 1)") != null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 3)") != null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 4)") == null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 0)") == null);
    }
    
    /**
     * Test the setting and getting of values on collections
     */
    public void testCollectionManipulation() 
    throws Exception {
        ArrayList list = new ArrayList();
        MethodUtils.invokeMethod(list, "add", "object1");
        MethodUtils.invokeMethod(list, "add", "object2");
        MethodUtils.invokeMethod(list, "add", "object3");
        assertTrue(list.size() == 3);
    }
    
    /**
     * Test the set value on a PropertyMap
     */
    public void testSetValueOnPropertyMap()
    throws Exception {
        PropertyMap map = new PropertyMap();
        map.setName("archetypes");
        
        AssertionProperty prop = new AssertionProperty();
        prop.setName("shortName");
        prop.setType("java.lang.String");
        map.addProperty(prop);
        
        JXPathContext context = JXPathContext.newContext(map);
        context.setValue("/properties/shortName/value", "descripor.archetypeRange");
        assertTrue(prop.getValue().equals("descripor.archetypeRange"));
    }
    
    /**
     * This performs a get using an object and a jxpath expression and returns
     * the resolved object
     * 
     * @param source
     *            the source object
     * @param path
     *            the path expression
     * @return Object
     */
    private Object getValue(Object source, String path) {
        /**
         * Object obj = JXPathContext.newContext(source).getValue(path); if (obj
         * instanceof Pointer) { obj = ((Pointer)obj).getValue(); }
         * 
         * return obj;
         */

        return JXPathContext.newContext(source).getValue(path);
    }

    /**
     * This performs a set using an object an jxpath expression and a value
     * object
     * 
     * @param source
     *            the source object
     * @param path
     *            the path expression
     * @param value
     *            the value to set
     */
    private void setValue(Object source, String path, Object value) {
        JXPathContext.newContext(source).setValue(path, value);
    }
}


class ValueHolder {
    private Object target;
    private Object source;
    
    
    /**
     * @param source
     * @param target
     */
    public ValueHolder(Object source, Object target) {
        this.source = source;
        this.target = target;
    }
    
    /**
     * @return Returns the source.
     */
    public Object getSource() {
        return source;
    }
    /**
     * @param source The source to set.
     */
    public void setSource(Object source) {
        this.source = source;
    }
    /**
     * @return Returns the target.
     */
    public Object getTarget() {
        return target;
    }
    /**
     * @param target The target to set.
     */
    public void setTarget(Object target) {
        this.target = target;
    }
    
}
