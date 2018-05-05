/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.practice;

import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Rules for <em>party.organisationLocation</em> instances.
 *
 * @author Tim Anderson
 */
public class LocationRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@link LocationRules}.
     *
     * @param service the archetype service
     */
    public LocationRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the practice associated with a location.
     *
     * @param location the location
     * @return the practice associated with the location, or {@code null} if none is found
     */
    public Party getPractice(Party location) {
        return (Party) getBean(location).getNodeSourceEntity("practice");
    }

    /**
     * Returns the default deposit account associated with a location.
     *
     * @param location the location
     * @return the default deposit account or {@code null} if none is found
     */
    public Party getDefaultDepositAccount(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(location, "depositAccounts", false, service);
    }

    /**
     * Returns the default till associated with a location.
     *
     * @param location the location
     * @return the default till or {@code null} if none is found
     */
    public Party getDefaultTill(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(location, "tills", false, service);
    }

    /**
     * Returns the default schedule view associated with a location.
     *
     * @param location the location
     * @return the default schedule or {@code null} if none is found
     */
    public Entity getDefaultScheduleView(Party location) {
        return EntityRelationshipHelper.getDefaultTarget(location, "scheduleViews", false, service);
    }

    /**
     * Returns the <em>entity.organisationScheduleView</em>s associated with a location.
     *
     * @param location the location
     * @return the schedules views
     */
    public List<Entity> getScheduleViews(Party location) {
        return getBean(location).getNodeTargetEntities("scheduleViews");
    }

    /**
     * Determines if a location has a schedule view.
     *
     * @param location the practice location
     * @param view     the view reference
     * @return {@code true} if the location has the view, otherwise  {@code false}
     */
    public boolean hasScheduleView(Party location, IMObjectReference view) {
        return getBean(location).getNodeTargetObjectRefs("scheduleViews").contains(view);
    }

    /**
     * Returns the default work list view associated with a location.
     *
     * @param location the location
     * @return the default work list view or {@code null} if none is found
     */
    public Entity getDefaultWorkListView(Party location) {
        return EntityRelationshipHelper.getDefaultTarget(
                location, "workListViews", false, service);
    }

    /**
     * Returns the <em>entity.organisationWorkListView</em>s associated with a location.
     *
     * @param location the location
     * @return the work list views
     */
    public List<Entity> getWorkListViews(Party location) {
        EntityBean bean = getBean(location);
        return bean.getNodeTargetEntities("workListViews");
    }

    /**
     * Determines if a location has a work list view.
     *
     * @param location the practice location
     * @param view     the view reference
     * @return {@code true} if the location has the view, otherwise  {@code false}
     */
    public boolean hasWorkListView(Party location, IMObjectReference view) {
        return getBean(location).getNodeTargetObjectRefs("workListViews").contains(view);
    }

    /**
     * Returns the default stock location reference associated with a location.
     *
     * @param location the location
     * @return the default stock location reference, or {@code null} if none is found
     */
    public IMObjectReference getDefaultStockLocationRef(Party location) {
        return EntityRelationshipHelper.getDefaultTargetRef(location, "stockLocations", true, service);
    }

    /**
     * Returns the default stock location associated with a location.
     * <p>
     * NOTE: retrieval of stock locations may be an expensive operation,
     * due to the no. of relationships to products.
     *
     * @param location the location
     * @return the default location or {@code null} if none is found
     */
    public Party getDefaultStockLocation(Party location) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(location, "stockLocations", true, service);
    }

    /**
     * Returns the pricing group for a practice location.
     *
     * @param location the practice location
     * @return the pricing group (an instance of <em>lookup.pricingGroup</em>), or {@code null} if none is found
     */
    public Lookup getPricingGroup(Party location) {
        IMObjectBean bean = new IMObjectBean(location, service);
        List<Lookup> values = bean.getValues("pricingGroup", Lookup.class);
        return !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Returns the appointment reminder SMS template configured for the location.
     *
     * @param location the location
     * @return the template or {@code null} if none is configured
     */
    public Entity getAppointmentSMSTemplate(Party location) {
        return getBean(location).getNodeTargetEntity("smsAppointment");
    }

    /**
     * Returns the location mail server.
     *
     * @param location the practice location
     * @return the location server, or {@code null} if none is configured
     */
    public MailServer getMailServer(Party location) {
        Entity entity = getBean(location).getNodeTargetEntity("mailServer");
        return (entity != null) ? new MailServer(entity, service) : null;
    }

    /**
     * Returns the follow-up work lists associated with a location.
     *
     * @param location the practice location
     * @return the follow-up work lists, in the order they are defined in the location
     */
    public List<Entity> getFollowupWorkLists(Party location) {
        return getBean(location).getNodeTargetEntities("followupWorkLists", SequenceComparator.INSTANCE);
    }

    /**
     * Returns the default printer for a location.
     *
     * @param location the location
     * @return the default printer, or {@code null} if none is defined
     */
    public String getDefaultPrinter(Party location) {
        IMObjectBean bean = new IMObjectBean(location, service);
        return bean.getString("defaultPrinter");
    }

    /**
     * Returns the names of printers associated with a location.
     * <p>
     * If none are defined, it is assumed that all printers may be used
     *
     * @param location the location
     * @return the printer names
     */
    public Collection<String> getPrinterNames(Party location) {
        Set<String> result = new HashSet<>();
        ArchetypeQuery query = new ArchetypeQuery(location.getObjectReference());
        query.add(Constraints.join("printers").add(Constraints.join("target", "printer")));
        query.add(new NodeSelectConstraint("printer.name"));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IPage<ObjectSet> objects = service.getObjects(query);
        for (ObjectSet set : objects.getResults()) {
            result.add(set.getString("printer.name"));
        }
        return result;
    }

    /**
     * Wraps the location in a bean.
     *
     * @param location the location
     * @return the bean
     */
    protected EntityBean getBean(Party location) {
        return new EntityBean(location, service);
    }

}
