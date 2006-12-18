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

package org.openvpms.archetype.function.party;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * JXPath extension functions that operate on {@link Party} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PartyFunctions {

    /**
     * Returns a set of default Contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a set of contacts. May be <code>null</code>
     */
    public static Set<Contact> getDefaultContacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getDefaultContacts((Party) pointer.getValue());
    }

    /**
     * Returns a list of default contacts.
     *
     * @param party the party
     * @return a list of default contacts
     */
    public static Set<Contact> getDefaultContacts(Party party) {
    	Set<Contact> contacts = new HashSet<Contact>();
    	IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
    	Contact phone = (Contact)service.create("contact.phoneNumber");
    	Contact location = (Contact)service.create("contact.location");
    	service.deriveValues(phone);
    	service.deriveValues(location);
    	contacts.add(phone);
    	contacts.add(location);
    	return contacts;
    }
    
    /**
     * Returns the full name for the passed party.
     *
     * @param context the expression context. Expected to reference a patient party.
     * @return the parties full name.
     */
    public static String getPartyFullName(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getPartyFullName((Party) pointer.getValue());
    }

    /**
     * Return a formatted name for the party
     * 
     * @param party the party 
     * @return String Formatted name string
     */
    
    public static String getPartyFullName(Party party) {
    	try {
	    	if (party != null) {
		        IMObjectBean bean = new IMObjectBean(party);
		        if (TypeHelper.isA(party, "party.customerperson")) {
		        	return  getPersonName(bean);
		        } else if (TypeHelper.isA(party, "party.customerOrganisation")) {
		        	return bean.getString("name", "");
		        } else if (TypeHelper.isA(party, "party.patientpet")) {
		        	return bean.getString("name", "");
		        } else {
		        	return "";
		        }
	    	}
	    	else {
	    		return "";
	    	}
    	}
    	catch (Exception e) {
    		return "";
    	}
    		
    }
    
    /**
     * Return a formatted name for the customerPerson party
     * 
     * @param party the party 
     * @return String Formatted name string
     */
    
    public static String getPersonName(IMObjectBean bean) {
    	String title = LookupHelper.getName(ArchetypeServiceHelper.getArchetypeService(), 
    			bean.getDescriptor("title"), bean.getObject());
    	if (title != null) {
    		return title + " " + bean.getString("firstName", "") + " " + bean.getString("lastName", "");
    	}
    	else {
    		return bean.getString("firstName", "") + bean.getString("lastName", "");    		
    	}
    }

    /**
     * Returns the current owner party for the passed party.
     *
     * @param context the expression context. Expected to reference a patient party.
     * @return the patients current owner Party. May be <code>null</code>
     */
    public static Party getPatientOwner(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getPatientOwner((Party) pointer.getValue());
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <code>null</code> if none can be found
     */
    public static Party getPatientOwner(Party patient) {
    	return new PatientRules().getOwner(patient);
    }
    
    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted list of contacts. May be <code>null</code>
     */
    public static String getPreferredContacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getPreferredContacts((Party) pointer.getValue());
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param party the party
     * @return a formatted list of contacts
     */
    public static String getPreferredContacts(Party party) {
        StringBuffer result = new StringBuffer();
        if (party != null) {
            for (Contact contact : party.getContacts()) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (bean.hasNode("preferred")) {
                    boolean preferred = bean.getBoolean("preferred");
                    if (preferred) {
                        String description = getContactDescription(bean);
                        if (description != null) {
                            if (result.length() != 0) {
                                result.append(", ");
                            }
                            result.append(description);
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getBillingAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        return getBillingAddress((Party) pointer.getValue());
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @return a formatted billing address
     */
    public static String getBillingAddress(Party party) {
        return getAddress(party, "Billing");
        
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getCorrespondenceAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        return getCorrespondenceAddress((Party) pointer.getValue());
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @return a formatted billing address
     */
    public static String getCorrespondenceAddress(Party party) {
        return getAddress(party, "Correspondence");
        
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @param purpose the contact purpose fro the address
     * @return a formatted billing address
     */
    public static String getAddress(Party party, String purpose) {
        Contact contact = getContact(party, "contact.location", purpose);
        if (contact != null) {
            return formatAddress(contact);
        }
        else {
            return "";        	
        }
        
    }

    /**
     * Returns a contact for the specified Party, contact type and purpose.
     * If cannot find one with matching purpose returns last preferred contact.
     * If cannot find with matching purpose and preferred returns last found.
     *  
     * @param party the party
     * @param type the contact type as archetype shortname
     * @param purpose the contact purpose fro the address
     * @return a formatted billing address
     */
    public static Contact getContact(Party party, String type, String purpose) {
        Contact currentContact = null;
        if (party != null) {
            for (Contact contact : party.getContacts()) {
                //We are only interested in location contacts
                if (contact.getArchetypeId().getShortName().equals(type)) {
                    IMObjectBean bean = new IMObjectBean(contact);
                    // If has  the passed contact purpose this is our contact.
                    if (hasContactPurpose(contact, purpose)) {
                        currentContact = contact;
                        break;
                    }
                    // If preferred location save but keep searching just in case we have one with billing
                    // purpose or another preferred.
                    if (bean.hasNode("preferred") && bean.getBoolean("preferred"))
                        currentContact = contact;
                    else {
                    	// Otherwise if we don't have one set to current contact
                    	if (currentContact == null){
                    		currentContact = contact;
                    	}
                    }
                }
            }
        }
        return currentContact;        
    }

    /**
     * Indicates if a contact has a particular purpose 
     * 
     * @param contact the contact
     * @param contact purpose string
     * @return True or False
     */
    
    private static Boolean hasContactPurpose(Contact contact, String contactPurpose) {
        for (Classification classification : contact.getClassifications()) {
            if (classification.getName().equalsIgnoreCase(contactPurpose))
                return true;
        }
        return false;
        
    }

    /**
     * Format Address
     * 
     * @param contact contact 
     * @return String Formatted address string
     */
    
    private static String formatAddress(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact);
        String address = bean.getString("address");
        String suburb = bean.getString("suburb");
        String state = bean.getString("state");
        String postcode = bean.getString("postcode");
        return address + "\n" + suburb + " " + state + " " + postcode;
        
    }
    /**
     * Returns the description of a contact.
     *
     * @param contact the contact
     * @return the description of <code>object</code>. May be <code>null</code>
     */
    private static String getContactDescription(IMObjectBean contact) {
        StringBuffer result = new StringBuffer();
        if (contact.hasNode("description")) {
            result.append(contact.getString("description"));
        }

        if (contact.hasNode("purposes")) {
            List<IMObject> list = contact.getValues("purposes");
            if (!list.isEmpty()) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append("(");
                result.append(getValues(list, "name"));
                result.append(")");
            }
        }
        return result.toString();
    }

    /**
     * Returns a concatenated list of values for a set of objects.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the stringified value of <code>node</code> for each object,
     *         separated by ", "
     */
    private static String getValues(List<IMObject> objects, String node) {
        StringBuffer result = new StringBuffer();

        for (IMObject object : objects) {
            IMObjectBean bean = new IMObjectBean(object);
            if (bean.hasNode(node)) {
                if (result.length() != 0) {
                    result.append(", ");
                }
                result.append(bean.getString(node));
            }
        }
        return result.toString();
    }

    /**
     * Returns a stringfield form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String identities(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        StringBuffer result = new StringBuffer();

        Party party = (Party) pointer.getValue();
        for (EntityIdentity identity : party.getIdentities()) {
            IMObjectBean bean = new IMObjectBean(identity);
            if (bean.hasNode("name")) {
                String name = bean.getString("name");
                String displayName = bean.getDisplayName();
                if (name != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(displayName);
                    result.append(": ");
                    result.append(name);
                }
            }
        }
        return result.toString();
    }

}
