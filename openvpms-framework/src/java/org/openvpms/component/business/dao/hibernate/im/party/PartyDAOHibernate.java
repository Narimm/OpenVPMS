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

package org.openvpms.component.business.dao.hibernate.im.party;

// spring-framework
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

// openvpms-framework
import org.openvpms.component.business.dao.im.party.IPartyDAO;
import org.openvpms.component.business.dao.im.party.PartyDAOException;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * This is a hibernate implementation of the {@link IPartyDAO} interface, which
 * is based on the Spring Framework's {@link HibernateDaoSupport} object.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PartyDAOHibernate extends HibernateDaoSupport implements IPartyDAO {

    /**
     * Default constructor
     */
    public PartyDAOHibernate() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#delete(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void delete(Party party) {
        try {
            getHibernateTemplate().delete(party);
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToDeleteParty,
                    new Object[]{party});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#findById(java.lang.String)
     */
    public Party findById(String id) {
        try {
            return (Party)getHibernateTemplate().load(Party.class, id);
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToFindPartyWithId,
                    new Object[]{id});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#insert(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void insert(Party party) {
        try{
            getHibernateTemplate().save(party);
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToInsertParty,
                    new Object[]{party}, exception);
        }
}

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#update(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void update(Party party) {
        try {
            getHibernateTemplate().saveOrUpdate(party);
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToUpdateParty,
                    new Object[]{party});
        }
    }

}
