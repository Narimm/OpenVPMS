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

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
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
import org.openvpms.component.system.common.jxpath.ObjectFunctions;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Test the JXPath expressions on etity objects and descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JXPathTestCase {

    /**
     * Cache a reference to the Archetype service
     */
    private ArchetypeService service;


    /**
     * Test JXPath on node descriptors
     */
    @Test
    public void testPersonNodeDescriptors() {

        // retrieve the node descriptor for animal.pet
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("party.person");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("firstName");
        assertTrue(ndesc != null);
        assertTrue(((Boolean) getValue(adesc, "nodeDescriptors/name/string")));
        assertTrue(((Boolean) getValue(adesc,
                                       " nodeDescriptors/description/string")));
        assertTrue(((Boolean) getValue(adesc,
                                       "nodeDescriptors/id/identifier")));
        assertNull(getValue(adesc, "nodeDescriptors/jimbo"));
    }

    /**
     * Test the path to collection bug ovpms-131
     */
    @Test
    public void testOVPMS131() {
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
    @Test
    public void testNonMandatoryNodes() {
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
    @Test
    public void testBooleanExpressionEvaulation() {
        ArchetypeDescriptor adesc = service
                .getArchetypeDescriptor("party.person");
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
    @Test
    public void testDerivedValueNodes() {
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
    @Test
    public void testJXPathDateFunction() {
        // create a default animalCarer object
        Date start = new Date();
        EntityRelationship er = (EntityRelationship) service
                .create("entityRelationship.animalCarer");
        assertTrue(er.getActiveStartTime() != null);
        assertTrue(er.getActiveStartTime().getTime() >= start.getTime());
        assertTrue(er.getActiveStartTime().getTime() <= new Date().getTime());
    }

    /**
     * Test that packaged functions work as expects
     */
    @Test
    public void testPackagedFunctions() {
        JXPathContext ctx = JXPathHelper.newContext(this);
        assertTrue(ctx.getValue("java.util.Date.new()") instanceof Date);
        assertTrue(ctx.getValue("'jimbo'").equals("jimbo"));
    }

    /**
     * Test the JXPath expressions into collections
     */
    @Test
    public void testJXPathCollectionExpressions() {
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
    @Test
    public void testPathToCollection() {

        // retrieve the node descriptor for animal.pet
        ArchetypeDescriptor adesc = (ArchetypeDescriptor) service.create("descriptor.archetype");
        assertNotNull(adesc);
        ArchetypeDescriptor metaDesc = service.getArchetypeDescriptor(adesc.getArchetypeId());
        TestPage page = new TestPage(adesc, metaDesc);
        assertTrue(getValue(page,
                            "pathToCollection(model,  node/nodeDescriptors/nodeDescriptors/path)")
                instanceof Collection);
    }

    /**
     * Test the JXPath expressions for retrieving an object with an id
     * from a collection
     */
    @Test
    public void testJXPathSearchCollectionForMatchingUid() {
        List<Party> list = new ArrayList<Party>();
        Party person = createPerson("MR", "Jim", "Alateras");
        person.setId(1);
        list.add(person);

        person = createPerson("MS", "Bernadette", "Feeney");
        person.setId(2);
        list.add(person);

        person = createPerson("MS", "Grace", "Alateras");
        person.setId(3);
        list.add(person);

        JXPathContext ctx = JXPathHelper.newContext(list);
        // NOTE: Using a extension function to do the work.
        assertTrue(ctx.getValue(
                "org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 1)") != null);
        assertTrue(ctx.getValue(
                "org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 3)") != null);
        assertTrue(ctx.getValue(
                "org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 4)") == null);
        assertTrue(ctx.getValue(
                "org.openvpms.component.system.service.jxpath.TestFunctions.findObjectWithUid(., 0)") == null);

        // execute the same test using function namespaces
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ClassFunctions(TestFunctions.class, "collfunc"));
        ctx.setFunctions(lib);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 1)") != null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 3)") != null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 4)") == null);
        assertTrue(ctx.getValue("collfunc:findObjectWithUid(., 0)") == null);
        assertTrue(
                ctx.getValue("collfunc:findObjectWithUid(., 1)/name") != null);
    }

    /**
     * Test the setting and getting of values on collections.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCollectionManipulation() throws Exception {
        ArrayList list = new ArrayList();
        MethodUtils.invokeMethod(list, "add", "object1");
        MethodUtils.invokeMethod(list, "add", "object2");
        MethodUtils.invokeMethod(list, "add", "object3");
        assertTrue(list.size() == 3);
    }

    /**
     * Test the set value on a PropertyMap
     */
    @Test
    public void testSetValueOnPropertyMap() {
        PropertyMap map = new PropertyMap();
        map.setName("archetypes");

        AssertionProperty prop = new AssertionProperty();
        prop.setName("shortName");
        prop.setType("java.lang.String");
        map.addProperty(prop);

        JXPathContext context = JXPathHelper.newContext(map);
        context.setValue("/properties/shortName/value",
                         "descripor.archetypeRange");
        assertTrue(prop.getValue().equals("descripor.archetypeRange"));
    }

    /**
     * Test that we can sum correctly over a list of objects
     */
    @Test
    public void testSumOverBigDecimal() {
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ClassFunctions(TestFunctions.class, "ns"));

        List<BigDecimalValues> values = new ArrayList<BigDecimalValues>();
        values.add(
                new BigDecimalValues(new BigDecimal(12), new BigDecimal(12)));
        values.add(
                new BigDecimalValues(new BigDecimal(13), new BigDecimal(13)));
        values.add(
                new BigDecimalValues(new BigDecimal(15), new BigDecimal(15)));

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
    @Test
    public void testSumOverDouble() {
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
        Object sum = ctx.getValue(
                "ns:sum(ns:toBigDecimalValues(values)/child::high)");
        assertTrue(sum.getClass() == BigDecimal.class);
        //sum = ctx.getValue("ns:sum(ns:toBigDecimal(child::low))");
        //assertTrue(sum.getClass() == BigDecimal.class);
    }

    /**
     * Test that the extension functions are called through JXPathHelper
     */
    @Test
    public void testJXPathHelperExtensionFunctions() {
        // prepare the helper
        Properties props = new Properties();
        props.put("tf", TestFunctions.class.getName());
        new JXPathHelper(props);

        Party person = new Party();
        person.setName("Mr Jim Alateras");

        JXPathContext context = JXPathHelper.newContext(person);
        Boolean bool = (Boolean) context.getValue("tf:testName(.)");
        assertTrue(bool);

        person.setName(null);
        bool = (Boolean) context.getValue("tf:testName(.)");
        assertFalse(bool);
    }

    /**
     * Test for bug OVPMS-236
     */
    @Test
    public void testOVPMS236() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "productPrice.margin");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("margin");

        IMObject object = service.create("productPrice.margin");
        ndesc.setValue(object, new BigDecimal("12.35"));
        assertTrue(ndesc.getValue(object) != null);
    }

    /**
     * Test for bug OVPMS-228
     */
    @Test
    public void testOVPMS228() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "productPrice.margin");
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
    @Test
    public void testOVPMS210() {
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
    @Test
    public void testOBF54() {
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
    @Test
    public void testJXPathMaths() {
        JXPathContext ctx = JXPathHelper.newContext(new Object());
        ctx.getValue("2 + 2");
        ctx.getValue("2 - 2");
        ctx.getValue("2 div 2");
        ctx.getValue("2 * 2");
        ctx.getValue("-2");
        ctx.getValue("2 mod 2");
    }

    /**
     * Verifies that {@link ObjectFunctions} can be added to the function
     * library and their instance and static methods invoked.
     */
    @Test
    public void testObjectFunctions() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        JXPathContext ctx = JXPathHelper.newContext(list);
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(new ObjectFunctions(new TestFunctions(), "test"));
        ctx.setFunctions(lib);

        // test instance methods
        assertEquals("a", ctx.getValue("test:getValue(., 0)"));
        assertEquals("c", ctx.getValue("test:getValue(., 2)"));
        assertEquals("foo", ctx.getValue("test:getValue()"));

        // test static methods
        assertEquals("Jimmy", ctx.getValue("test:getContacts()"));
    }

    /*
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        // configure jxpath
        new JXPathHelper();

        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";
        String dir = "org/openvpms/archetype";
        String extension = "adl";

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(dir, new String[]{extension}, assertionFile);
        service = new ArchetypeService(cache);
    }

    /**
     * This performs a get using an object and a jxpath expression and returns
     * the resolved object
     *
     * @param source the source object
     * @param path   the path expression
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
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.setName(title + " " + firstName + " " + lastName);

        return person;
    }
}

