package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.TimeZone;

/**
 * Factory for {@link HospitalizationService} instances.
 *
 * @author Tim Anderson
 */
public class FlowSheetServiceFactory {

    /**
     * The Smart Flow Sheet URL.
     */
    private final String url;

    /**
     * The emrApiKey submitted with each request.
     */
    private final String emrApiKey;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Constructs an {@link FlowSheetServiceFactory}.
     *
     * @param url       the Smart Flow Sheet URL
     * @param emrApiKey the emrApiKey submitted with each request
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers
     */
    public FlowSheetServiceFactory(String url, String emrApiKey, IArchetypeService service, ILookupService lookups,
                                   DocumentHandlers handlers) {
        this.url = url;
        this.emrApiKey = emrApiKey;
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
    }

    /**
     * Determines if a practice location has a clinic API key.
     *
     * @param location the practice location
     * @return {@code true} if the location has a key
     */
    public boolean supportsSmartFlowSheet(Party location) {
        return getClinicAPIKey(location) != null;
    }

    /**
     * Creates a {@link HospitalizationService} for the specified practice location.
     *
     * @param location the practice location
     * @return a new {@link HospitalizationService}
     */
    public HospitalizationService getHospitalisationService(Party location) {
        String clinicKey = getClinicAPIKey(location);
        if (clinicKey == null) {
            throw new IllegalArgumentException("Argument 'location' doesn't have a clinic key");
        }
        return new HospitalizationService(url, emrApiKey, clinicKey, TimeZone.getDefault(), service, lookups, handlers);
    }

    /**
     * Returns the clinic API key for a practice location.
     *
     * @param location the practice location
     * @return the clinic API key, or {@code null} if none exists
     */
    private String getClinicAPIKey(Party location) {
        IMObjectBean bean = new IMObjectBean(location);
        return StringUtils.trimToNull(bean.getString("smartFlowSheetKey"));
    }
}
