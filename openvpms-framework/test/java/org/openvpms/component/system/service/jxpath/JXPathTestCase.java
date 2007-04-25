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

import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.component.system.common.test.BaseTestCase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Test the JXPath expressions on etity objects and descriptors.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class JXPathTestCase extends BaseTestCase {

    /**
     * A reference to the JXPathHelper. Configures jxpath. 
     */
    @SuppressWarnings("unused")
    private JXPathHelper context = new JXPathHelper();

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
     * @param name
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
        assertTrue(((Boolean) getValue(adesc, "nodeDescriptors/name/string")));
        assertTrue(((Boolean) getValue(adesc," nodeDescriptors/description/string")));
        assertTrue(((Boolean) getValue(adesc,
                "nodeDescriptors/uid/identifier")));
        assertNull(getValue(adesc, "nodeDescriptors/jimbo"));
    }

    /**
     * Test the path to collection bug ovpms-131
     */
    public void testOVPMS131()
    throws Exception {
        Party person = createPerson("MR", "jima", "alateras");
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
        IMObject object = service.create("lookup.staff");
        assertNotNull(object);
        
        // now attempt to retrieve the value of alias 
        NodeDescriptor ndesc = service.getArchetypeDescriptor(
                object.getArchetypeId()).getNodeDescriptor("alias");
        assertNotNull(ndesc);
        assertNull(ndesc.getValue(object));
    }

    /**
     * Test that JXPath can evaulate complex boolean expressions
     */
    public void testBooleanExpressionEvaulation() throws Exception {
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("person.person");
        assertTrue(((Boolean) getValue(
                adesc,
                "nodeDescriptors/name/string and nodeDescriptors/description/string")));
        assertFalse(((Boolean) getValue(
                adesc,
                "nodeDescriptors/name/string and not(nodeDescriptors/description/string)")));
        assertTrue(((Boolean) getValue(
                adesc,
                "nodeDescriptors/name/string and not(nodeDescriptors/description/number)")));
    }

    /**
     * Test that jxpath derive values atually work
     */
    public void testDerivedValueNodes() throws Exception {
        // we know that both name and description are derived nodes
        // for person.person
        Party person = createPerson("MR", "Jim", "Alateras");
        
        try {
            service.validateObject(person);
        } catch (ValidationException exception) {
            fail("Validation of person failed");
        }
        assertFalse(StringUtils.isEmpty(person.getName()));
        assertTrue(person.getName().equals("Alateras,Jim"));
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
        Party person = (Party) service.create("person.person");
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
        JXPathContext ctx = JXPathHelper.newContext(this);
        assertTrue(ctx.getValue("java.util.Date.new()") instanceof Date);
        assertTrue(ctx.getValue("'jimbo'").equals("jimbo"));
    }
    
    /**
     * Test the JXPath expressions into collections
     */
    public void testJXPathCollectionExpressions()
    throws Exception {
        List<Party> list = new ArrayList<Party>();
        list.add(createPerson("MR", "Jim", "Alateras"));
        list.add(createPerson("MS", "Bernadette", "Feeney"));
        list.add(createPerson("MS", "Grace", "Alateras"));
        
        JXPathContext ctx = JXPathHelper.newContext(list);
        // NOTE: Index starts at 1 not 0.
        assertTrue(ctx.getValue(".[1]/details/firstName").equals("Jim"));
        assertTrue(ctx.getValue(".[2]/details/lastName").equals("Feeney"));
        assertTrue(ctx.getValue(".[3]/details/title").equals("MS"));
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
        assertTrue(getValue(page, "pathToCollection(model,  node/nodeDescriptors/nodeDescriptors/path)")
                instanceof Collection);
    }
    /**
     * Test the JXPath expressions for retrieving an object with a uid 
     * from a collection
     */
    public void testJXPathSearchCollectionForMatchingUid()
    throws Exception {
        List<Party> list = new ArrayList<Party>();
        Party person = createPerson("MR", "Jim", "Alateras");
        person.setUid(1);
        list.add(person);

        person = createPerson("MS", "Bernadette", "Feeney");
        person.setUid(2);
        list.add(person);

        person = createPerson("MS", "Grace", "Alateras");
        person.setUid(3);
        list.add(person);
        
        JXPathContext ctx = JXPathHelper.newContext(list);
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
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 1)/name") != null);
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
        
        JXPathContext context = JXPathHelper.newContext(map);
        context.setValue("/properties/shortName/value", "descripor.archetypeRange");
        assertTrue(prop.getValue().equals("descripor.archetypeRange"));
    }
    
    /**
     * Test that we can sum correctly over a list of objects
     */
    public void testSumOverBigDecimal()
    throws Exception {
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ClassFunctions(TestFunctions.class, "ns"));
        
        List<BigDecimalValues> values = new ArrayList<BigDecimalValues>();
        values.add(new BigDecimalValues(new BigDecimal(12), new BigDecimal(12)));
        values.add(new BigDecimalValues(new BigDecimal(13), new BigDecimal(13)));
        values.add(new BigDecimalValues(new BigDecimal(15), new BigDecimal(15)));
        
        JXPathContext ctx = JXPathHelper.newContext(values);
        ctx.setFunctions(lib);
        
        Object sum = ctx.getValue("ns:sum(child::high)");
        assertTrue(sum.getClass() == BigDecimal.class);
        sum = ctx.getValue("ns:sum(child::low)");
        assertTrue(sum.getClass() == BigDecimal.class);
    }
    
    /**
     * Test that we can still sum using a conversion function in the 
     * expression
     */
    public void testSumOverDouble()
    throws Exception {
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ClassFunctions(TestFunctions.class, "ns"));
        
        List<DoubleValues> values = new ArrayList<DoubleValues>();
        values.add(new DoubleValues(12, 12));
        values.add(new DoubleValues(13, 13));
        values.add(new DoubleValues(15, 15));
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("values", values);
        JXPathContext ctx = JXPathHelper.newContext(map);
        ctx.setFunctions(lib);
        
        //Object sum = ctx.getValue("sum(values/child::high[self::high < 0])");
        Object sum = ctx.getValue("ns:sum(ns:toBigDecimalValues(values)/child::high)");
        assertTrue(sum.getClass() == BigDecimal.class);
        //sum = ctx.getValue("ns:sum(ns:toBigDecimal(child::low))");
        //assertTrue(sum.getClass() == BigDecimal.class);
    }
     
    /**
     * Test that the extension functions are called through JXPathHelper
     */
    public void testJXPathHelperExtensionFunctions()
    throws Exception {
        // prepare the helper
        Properties props = new Properties();
        props.put("tf", TestFunctions.class.getName());
        new JXPathHelper(props);
        
        Party person = new Party();
        person.setName("Mr Jim Alateras");
        
        JXPathContext context = JXPathHelper.newContext(person);
        Boolean bool = (Boolean)context.getValue("tf:testName(.)");
        assertTrue(bool);
        
        person.setName(null);
        bool = (Boolean)context.getValue("tf:testName(.)");
        assertFalse(bool);
    }
    
    /**
     * Test for bug OVPMS-236
     */
    public void testOVPMS236()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("productPrice.margin");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("margin");
        
        IMObject object = service.create("productPrice.margin");
        ndesc.setValue(object, new BigDecimal("12.35"));
        assertTrue(ndesc.getValue(object) != null);
    }
    
    /**
     * Test for bug OVPMS-228
     */
    public void testOVPMS228()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("productPrice.margin");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("margin");
        NodeDescriptor ondesc = adesc.getNodeDescriptor("otherMargin");
        
        IMObject object = service.create("productPrice.margin");
        assertTrue(ndesc.getValue(object).getClass().getName(),
                ndesc.getValue(object).getClass() == ndesc.getClazz());
        assertTrue(ondesc.getValue(object).getClass().getName(),
                ondesc.getValue(object).getClass() == ondesc.getClazz());
    }
    
    /**
     * Test for bug OVPMD-210
     */
    public void testOVPMS210()
    throws Exception {
        BigDecimalValues values = new BigDecimalValues(new BigDecimal(100),
                new BigDecimal(200));
        JXPathContext ctx = JXPathHelper.newContext(values);
        Object obj = ctx.getValue("/low + /high");
        assertTrue(obj instanceof BigDecimal);
        assertTrue(obj.equals(new BigDecimal(300)));
    }
    
    /**
     * Test for bug OBf-54
     */
    public void testOBF54()
    throws Exception {
        OpenVPMSTypeConverter converter = new OpenVPMSTypeConverter();
        Object obj;
        
        obj = converter.convert("10.55", Money.class);
        assertTrue(obj.getClass().getName(), obj instanceof Money);
        
        obj = converter.convert(new BigDecimal(10.55), Money.class);
        assertTrue(obj.getClass().getName(), obj instanceof Money);
        
        obj = converter.convert(new Double(10.55), Money.class);
        assertTrue(obj.getClass().getName(), obj instanceof Money);
    }
    
    /**
     * Test normal maths operations
     */
    public void testJXPathMaths()
    throws Exception {
        JXPathContext ctx = JXPathHelper.newContext(new Object());
        ctx.getValue("2 + 2");
        ctx.getValue("2 - 2");
        ctx.getValue("2 div 2");
        ctx.getValue("2 * 2");
        ctx.getValue("-2");
        ctx.getValue("2 mod 2");
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
         * Object obj = JXPathHelper.newContext(source).getValue(path); if (obj
         * instanceof Pointer) { obj = ((Pointer)obj).getValue(); }
         * 
         * return obj;
         */

        return JXPathHelper.newContext(source).getValue(path);
    }

    /**
     * Create a person
     * 
     * @param title
     *            the person's title
     * @param firstName
     *            the person's first name
     * @param lastName
     *            the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.setName(title + " " + firstName + " " + lastName);
        
        return person;
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
