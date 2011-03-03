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
package org.openvpms.esci.adapter.map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.IdentifierType;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.basic.AdditionalAccountIDType;
import org.openvpms.esci.ubl.common.basic.CustomerAssignedAccountIDType;
import org.openvpms.esci.ubl.common.basic.SupplierAssignedAccountIDType;

import java.util.List;


/**
 * Wrapper around UBL types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class UBLType {

    /**
     * The parent element. May be <tt>null</tt>.
     */
    private final UBLType parent;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Stock location archetype id.
     */
    private static final ArchetypeId STOCK_LOCATION = new ArchetypeId(StockArchetypes.STOCK_LOCATION);


    /**
     * Constructs a new <tt>UBLType</tt>.
     *
     * @param parent  the parent element. May be <tt>null</tt>
     * @param service the archetype service
     */
    public UBLType(UBLType parent, IArchetypeService service) {
        this.parent = parent;
        this.service = service;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public abstract String getType();

    /**
     * Returns the type identifier.
     *
     * @return the type identifier
     * @throws ESCIAdapterException if the identifier is mandatory by not set, or the identifier is incorrectly
     *                              specified
     */
    public abstract String getID();

    /**
     * Returns the path to the element, relative to its parent.
     *
     * @return the path
     */
    public String getPath() {
        return getType();
    }

    /**
     * Returns the immediate parent.
     *
     * @return the parent, or <tt>null</tt> if there is no parent
     */
    public UBLType getParent() {
        return parent;
    }

    /**
     * Determines if the {@link #getType type} and {@link #getID identifier} of this should be used for
     * error reporting. If not, then the parent should be used.
     *
     * @return <tt>true</tt> if the type and ID should be used for error reporting, <tt>false</tt> if the parent should
     *         be used
     */
    public boolean useForErrorReporting() {
        return false;
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
     * Helper to verify that a required element is non-null, raising an exception if it is.
     *
     * @param element the element value. May be <tt>null</tt>
     * @param path    the element path
     * @return the element
     * @throws ESCIAdapterException if the element is null
     */
    protected <T> T getRequired(T element, String path) {
        checkRequired(element, path);
        return element;
    }

    /**
     * Verifies that a required element is non-null, raising an exception if it is.
     *
     * @param element the element value. May be <tt>null</tt>
     * @param path    the element path
     * @throws ESCIAdapterException if the element is null
     */
    protected <T> void checkRequired(T element, String path) {
        if (element == null) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.ublElementRequired(context.getPath(), context.getType(),
                                                                                  context.getID()));
        }
    }

    /**
     * Returns the numeric value of an <tt>IdentifierType</tt>.
     *
     * @param id   the identifier
     * @param path the identifier element path
     * @return the numeric value of <tt>id</tt>
     * @throws ESCIAdapterException if <tt>id</tt> is null or is not a valid identifier
     */
    protected long getNumericId(IdentifierType id, String path) {
        String value = getId(id, path);
        long result = NumberUtils.toLong(value, -1);
        if (result == -1) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidIdentifier(
                    context.getPath(), context.getType(), context.getID(), id.getValue()));
        }
        return result;
    }

    /**
     * Returns the string value of an <tt>IdentifierType</tt>.
     *
     * @param id   the identifier
     * @param path the identifier element path
     * @return the value of <tt>id</tt>
     * @throws ESCIAdapterException if <tt>id</tt> is null or empty
     */
    protected String getId(IdentifierType id, String path) {
        String result = getId(id);
        checkRequired(result, path);
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
     * Returns the supplier corresponding to a <tt>SupplierPartyType</tt>, if a CustomerAssignedAccountID is present.
     *
     * @param supplierType the supplierType
     * @param path         the supplier element path
     * @return the corresponding supplier, or <tt>null</tt>
     * @throws ESCIAdapterException      if the supplier was specified but not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Party getSupplier(SupplierPartyType supplierType, String path) {
        checkRequired(supplierType, path);
        CustomerAssignedAccountIDType accountId = supplierType.getCustomerAssignedAccountID();
        Party supplier = null;
        if (accountId != null) {
            long id = getNumericId(accountId, path + "/CustomerAssignedAccountID");
            supplier = (Party) getObject(id, "party.supplier*");
            if (supplier == null) {
                ErrorContext context = new ErrorContext(this, path + "/CustomerAssignedAccountID");
                throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplier(context.getPath(), context.getType(),
                                                                                   context.getID(),
                                                                                   accountId.getValue()));
            }
        }
        return supplier;
    }

    /**
     * Returns the supplier assigned account id for the supplier, if one is provided.
     *
     * @param supplierType the supplierType
     * @param path         the supplier element path
     * @return the supplier assigned account id for the supplier, or <tt>null</tt> if none is specified
     */
    protected String getSupplierId(SupplierPartyType supplierType, String path) {
        checkRequired(supplierType, path);
        return getAdditionalAccountId(supplierType.getAdditionalAccountID(), path + "/AdditionalAccountID");
    }

    /**
     * Verifies that the supplier matches that expected.
     *
     * @param expectedSupplier  the expected supplier
     * @param expectedAccountId the expected account identifier. May be <tt>null</tt>
     * @param supplier          the actual supplier
     * @param accountId         the actual account identifier
     * @param path              the path to the element, from the parent
     * @throws ESCIAdapterException if the supplier is invalid
     */
    protected void checkSupplier(Party expectedSupplier, String expectedAccountId, Party supplier, String accountId,
                                 String path) {
        if (supplier == null && accountId == null) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.noCustomerOrAdditionalAccountId(context.getPath(),
                                                                                               context.getType(),
                                                                                               context.getID()));
        }
        if (supplier != null && !expectedSupplier.equals(supplier)) {
            ErrorContext context = new ErrorContext(this, path + "/CustomerAssignedAccountID");
            throw new ESCIAdapterException(ESCIAdapterMessages.supplierMismatch(context.getPath(), context.getType(),
                                                                                context.getID(), expectedSupplier,
                                                                                supplier));
        }
        if (accountId != null && !accountId.equals(expectedAccountId)) {
            ErrorContext context = new ErrorContext(this, path + "/AdditionalAccountID");
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                    context.getPath(), context.getType(), context.getID(), expectedAccountId, accountId));
        }
    }

    /**
     * Returns the stock location, if the <tt>AccountingCustomerParty/CustomerAssignedAccountID</tt> is provided.
     *
     * @param customerType the customer
     * @param path         the customer element path
     * @return the stock location, or <tt>null</tt> if the CustomerAssignedAccountID is not present
     * @throws ESCIAdapterException      if the stock location was not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Party getStockLocation(CustomerPartyType customerType, String path) {
        checkRequired(customerType, path);
        CustomerAssignedAccountIDType accountId = customerType.getCustomerAssignedAccountID();
        Party location = null;
        if (accountId != null) {
            String idPath = path + "/CustomerAssignedAccountID";
            location = (Party) getObject(STOCK_LOCATION, accountId, idPath);
            if (location == null) {
                ErrorContext context = new ErrorContext(this, idPath);
                throw new ESCIAdapterException(ESCIAdapterMessages.invalidStockLocation(
                        context.getPath(), context.getType(), context.getID(), accountId.getValue()));
            }
        }
        return location;
    }

    /**
     * Returns the supplier assigned account id for the stock location (i.e customer), if one is provided.
     *
     * @param customerType the customer type
     * @param path         the customer element path
     * @return the supplier assigned account id for the stock location, or <tt>null</tt> if none is specified
     * @throws ESCIAdapterException if there is more than one identifier, or the available identifier is null or empty
     */
    protected String getStockLocationId(CustomerPartyType customerType, String path) {
        checkRequired(customerType, path);
        SupplierAssignedAccountIDType accountId = customerType.getSupplierAssignedAccountID();
        return (accountId != null) ? getId(accountId, path + "/SupplierAssignedAccountID") : null;
    }

    /**
     * Verifies that the stock location matches that expected.
     *
     * @param expectedStockLocation the expected stock location
     * @param expectedAccountId     the expected account identifier. May be <tt>null</tt>
     * @param stockLocation         the actual stock location
     * @param accountId             the actual account identifier
     * @param path                  the path to the element, from the parent
     * @throws ESCIAdapterException if the stock location is invalid
     */
    protected void checkStockLocation(Party expectedStockLocation, String expectedAccountId, Party stockLocation,
                                      String accountId, String path) {
        if (stockLocation == null && accountId == null) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.noCustomerOrSupplierAccountId(
                    context.getPath(), context.getType(), context.getID()));
        }
        if (stockLocation != null && !expectedStockLocation.equals(stockLocation)) {
            ErrorContext context = new ErrorContext(this, path + "/CustomerAssignedAccountID");
            throw new ESCIAdapterException(ESCIAdapterMessages.stockLocationMismatch(
                    context.getPath(), context.getType(), context.getID(), expectedStockLocation, stockLocation));
        }
        if (accountId != null && !accountId.equals(expectedAccountId)) {
            ErrorContext context = new ErrorContext(this, path + "/SupplierAssignedAccountID");
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                    context.getPath(), context.getType(), context.getID(), expectedAccountId, accountId));
        }
    }

    /**
     * Returns an addition account identifier, if one is present.
     *
     * @param ids  the available additional account identifiers. Must be 0..1
     * @param path the element path
     * @return the additional account identifier, or <tt>null</tt> if none is available
     * @throws ESCIAdapterException if there is more than one identifier, or the available identifier is null or empty
     */
    protected String getAdditionalAccountId(List<AdditionalAccountIDType> ids, String path) {
        String result = null;
        if (!ids.isEmpty()) {
            if (ids.size() != 1) {
                ErrorContext context = new ErrorContext(this, path);
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                        context.getPath(), context.getType(), context.getID(), "1", ids.size()));
            }
            result = getId(ids.get(0), path);
        }
        return result;
    }

    /**
     * Returns an object given its id.
     *
     * @param id         the object identifier
     * @param shortNames the possible archetype short names for the object
     * @return the corresponding object or <tt>null</tt> if it is not found
     * @throws ArchetypeServiceException for any archetype service error
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

    /**
     * Returns an <tt>IMObjectReference</tt> for a given archetype id and <tt>IdentfierType</tt>.
     *
     * @param archetypeId the archetype identifier
     * @param id          the identifier
     * @param path        the identifier element path
     * @return the corresponding object, or <tt>null</tt> if it is not found
     * @throws ESCIAdapterException      if <tt>id</tt> is null or is not a valid identifier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject getObject(ArchetypeId archetypeId, IdentifierType id, String path) {
        IMObjectReference ref = getReference(archetypeId, id, path);
        return service.get(ref);
    }

    /**
     * Returns an <tt>IMObjectReference</tt> for a given archetype id and <tt>IdentfierType</tt>.
     *
     * @param archetypeId the archetype identifier
     * @param id          the identifier
     * @param path        the identifier element path
     * @return the corresponding reference
     * @throws ESCIAdapterException if <tt>id</tt> is null or is not a valid identifier
     */
    protected IMObjectReference getReference(ArchetypeId archetypeId, IdentifierType id, String path) {
        long objectId = getNumericId(id, path);
        return new IMObjectReference(archetypeId, objectId);
    }

}
