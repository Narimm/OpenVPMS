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
import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// openvpms-framework
import org.apache.commons.lang.StringUtils;
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
     * @see org.openvpms.component.business.dao.im.party.IPartyDAO#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public List get(String rmName, String entityName, String conceptName, String instanceName) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<String> params = new ArrayList<String>();
            boolean andRequired = false;
            
            queryString.append("select party from org.openvpms.component.business.domain.im.party.Party as party");
            
            // check to see if one or more of the values have been specified
            if ((StringUtils.isEmpty(rmName) == false) ||
                (StringUtils.isEmpty(entityName) == false) ||
                (StringUtils.isEmpty(conceptName) == false) ||
                (StringUtils.isEmpty(instanceName) == false)) {
                queryString.append(" where ");
            }
            
            // process the rmName
            if (StringUtils.isEmpty(rmName) == false) {
                names.add("rmName");
                andRequired = true;
                if ((rmName.endsWith("*")) || rmName.startsWith("*")) {
                    queryString.append(" party.archetypeId.rmName like :rmName");
                    params.add(rmName.replace("*", "%"));
                } else {
                    queryString.append(" party.archetypeId.rmName = :rmName");
                    params.add(rmName);
                }
                
            }
            
            // process the entity name
            if (StringUtils.isEmpty(entityName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("entityName");
                andRequired = true;
                if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                    queryString.append(" party.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString.append(" party.archetypeId.entityName = :entityName");
                    params.add(entityName);
                }
                
            }
            
            // process the concept name
            if (StringUtils.isEmpty(conceptName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("conceptName");
                andRequired = true;
                if ((conceptName.endsWith("*")) || (conceptName.startsWith("*"))) {
                    queryString.append(" party.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString.append(" party.archetypeId.concept = :conceptName");
                    params.add(conceptName);
                }
            }
            
            // process the instance name
            if (StringUtils.isEmpty(instanceName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("instanceName");
                if ((instanceName.endsWith("*")) || (instanceName.startsWith("*"))) {
                    queryString.append(" party.name like :instanceName");
                    params.add(instanceName.replace("*", "%"));
                } else {
                    queryString.append(" party.name = :instanceName");
                    params.add(instanceName);
                }
                
            }
            
            // now execute te query
           return getHibernateTemplate().findByNamedParam(
                   queryString.toString(),
                   (String[])names.toArray(new String[names.size()]),
                   params.toArray());
        } catch (Exception exception) {
            throw new PartyDAOException(
                    PartyDAOException.ErrorCode.FailedToFindParties,
                    new Object[]{rmName, entityName, conceptName, instanceName},
                    exception);
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
