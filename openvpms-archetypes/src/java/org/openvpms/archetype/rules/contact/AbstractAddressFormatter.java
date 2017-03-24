package org.openvpms.archetype.rules.contact;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;

/**
 * Abstract implementation of the {@link AddressFormatter}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAddressFormatter implements AddressFormatter {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The lookup service.
     */
    private ILookupService lookups;

    /**
     * Constructs an {@link AbstractAddressFormatter}.
     *
     * @param service the archetype service
     * @param lookups the lookups
     */
    public AbstractAddressFormatter(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Formats an address.
     *
     * @param location   the location
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted address. May be {@code null}
     */
    @Override
    public String format(Contact location, boolean singleLine) {
        IMObjectBean bean = new IMObjectBean(location, service);
        String address = bean.getString("address");
        String suburb = lookups.getName(location, "suburb");
        String stateName = lookups.getName(location, "state");
        String stateCode = bean.getString("state");
        String postcode = bean.getString("postcode");
        return format(location, address, suburb, stateCode, stateName, postcode, singleLine);
    }

    /**
     * Formats an address.
     *
     * @param location   the location contact
     * @param address    the address
     * @param suburb     the suburb
     * @param stateCode  the state code
     * @param stateName  the state name
     * @param postcode   the postcode
     * @param singleLine if {@code true} formats the address on a single line
     * @return the formatted address
     */
    protected String format(Contact location, String address, String suburb, String stateCode, String stateName,
                            String postcode, boolean singleLine) {
        return formatDefault(address, suburb, stateName, postcode, singleLine);
    }

    /**
     * Formats using the default format.
     *
     * @param address    the address
     * @param suburb     the suburb
     * @param state      the state
     * @param postcode   the postcode
     * @param singleLine if {@code true} formats the address on a single line
     * @return the formatted address
     */
    protected String formatDefault(String address, String suburb, String state, String postcode, boolean singleLine) {
        StringBuilder result = new StringBuilder();
        if (!StringUtils.isEmpty(address)) {
            if (singleLine) {
                result.append(address.replace('\n', ' '));
                result.append(", ");
            } else {
                result.append(address);
                result.append("\n");
            }
        }
        if (!StringUtils.isEmpty(suburb)) {
            result.append(suburb);
            result.append(" ");
        }
        if (!StringUtils.isEmpty(state)) {
            result.append(state);
            result.append(" ");
        }
        if (!StringUtils.isEmpty(postcode)) {
            result.append(postcode);
        }
        return result.toString();
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Return the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookups() {
        return lookups;
    }

}
