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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.ParticipationConstraint;

import java.util.Date;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_MEDICATION;
import static org.openvpms.component.system.common.query.Constraints.between;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.exists;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.Constraints.subQuery;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;

/**
 * Patient history service.
 *
 * @author Tim Anderson
 */
public class PatientHistory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Products to query.
     */
    private static final String[] PRODUCTS = {ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE,
                                              ProductArchetypes.MERCHANDISE};

    /**
     * Constructs a {@link PatientHistory}.
     *
     * @param service the archetype service
     */
    public PatientHistory(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns all medication acts for a patient.
     *
     * @param patient the patient
     * @return the medication acts for the patient
     */
    public Iterable<Act> getMedication(Party patient) {
        ArchetypeQuery query = createMedicationQuery(patient);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns medication acts for a patient, between the specified dates, inclusive.
     *
     * @param patient the patient
     * @param from    the from date-time. May be {@code null}
     * @param to      the to date-time. May be {@code null}
     * @return the medication acts for the patient
     */
    public Iterable<Act> getMedication(Party patient, Date from, Date to) {
        return getMedication(patient, null, from, to);
    }

    /**
     * Returns medication acts for a patient, with the specified product type name.
     *
     * @param patient         the patient
     * @param productTypeName the product type name. May be {@code null}
     * @return the medication acts for the patient
     */
    public Iterable<Act> getMedication(Party patient, String productTypeName) {
        return getMedication(patient, productTypeName, null, null);
    }

    /**
     * Returns medication acts for a patient, with the specified product type name, between the specified dates,
     * inclusive.
     *
     * @param patient         the patient
     * @param productTypeName the product type name. May be {@code null}
     * @param from            the from date-time. May be {@code null}
     * @param to              the to date-time. May be {@code null}
     * @return the medication acts for the patient
     */
    public Iterable<Act> getMedication(Party patient, String productTypeName, Date from, Date to) {
        ArchetypeQuery query = createMedicationQuery(patient);
        if (productTypeName != null) {
            // Original query. In MySQL 5.1 and 5.5, this uses the wrong index, resulting in very slow queries
            // query.add(join("product").add(join("entity").add(join("type").add(
            // join("source").add(eq("name", productTypeName))))));

            // New version, that uses a correlated sub-query.
            query.add(join("product", "p1"));
            query.add(exists(
                    subQuery(PRODUCTS, "p2").add(join("type").add(join("source").add(eq("name", productTypeName)).add(
                            idEq("p1.entity", "p2"))))));
        }

        if (from != null || to != null) {
            if (from != null && to != null) {
                query.add(between("startTime", from, to));
            } else if (from == null) {
                query.add(lte("startTime", to));
            } else {
                query.add(gte("startTime", from));
            }
        }
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Creates a medication query for a patient.
     *
     * @param patient the patient
     * @return medication query
     */
    private ArchetypeQuery createMedicationQuery(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(shortName("act", PATIENT_MEDICATION));
        JoinConstraint participation = join("patient");
        participation.add(eq("entity", patient));
        participation.add(new ParticipationConstraint(ActShortName, PATIENT_MEDICATION));
        query.add(participation);
        query.add(sort("startTime", false));
        query.add(sort("id", false));
        return query;
    }
}
