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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.exception.ESCIException;

import javax.annotation.Resource;


/**
 * Base class for mappers between UBL to OpenVPMS types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractUBLMapper {

    /**
     * Expected UBL version.
     */
    protected static final String UBL_VERSION = "2.0";

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the supplier corresponding to a <tt>SupplierPaztyType</tt>.
     *
     * @param supplierType the supplierType
     * @param path         the supplier element path
     * @param parent       the parent element
     * @param parentId     the parent element identifier
     * @return the corresponding supplier
     * @throws ESCIException if the supplier was not found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected Party getSupplier(SupplierPartyType supplierType, String path, String parent, String parentId) {
        checkRequired(supplierType, path, parent, parentId);
        CustomerAssignedAccountIDType accountId = supplierType.getCustomerAssignedAccountID();
        long id = getNumericId(accountId, path + "/CustomerAssignedAccountID", parent, parentId);
        Party supplier = (Party) getObject(id, "party.supplier*");
        if (supplier == null) {
            Message message = ESCIAdapterMessages.invalidSupplier(path + "/CustomerAssignedAccountID", parent,
                                                                  parentId, accountId.getValue());
            throw new ESCIException(message.toString());
        }
        return supplier;
    }

    /**
     * Returns an order given its reference.
     * <p/>
     * This only returns orders associated with the specified supplier.
     *
     * @param orderRef the order reference
     * @param supplier the supplier
     * @param parent   the parent UBL document, for error reporting
     * @param parentId the parent UBL document identifier, for error reporting
     * @return the corresponding order
     * @throws ESCIException if the order was not found or was not created
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected FinancialAct getOrder(IMObjectReference orderRef, Party supplier, String parent, String parentId) {
        FinancialAct order = (FinancialAct) service.get(orderRef);
        boolean valid = false;
        if (order != null) {
            ActBean bean = new ActBean(order, service);
            if (ObjectUtils.equals(bean.getNodeParticipantRef("supplier"), supplier.getObjectReference())) {
                valid = true;
            }
        }
        if (!valid) {
            Message message = ESCIAdapterMessages.invalidOrder(parent, parentId, orderRef.getId());
            throw new ESCIException(message.toString());
        }
        return order;
    }

    /**
     * Helper to verify that a required element is non-null, raising an exception if it is.
     *
     * @param element  the element value. May be <tt>null</tt>
     * @param path     the element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @return the element
     * @throws ESCIException if the element is null
     */
    protected <T> T getRequired(T element, String path, String parent, String parentId) {
        checkRequired(element, path, parent, parentId);
        return element;
    }

    /**
     * Verifies that a required element is non-null, raising an exception if it is.
     *
     * @param element  the element value. May be <tt>null</tt>
     * @param path     the element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @throws ESCIException if the element is null
     */
    protected <T> void checkRequired(T element, String path, String parent, String parentId) {
        if (element == null) {
            Message message = ESCIAdapterMessages.ublElementRequired(path, parent, parentId);
            throw new ESCIException(message.toString());
        }
    }

    /**
     * Returns an <tt>IMObjectReference</tt> for a given archetype id and <tt>IdentfierType</tt>.
     *
     * @param archetypeId the archetype identifier
     * @param id          the identifier
     * @param path        the identifier element path
     * @param parent      the parent element
     * @param parentId    the parent element identifier
     * @return the corresponding object, or <tt>null</tt> if it is not found
     * @throws org.openvpms.esci.exception.ESCIException
     *          if <tt>id</tt> is null or is not a valid identifier
     */
    protected IMObject getObject(ArchetypeId archetypeId, IdentifierType id, String path, String parent,
                                 String parentId) {
        IMObjectReference ref = getReference(archetypeId, id, path, parent, parentId);
        return service.get(ref);
    }

    /**
     * Returns an <tt>IMObjectReference</tt> for a given archetype id and <tt>IdentfierType</tt>.
     *
     * @param archetypeId the archetype identifier
     * @param id          the identifier
     * @param path        the identifier element path
     * @param parent      the parent element
     * @param parentId    the parent element identifier
     * @return the corresponding reference
     * @throws org.openvpms.esci.exception.ESCIException
     *          if <tt>id</tt> is null or is not a valid identifier
     */
    protected IMObjectReference getReference(ArchetypeId archetypeId, IdentifierType id, String path, String parent,
                                             String parentId) {
        long objectId = getNumericId(id, path, parent, parentId);
        return new IMObjectReference(archetypeId, objectId);
    }

    /**
     * Returns the numeric value of an <tt>IdentifierType</tt>.
     *
     * @param id       the identifier
     * @param path     the identifier element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @return the numeric value of <tt>id</tt>
     * @throws org.openvpms.esci.exception.ESCIException
     *          if <tt>id</tt> is null or is not a valid identifier
     */
    protected long getNumericId(IdentifierType id, String path, String parent, String parentId) {
        String value = getId(id, path, parent, parentId);
        long result = NumberUtils.toLong(value, -1);
        if (result == -1) {
            Message message = ESCIAdapterMessages.ublInvalidIdentifier(path, parent, parentId, id.getValue());
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Returns the string value of an <tt>IdentifierType</tt>.
     *
     * @param id       the identifier
     * @param path     the identifier element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @return the value of <tt>id</tt>
     * @throws org.openvpms.esci.exception.ESCIException
     *          if <tt>id</tt> is null or empty
     */
    protected String getId(IdentifierType id, String path, String parent, String parentId) {
        String result = getId(id);
        checkRequired(result, path, parent, parentId);
        return result;
    }

    /**
     * Returns the string value of an identifier.
     *
     * @param id the identifier. May be <tt>null</tt>
     * @return the identifier value. May be <tt>null</tt>
     */
    protected String getId(IdentifierType id) {
        String result = null;
        if (id != null) {
            result = StringUtils.trimToNull(id.getValue());
        }
        return result;
    }

    /**
     * Verifies that an user is linked to a supplier.
     *
     * @param supplier the supplier
     * @param user     the ESCI user that submitted the innvoice
     * @param factory  the bean factory
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the user has no relationship to the supplier
     */
    protected void checkSupplier(Party supplier, User user, IMObjectBeanFactory factory) {
        EntityBean bean = factory.createEntityBean(user);
        if (!bean.getNodeTargetEntityRefs("supplier").contains(supplier.getObjectReference())) {
            Message message = ESCIAdapterMessages.userNotLinkedToSupplier(user, supplier);
            throw new ESCIException(message.toString());
        }
    }

    /**
     * Returns an object given its id.
     *
     * @param id         the object identifier
     * @param shortNames the possible archetype short names for the object
     * @return the corresponding object or <tt>null</tt> if it is not found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected IMObject getObject(long id, String... shortNames) {
        IMObject result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(Constraints.eq("id", id));
        IPage<IMObject> page = service.get(query);
        if (page.getResults().size() == 1) {
            result = page.getResults().get(0);
        }
        return result;
    }
}
