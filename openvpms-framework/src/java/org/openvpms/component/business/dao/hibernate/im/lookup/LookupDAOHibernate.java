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

package org.openvpms.component.business.dao.hibernate.im.lookup;

// spring-framework
import java.util.List;

import org.springframework.orm.hibernate.support.HibernateDaoSupport;

// openvpms-framework
import org.openvpms.component.business.dao.im.lookup.ILookupDAO;
import org.openvpms.component.business.dao.im.lookup.LookupDAOException;
import org.openvpms.component.business.dao.im.party.PartyDAOException;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * This is a hibernate implementation of the {@link ILookupDAO} interface, which
 * is based on the Spring Framework's {@link HibernateDaoSupport} object.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupDAOHibernate extends HibernateDaoSupport implements
        ILookupDAO {

    /**
     * Default constructor
     */
    public LookupDAOHibernate() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#delete(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void delete(Lookup lookup) {
        try {
            getHibernateTemplate().delete(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToDeleteLookup,
                    new Object[] { lookup });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#delete(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void delete(LookupRelationship relationship) {
        try {
            getHibernateTemplate().delete(relationship);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToDeleteLookupRelationship,
                    new Object[] { relationship });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getLookups(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Lookup[] getLookupsByConcept(String concept) {
        try {
            List results = getHibernateTemplate()
                    .findByNamedQueryAndNamedParam("lookup.getByConcept",
                            new String[] { "concept" },
                            new Object[] { concept });

            return (Lookup[]) results.toArray(new Lookup[results.size()]);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetLookupsByConcept,
                    new Object[] { concept });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getSourceLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    @SuppressWarnings("unchecked")
    public Lookup[] getSourceLookups(String type, Lookup target) {
        try {
            List results = getHibernateTemplate()
                    .findByNamedQueryAndNamedParam("lookupRelationship.getSourceLookups",
                            new String[] { "type", "target" },
                            new Object[] { type, target });

            return (Lookup[]) results.toArray(new Lookup[results.size()]);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetSourceLookups,
                    new Object[] { type, target });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getTargetLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    @SuppressWarnings("unchecked")
    public Lookup[] getTargetLookups(String type, Lookup source) {
        try {
            List results = getHibernateTemplate()
                    .findByNamedQueryAndNamedParam("lookupRelationship.getTargetLookups",
                            new String[] { "type", "source" },
                            new Object[] { type, source });

            return (Lookup[]) results.toArray(new Lookup[results.size()]);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetTargetLookups,
                    new Object[] { type, source });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#insert(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void insert(Lookup lookup) {
        try {
            getHibernateTemplate().save(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToInsertLookup,
                    new Object[] { lookup });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#insert(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void insert(LookupRelationship relationship) {
        try {
            getHibernateTemplate().save(relationship);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToInsertLookupRelationship,
                    new Object[] { relationship });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#update(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void update(Lookup lookup) {
        try {
            getHibernateTemplate().saveOrUpdate(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToUpdateLookup,
                    new Object[] { lookup });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#findById(java.lang.String)
     */
    public Party findById(String id) {
        try {
            return (Party) getHibernateTemplate().load(Party.class, id);
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToFindPartyWithId,
                    new Object[] { id });
        }
    }

}
