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

package org.openvpms.web.component.subscription;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openvpms.archetype.rules.doc.DefaultDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.subscription.core.Subscription;
import org.openvpms.subscription.core.SubscriptionFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;

/**
 * Subscription helper methods.
 *
 * @author Tim Anderson
 */
public class SubscriptionHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SubscriptionHelper.class.getName());

    /**
     * Formats the current subscription, if any.
     *
     * @return the subscription message, possibly containing HTML
     */
    public static String formatSubscription(IArchetypeService service) {
        Subscription subscription = getSubscription(service);
        String organisation = (subscription != null) ? subscription.getOrganisationName() : null;
        String name = (subscription != null) ? subscription.getSubscriberName() : null;
        Date expiryDate = (subscription != null) ? subscription.getExpiryDate() : null;
        return formatSubscription(organisation, name, expiryDate, new Date());
    }

    /**
     * Returns the current subscription.
     *
     * @param service the archetype service
     * @return the subscription, or {@code null} if there is none
     */
    public static Subscription getSubscription(IArchetypeService service) {
        Subscription result = null;
        try {
            Party practice = new PracticeRules(service, null).getPractice();
            if (practice != null) {
                DocumentAct act = getSubscriptionAct(practice, service);
                result = getSubscription(act, service);
            }
        } catch (Throwable exception) {
            log.error(exception);
        }
        return result;
    }

    /**
     * Returns the subscription.
     *
     * @param act     the <em>act.subscription</em>
     * @param service the archetype service
     * @return the subscription, or {@code null} if there is none
     * @throws IOException              for any I/O error
     * @throws GeneralSecurityException for any security error
     */
    public static Subscription getSubscription(DocumentAct act, IArchetypeService service)
            throws IOException, GeneralSecurityException {
        Subscription result = null;
        if (act != null && act.getDocument() != null) {
            Document document = (Document) service.get(act.getDocument());
            if (document != null) {
                DocumentHandler documentHandler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT,
                                                                             service);
                InputStream content = documentHandler.getContent(document);
                result = SubscriptionFactory.create(content);
            }
        }
        return result;
    }

    /**
     * Returns the subscription act associated with an  <em>party.organisationPractice</em>.
     *
     * @param practice the practice. A <em>party.organisationPractice</em>
     * @param service  the archetype service
     * @return the subscription act, or {@code null} if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static DocumentAct getSubscriptionAct(Party practice, IArchetypeService service) {
        DocumentAct result = null;
        Participation participation = getSubscriptionParticipation(practice, service);
        if (participation != null) {
            result = (DocumentAct) getObject(participation.getAct(), service);
        }
        return result;
    }

    /**
     * Returns the <em>participation.subscription</em> associated with an <em>party.organisationPractice</em>,
     * if available.
     *
     * @param practice the practice. A <em>party.organisationPractice</em>
     * @param service  the archetype service
     * @return the participation, or {@code null} if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Participation getSubscriptionParticipation(Party practice, IArchetypeService service) {
        ArchetypeQuery query = new ArchetypeQuery("participation.subscription", true, true);
        query.add(new ObjectRefNodeConstraint("entity", practice.getObjectReference()));
        query.setFirstResult(0);
        query.setMaxResults(1);
        List<IMObject> rows = service.get(query).getResults();
        return (!rows.isEmpty()) ? (Participation) rows.get(0) : null;
    }

    /**
     * Formats a subscription.
     *
     * @param organisation the organisation name. May be {@code null}
     * @param name         the subscriber name. May be {@code null}
     * @param expiryDate   the subscription expiry date. May be {@code null}
     * @param now          the current date
     * @return the subscription message
     */
    static String formatSubscription(String organisation, String name, Date expiryDate, Date now) {
        String result = null;
        if (organisation != null || name != null) {
            String user = organisation;
            if (user == null) {
                user = name;
            }
            user = StringEscapeUtils.escapeHtml(user);
            if (expiryDate != null) {
                String date = DateFormatter.getFullDateFormat().format(expiryDate);
                date = StringEscapeUtils.escapeHtml(date);
                if (DateRules.compareDates(expiryDate, now) < 0) {
                    result = Messages.format("subscription.summary.expired", user, date);
                } else if (Days.daysBetween(new DateTime(now), new DateTime(expiryDate)).getDays() <= 21) {
                    result = Messages.format("subscription.summary.expiring", user, date);
                } else {
                    result = Messages.format("subscription.summary.active", user, date);
                }
            }
        }
        if (result == null) {
            result = Messages.get("subscription.summary.nosubscription");
        }
        return result;
    }

    /**
     * Helper to return an object by reference.
     *
     * @param ref     the reference. May be {@code null}
     * @param service the archetype service
     * @return the corresponding object, or {@code null} if none is found
     */
    private static IMObject getObject(IMObjectReference ref, IArchetypeService service) {
        return (ref != null) ? service.get(ref) : null;
    }

}
