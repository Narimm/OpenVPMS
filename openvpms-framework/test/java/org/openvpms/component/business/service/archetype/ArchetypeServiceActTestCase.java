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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectDAOHibernate;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import static org.openvpms.component.business.service.archetype.ArchetypeServiceException.ErrorCode.FailedToDeleteObject;
import static org.openvpms.component.business.service.archetype.ArchetypeServiceException.ErrorCode.FailedToSaveCollectionOfObjects;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Test that ability to create and query on acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceActTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The transaction template.
     */
    private TransactionTemplate template;


    /**
     * Test the creation of a simple act.
     */
    public void testSimpleActCreation() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        service.save(person);
        Act act = createSimpleAct("study", "inprogress");
        Participation participation = createSimpleParticipation(
                "studyParticipation",
                person, act);
        act.addParticipation(participation);
        service.save(act);

        Act act1 = (Act) ArchetypeQueryHelper.getByUid(
                service, act.getArchetypeId(), act.getUid());
        assertEquals(act1, act);
    }

    /**
     * Test the search by acts function
     */
    @SuppressWarnings("unchecked")
    public void testGetActs() throws Exception {
        // create an act which participates in 5 acts
        Party person = createPerson("MR", "Jim", "Alateras");
        for (int index = 0; index < 5; index++) {
            Act act = createSimpleAct("study" + index, "inprogress");
            Participation participation = createSimpleParticipation(
                    "studyParticipation",
                    person, act);
            act.addParticipation(participation);
            service.save(act);
        }

        service.save(person);

        // now use the getActs request
        IPage<Act> acts = ArchetypeQueryHelper.getActs(
                service, person.getObjectReference(),
                "participation.simple", "act", "simple",
                null, null, null, null,
                null, false, 0, ArchetypeQuery.ALL_RESULTS);
        assertEquals(5, acts.getTotalResults());

        // now look at the paging aspects
        acts = ArchetypeQueryHelper.getActs(service,
                                            person.getObjectReference(),
                                            "participation.simple", "act",
                                            "simple", null, null, null, null,
                                            null, false, 0, 1);
        assertEquals(5, acts.getTotalResults());
        assertEquals(1, acts.getResults().size());
        assertFalse(StringUtils.isEmpty(acts.getResults().get(0).getName()));
    }

    /**
     * Retrieve acts using a start and end date.
     */
    public void testGetActsBetweenTimes() throws Exception {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 2 * 60 * 60 * 1000);
        ArchetypeQuery query = new ArchetypeQuery("act.simple", false, true)
                .add(new NodeConstraint("startTime", RelationalOp.BTW,
                                        startTime, endTime))
                .add(new NodeConstraint("name", "between"));
        int acount = service.get(query).getResults().size();
        service.save(createSimpleAct("between", "start"));
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);

        for (int index = 0; index < 5; index++) {
            service.save(createSimpleAct("between", "start"));
        }
        acount1 = service.get(query).getResults().size();
        assertEquals(acount + 6, acount1);
    }

    /**
     * Tests OVPMS-211.
     */
    public void testOVPMS211() throws Exception {
        Act estimationItem1
                = (Act) service.create("act.customerEstimationItem");
        ActBean estimationItem1Bean = new ActBean(estimationItem1);
        estimationItem1Bean.setValue("fixedPrice", "1.0");
        estimationItem1Bean.setValue("lowQty", "2.0");
        estimationItem1Bean.setValue("lowUnitPrice", "3.0");
        estimationItem1Bean.setValue("highQty", "4.0");
        estimationItem1Bean.setValue("highUnitPrice", "5.0");
        estimationItem1Bean.save();

        Act estimation = (Act) service.create("act.customerEstimation");
        ActBean estimationBean = new ActBean(estimation);
        estimationBean.setValue("status", "IN_PROGRESS");
        estimationBean.addRelationship("actRelationship.customerEstimationItem",
                                       estimationItem1);

        Act estimationItem2
                = (Act) service.create("act.customerEstimationItem");
        ActBean estimationItem2Bean = new ActBean(estimationItem2);
        estimationItem2Bean.setValue("fixedPrice", "2.0");
        estimationItem2Bean.setValue("lowQty", "3.0");
        estimationItem2Bean.setValue("lowUnitPrice", "4.0");
        estimationItem2Bean.setValue("highQty", "5.0");
        estimationItem2Bean.setValue("highUnitPrice", "6.0");
        estimationItem2Bean.save();

        estimationBean.addRelationship("actRelationship.customerEstimationItem",
                                       estimationItem2);
        estimationBean.save();

        // reload the estimation
        estimation = reload(estimation);
        estimationBean = new ActBean(estimation);

        // verify low & high totals have been calculated
        BigDecimal lowTotal = estimationBean.getBigDecimal("lowTotal");
        BigDecimal highTotal = estimationBean.getBigDecimal("highTotal");
        assertTrue(lowTotal.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(highTotal.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Tests OVPMS-228.
     */
    public void testOVPMS228() throws Exception {
        Act act = (Act) service.create("act.customerAccountPayment");
        assertNotNull(act);
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                act.getArchetypeId());
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("amount");
        assertNotNull(ndesc);
        ndesc.getValue(act);
        assertTrue(ndesc.getValue(act).getClass().getName(),
                   ndesc.getValue(act) instanceof BigDecimal);
    }

    /**
     * Saves a collection of acts.
     *
     * @throws Exception for any error
     */
    public void testSaveCollection() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        List<IMObject> acts = Arrays.asList((IMObject) act1, act2, act3);
        checkSaveCollection(acts, 0);

        // verify the acts can be re-saved
        act1.setStatus("POSTED");
        act2.setStatus("POSTED");
        act3.setStatus("POSTED");
        checkSaveCollection(acts, 1);

        // now change the first act, and attempt to re-save the collection.
        // This should fail as the collection doesn't have the latest version
        // of act1
        act1 = reload(act1);
        act1.setStatus("COMPLETED");
        service.save(act1);
        try {
            checkSaveCollection(acts, 2);
            fail("Expected save to fail");
        } catch (ArchetypeServiceException expected) {
            assertEquals(FailedToSaveCollectionOfObjects,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that the {@link IArchetypeService#save(Collection<IMObject>)}
     * method can be used to save 2 or more acts that reference the same
     * ActRelationship.
     *
     * @throws Exception for any error
     */
    public void testOBF163() throws Exception {
        Act estimation = (Act) service.create("act.customerEstimation");
        estimation.setStatus("POSTED");
        ActRelationship relationship = (ActRelationship) service.create(
                "actRelationship.customerEstimationItem");
        Act item = (Act) service.create("act.customerEstimationItem");
        relationship.setSource(estimation.getObjectReference());
        relationship.setTarget(item.getObjectReference());
        estimation.addActRelationship(relationship);
        item.addActRelationship(relationship);

        List<IMObject> acts = Arrays.asList((IMObject) estimation, item);
        checkSaveCollection(acts, 0);

        // reload the estimation and item. Each will have a separate copy of
        // the same persistent act relationship
        estimation = reload(estimation);
        item = reload(item);
        assertNotNull(estimation);
        assertNotNull(item);

        acts = Arrays.asList((IMObject) estimation, item);

        // save the collection, and verify they have saved by checking the
        // versions.
        checkSaveCollection(acts, 1);

        // now remove the relationship, and add a new one
        estimation.removeActRelationship(relationship);
        item.removeActRelationship(relationship);

        ActRelationship relationship2 = (ActRelationship) service.create(
                "actRelationship.customerEstimationItem");
        relationship2.setSource(estimation.getObjectReference());
        relationship2.setTarget(item.getObjectReference());
        estimation.addActRelationship(relationship2);
        item.addActRelationship(relationship2);

        checkSaveCollection(acts, 2);
    }

    /**
     * Verifies that the {@link IArchetypeService#save(Collection<IMObject>)}
     * method and {@link IArchetypeService#save(IMObject) method can be used
     * to save the same object.
     *
     * @throws Exception for any error
     */
    public void testOBF170() {
        Party person = createPerson("MR", "Jim", "Alateras");
        service.save(person);

        Act act1 = createSimpleAct("act1", "IN_PROGRESS");

        Participation p1 = createSimpleParticipation("act1p1", person, act1);
        act1.addParticipation(p1);

        service.save(act1);
        act1.setStatus("POSTED");
        Collection<IMObject> objects = Arrays.asList((IMObject) act1);
        service.save(objects);

        act1.removeParticipation(p1);
        objects = Arrays.asList((IMObject) act1);
        service.save(objects);

        service.save(act1);
    }

    /**
     * Verifies an act can be removed.
     *
     * @throws Exception for any error
     */
    public void testSingleActRemove() throws Exception {
        Act act = createSimpleAct("act", "IN_PROGRESS");
        service.save(act);
        assertEquals(act, reload(act));

        service.remove(act);
        assertNull(reload(act));
    }

    /**
     * Creates a set of acts with non-parent/child relationships, and verifies
     * that deleting one act doesn't cascade to the rest.
     *
     * @throws Exception for any error
     */
    public void testPeerActRemoval() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        // create a relationship from act1 -> act2
        addRelationship(act1, act2, "act1->act2");

        // create a relationship from act2 -> act3
        addRelationship(act2, act3, "act2->act3");

        service.save(act1);
        service.save(act2);
        service.save(act3);

        service.remove(act1);
        assertNull(reload(act1));
        assertNotNull(reload(act2));
        assertNotNull(reload(act3));

        service.remove(act3);
        assertNull(reload(act3));
        assertNotNull(reload(act2));
    }

    /**
     * Creates a parent/child act hierarchy, and verifies that:
     * <ul>
     * <li>deleting the children doesn't affect the remaining children or
     * parent; and</li>
     * <li>deleting the parent causes deletion of the children</li>
     * </ul>
     */
    public void testParentChildRemoval() {
        Act estimation = (Act) service.create("act.customerEstimation");
        service.remove(estimation);
        estimation.setStatus("IN_PROGRESS");
        Act item1 = (Act) service.create("act.customerEstimationItem");
        Act item2 = (Act) service.create("act.customerEstimationItem");
        Act item3 = (Act) service.create("act.customerEstimationItem");
        ActBean bean = new ActBean(estimation);
        bean.addRelationship("actRelationship.customerEstimationItem", item1);
        bean.addRelationship("actRelationship.customerEstimationItem", item2);
        bean.addRelationship("actRelationship.customerEstimationItem", item3);
        service.save(item1);
        service.save(item2);
        service.save(item3);
        bean.save();

        // remove an item, and verify it has been removed and that the other
        // acts aren't removed
        service.remove(item1);
        assertNull(reload(item1));
        assertNotNull(reload(estimation));
        assertNotNull(reload(item2));
        assertNotNull(reload(item3));

        // now remove the estimation and verify the remaining items are removed
        estimation = reload(estimation);
        assertNotNull(estimation);
        service.remove(estimation);
        assertNull(reload(estimation));
        assertNull(reload(item2));
        assertNull(reload(item3));
    }

    /**
     * Verifies that a set of acts in a cyclic parent/child relationship can
     * be removed.
     *
     * @throws Exception for any error
     */
    public void testCyclicParentChildRemoval() throws Exception {
        // create 3 acts, with the following relationships:
        // act1 -> act2 -> act3 -> act1
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        addRelationship(act1, act2, "act1->act2", true);
        addRelationship(act2, act3, "act2->act3", true);
        addRelationship(act3, act1, "act3->act1", true);

        // remove act2. The removal should cascade to include act3 and act1
        service.remove(act2);
        assertNull(reload(act1));
        assertNull(reload(act2));
        assertNull(reload(act3));
    }

    /**
     * Verifies that acts with peer and parent/child relationships are handled
     * correctly at deletion, i.e the deletion cascades to those target
     * acts in parent/child relationships, and not those in peer relationships.
     *
     * @throws Exception for any error
     */
    public void testPeerParentChildRemoval() throws Exception {
        // create 3 acts with the following relationships:
        // act1 -- (parent/child) --> act2
        //   |-------- (peer) ------> act3

        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act2", "IN_PROGRESS");

        addRelationship(act1, act2, "act1->act2", true);
        addRelationship(act1, act3, "act1->act3", false);

        service.save(act1);
        service.save(act2);
        service.save(act3);

        // remove act1, and verify that it and act2 are removed, and act3
        // remains.
        service.remove(act1);

        assertNull(reload(act1));
        assertNull(reload(act2));
        assertNotNull(reload(act3));
    }

    /**
     * Verifies that removal of acts with a parent/child relationship fails
     * when the parent act has changed subsequent to the version being deleted.
     *
     * @throws Exception for any error
     */
    public void testStaleParentChildRemoval() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        addRelationship(act1, act2, "act1->act2", true);
        service.save(act1);
        service.save(act2);
        service.save(act3);

        Act stale = reload(act1);

        addRelationship(act1, act3, "act1->act3", true);
        service.save(act1);
        service.save(act3);

        try {
            service.remove(stale);
            fail("Expected removal to fail");
        } catch (ArchetypeServiceException expected) {
            assertEquals(FailedToDeleteObject, expected.getErrorCode());
            IMObjectDAOException cause
                    = (IMObjectDAOException) expected.getCause();

            // verify the cause comes from the DAO collection deletion method
            assertEquals(IMObjectDAOException.ErrorCode.FailedToDeleteIMObject,
                         cause.getErrorCode());
        }
    }

    /**
     * Creates a set of acts with non-parent/child relationships, and verifies
     * that deleting one act doesn't cascade to the rest, within transactions.
     *
     * @throws Exception for any error
     */
    public void testPeerActRemovalInTxn() throws Exception {
        final Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        final Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        final Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        // create a relationship from act1 -> act2
        final ActRelationship relAct1Act2 = addRelationship(act1, act2,
                                                            "act1->act2");

        // create a relationship from act2 -> act3
        final ActRelationship relAct2Act3 = addRelationship(act2, act3,
                                                            "act2->act3");

        // create a relationship from act1 -> act3
        final ActRelationship relAct1Act3 = addRelationship(act1, act3,
                                                            "act1->act3");

        service.save(act1);
        service.save(act2);
        service.save(act3);

        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                service.remove(act1);
                assertNotNull(reload(act1));  // can reload till commit

                // reload act2 and verify that it no longer has a relationship
                // to act1, and can be saved again
                Act act2reloaded = reload(act2);
                Set<ActRelationship> relationships
                        = act2reloaded.getActRelationships();
                assertFalse(relationships.contains(relAct1Act2));
                assertTrue(relationships.contains(relAct2Act3));
                act2reloaded.setStatus("POSTED");
                service.save(act2reloaded);

                // reload act3 and verify that it no longer has a relationship
                // to act1, and can be saved again
                Act act3reloaded = reload(act2);
                relationships = act3reloaded.getActRelationships();
                assertFalse(relationships.contains(relAct1Act3));
                act3reloaded.setStatus("POSTED");
                service.save(act3reloaded);
                return null;
            }
        });
        assertNull(reload(act1));
        assertNotNull(reload(act2));
        assertNotNull(reload(act3));

        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                Act act3reloaded = reload(act3);
                service.remove(act3reloaded);
                assertNotNull(reload(act3reloaded));  // can reload till commit

                // reload act2 and verify that it no longer has a relationship
                // to act3
                Act act2reloaded = reload(act2);
                assertFalse(act2reloaded.getActRelationships().contains(
                        relAct2Act3));
                assertEquals("POSTED", act2reloaded.getStatus());
                return null;
            }
        });

        assertNull(reload(act3));
        assertNotNull(reload(act2));
    }

    /**
     * Creates two acts, act1 and act2 with a relationship between them.
     * In a transaction, deletes act2 and associates act1 with act3.
     */
    public void testActReplacementInTxn() {
        final Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        final Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        final Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        // create a relationship from act1 -> act2
        final ActRelationship relAct1Act2 = addRelationship(act1, act2,
                                                            "act1->act2");

        service.save(act1);
        service.save(act2);
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                service.remove(act2);
                Act reloaded = reload(act1);

                // relationship should be removed
                assertFalse(reloaded.getActRelationships().contains(
                        relAct1Act2));

                // add a new relationship
                addRelationship(reloaded, act3, "act1->act3");
                service.save(reloaded);
                service.save(act3);
                return null;
            }
        });

        Act reloaded = reload(act1);
        ActBean relBean = new ActBean(reloaded);
        assertTrue(relBean.getActs().contains(act3));
    }

    /**
     * Verifies that acts with peer and parent/child relationships are handled
     * correctly at deletion in a transaction, i.e the deletion cascades to
     * those target acts in parent/child relationships, and not those in peer
     * relationships.
     */
    public void testPeerParentChildRemovalInTxn() {
        // Create 4 acts with the following relationships:
        // act1 -- (parent/child) --> act2 -- (peer) --> act 4
        //   |---- (parent/child) --> act3 -- (peer) -----|
        final Act act1 = createSimpleAct("act1", "IN_POGRESS");
        final Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        final Act act3 = createSimpleAct("act3", "IN_PROGRESS");
        final Act act4 = createSimpleAct("act4", "IN_PROGRESS");

        addRelationship(act1, act2, "act1->act2", true);
        addRelationship(act1, act3, "act1->act3", true);
        final ActRelationship relAct2Act4
                = addRelationship(act2, act4, "act2->act4", false);
        final ActRelationship relAct3Act4
                = addRelationship(act3, act4, "act3->act4", false);
        service.save(act1);
        service.save(act2);
        service.save(act3);
        service.save(act4);

        assertTrue(act4.getActRelationships().contains(relAct2Act4));
        assertTrue(act4.getActRelationships().contains(relAct3Act4));

        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                service.remove(act1);

                // reload act4 and verify it no longer has any relationships
                Act reloaded = reload(act4);
                assertTrue(reloaded.getActRelationships().isEmpty());

                // verify it can be re-saved
                reloaded.setName("A test");
                service.save(reloaded);
                return null;
            }
        });

        assertNull(reload(act1)); // deletion of act1 should have cascaded to
        assertNull(reload(act2)); // act2 and act3
        assertNull(reload(act3));

        Act reloaded = reload(act4);
        assertTrue(reloaded.getActRelationships().isEmpty());
    }

    /**
     * Verifies that new objects can be subsequently saved if a rollback
     * occurs, for OBF-186.
     */
    public void testResetNewIdsOnRollback() {
        // Create 2 acts with the following relationships:
        // act1 -- (parent/child) --> act2
        final Act act1 = createSimpleAct("act1", "IN_POGRESS");
        final Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        final ActRelationship rel
                = addRelationship(act1, act2, "act1->act2", true);

        // initial ids should be unset
        assertEquals(-1, act1.getUid());
        assertEquals(-1, act2.getUid());
        assertEquals(-1, rel.getUid());

        // save objects in a transaction, and rollback. Within the transaction,
        // the objects should be assigned identifiers. After rollback, they
        // should be reset to -1.
        try {
            template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status) {
                    service.save(act1);
                    service.save(act2);

                    // objects should have ids assigned now
                    assertFalse(-1 == act1.getUid());
                    assertFalse(-1 == act2.getUid());
                    assertFalse(-1 == rel.getUid());
                    throw new RuntimeException("Trigger rollback");
                }
            });
            fail("Expected transaction to fail");
        } catch (Exception expected) {
            // expected behaviour
        }

        // id changes should be reverted on rollback
        assertEquals(-1, act1.getUid());
        assertEquals(-1, act2.getUid());
        assertEquals(-1, rel.getUid());

        // now verify the objects can be saved
        try {
            template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status) {
                    service.save(act1);
                    service.save(act2);
                    return null;
                }
            });
        } catch (Exception error) {
            fail("Expected transaction to succeed");
        }

        // objects should have ids assigned now
        assertFalse(-1 == act1.getUid());
        assertFalse(-1 == act2.getUid());
        assertFalse(-1 == rel.getUid());

        // now verfiy that a subsequent rollback of persistent objects
        // doesn't reset the ids
        try {
            template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status) {
                    service.save(act1);
                    service.save(act2);
                    throw new RuntimeException("Trigger rollback");
                }
            });
            fail("Expected transaction to fail");
        } catch (Exception expected) {
            // expected behaviour
        }

        // objects should have ids assigned still
        assertFalse(-1 == act1.getUid());
        assertFalse(-1 == act2.getUid());
        assertFalse(-1 == rel.getUid());
    }

    /**
     * Tests the fix for OBF-190.
     */
    public void testOBF190() {
        // Create 2 acts with the following relationship:
        // act1 -- (parent/child) --> act2
        final Act act1 = createSimpleAct("act1", "IN_POGRESS");
        final Act act2 = createSimpleAct("act2", "IN_PROGRESS");

        String name = "act1->act2";
        final ActRelationship rel = addRelationship(act1, act2, name, true);
        service.save(act2);

        final String newName = "act1->act2 changed";

        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                // reload act2 and verify it contains the relationship
                Act reloaded = reload(act2);
                assertTrue(reloaded.getTargetActRelationships().contains(rel));

                // update the relationship and save act1
                rel.setName(newName);
                service.save(act1);
                return null;
            }
        });

        // reload act1 and verify it contains the relationship
        Act reloaded = reload(act1);
        assertNotNull(reloaded);
        Set<ActRelationship> relationships
                = reloaded.getSourceActRelationships();
        assertEquals(1, relationships.size());
        ActRelationship reloadedRel
                = relationships.toArray(new ActRelationship[0])[0];

        // verify the relationship was updated
        assertEquals(reloadedRel, rel);
        assertEquals(reloadedRel.getVersion(), rel.getVersion());
        assertEquals(newName, reloadedRel.getName());

        // verify the acts can be saved again
        service.save(act1);
        service.save(act2);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
    */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
        PlatformTransactionManager txnManager
                = (PlatformTransactionManager) applicationContext.getBean(
                "txnManager");
        template = new TransactionTemplate(txnManager);
    }

    /**
     * Saves a collection via the {@link IMObjectDAOHibernate#save(Collection)}
     * method and verifies they have saved with the correct version.
     *
     * @param objects the objects to save
     * @param version the expected version
     */
    private void checkSaveCollection(List<IMObject> objects, long version) {
        service.save(objects);
        for (IMObject object : objects) {
            assertEquals(version, object.getVersion());
            IMObject reloaded = reload(object);
            assertEquals(object, reloaded);
            assertEquals(version, reloaded.getVersion());
        }
    }

    /**
     * Helper to reload an object.
     *
     * @param object the object to reload
     * @return the reloaded object, or <tt>null</tt> if it can't be found
     * @throws ArchetypeServiceException for any error
     */
    @SuppressWarnings("unchecked")
    private <T extends IMObject> T reload(T object) {
        return (T) ArchetypeQueryHelper.getByObjectReference(
                service, object.getObjectReference());
    }

    /**
     * Create a simple act
     *
     * @param name   the name of the act
     * @param status the status of the act
     * @return Act
     */
    private Act createSimpleAct(String name, String status) {
        Act act = (Act) service.create("act.simple");

        act.setName(name);
        act.setStatus(status);
        act.setActivityStartTime(new Date());
        act.setActivityEndTime(
                new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000));

        return act;
    }

    /**
     * Create a simple participation
     *
     * @param name   the name of the participation
     * @param entity the entity in the participation
     * @param act    the act in the participation
     */
    private Participation createSimpleParticipation(String name, Entity entity,
                                                    Act act) {
        Participation participation = (Participation) service.create(
                "participation.simple");
        participation.setName(name);
        participation.setEntity(entity.getObjectReference());
        participation.setAct(act.getObjectReference());

        return participation;
    }

    /**
     * Create a person with the specified title, firstName and LastName
     *
     * @param title
     * @param firstName
     * @param lastName
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Helper to add a peer <em>actRelationship.simple</em> relationship
     * between two acts.
     *
     * @param source the source act
     * @param target that target act
     * @param name   the relationship name
     * @return the relationship
     */
    private ActRelationship addRelationship(Act source, Act target,
                                            String name) {
        return addRelationship(source, target, name, false);
    }

    /**
     * Helper to add an <em>actRelationship.simple</em> relationship between two
     * acts.
     *
     * @param source      the source act
     * @param target      that target act
     * @param name        the relationship name
     * @param parentChild if <tt>true</tt> add a parent-child relationship,
     *                    otherwise add a peer relationship
     * @return the relationship
     */
    private ActRelationship addRelationship(Act source, Act target, String name,
                                            boolean parentChild) {
        ActBean bean = new ActBean(source);
        ActRelationship result = bean.addRelationship("actRelationship.simple",
                                                      target);
        result.setName(name);
        result.setParentChildRelationship(parentChild);
        return result;
    }
}
