/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.admin.organisation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DefaultDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.subscription.core.Subscription;
import org.openvpms.subscription.core.SubscriptionFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;

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
     * @return the subscription message
     */
    public static String formatSubscription() {
        return formatSubscription(new LocalContext());
    }

    /**
     * Formats the current subscription, if any.
     *
     * @param context the context
     * @return the subscription message
     */
    public static String formatSubscription(Context context) {
        String result = null;
        Subscription subscription = getSubscription(context);
        if (subscription != null) {
            String user = subscription.getOrganisationName();
            if (user == null) {
                user = subscription.getSubscriberName();
            }
            Date expiryDate = subscription.getExpiryDate();
            if (expiryDate != null) {
                String date = DateHelper.getFullDateFormat().format(expiryDate);
                if (expiryDate.before(new Date())) {
                    result = Messages.get("subscription.summary.expired", user, date);
                } else {
                    result = Messages.get("subscription.summary.active", user, date);
                }
            }
        }
        if (result == null) {
            result = Messages.get("subscription.summary.nosubscription");
        }
        return result;
    }

    /**
     * Returns the current subscription.
     *
     * @param context the context
     * @return the subscription, or {@code null} if there is none
     */
    public static Subscription getSubscription(Context context) {
        Subscription result = null;
        try {
            Party practice = new PracticeRules().getPractice();
            if (practice != null) {
                DocumentAct act = getSubscriptionAct(practice, context);
                result = getSubscription(act, context);
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
     * @param context the context
     * @return the subscription, or {@code null} if there is none
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static Subscription getSubscription(DocumentAct act, Context context)
        throws IOException, GeneralSecurityException {
        Subscription result = null;
        if (act != null) {
            Document document = (Document) IMObjectHelper.getObject(act.getDocument(), context);
            if (document != null) {
                DocumentHandler documentHandler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT);
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
     * @param context  the context
     * @return the subscription act, or {@code null} if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static DocumentAct getSubscriptionAct(Party practice, Context context) {
        DocumentAct result = null;
        Participation participation = getSubscriptionParticipation(practice);
        if (participation != null) {
            result = (DocumentAct) IMObjectHelper.getObject(participation.getAct(), context);
        }
        return result;
    }

    /**
     * Returns the <em>participation.subscription</em> associated with an <em>party.organisationPractice</em>,
     * if available.
     *
     * @param practice the practice. A <em>party.organisationPractice</em>
     * @return the participation, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Participation getSubscriptionParticipation(Party practice) {
        ArchetypeQuery query = new ArchetypeQuery("participation.subscription", true, true);
        query.add(new ObjectRefNodeConstraint("entity", practice.getObjectReference()));
        query.setFirstResult(0);
        query.setMaxResults(1);
        List<IMObject> rows = ArchetypeServiceHelper.getArchetypeService().get(query).getResults();
        return (!rows.isEmpty()) ? (Participation) rows.get(0) : null;
    }

}
