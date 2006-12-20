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


package org.openvpms.component.business.dao.hibernate.im.security;

import org.hibernate.Query;
import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.entity.HibernateResultCollectorFactory;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.im.common.ResultCollectorFactory;
import org.openvpms.component.business.dao.im.security.IUserDAO;
import org.openvpms.component.business.dao.im.security.UserDAOException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;


/**
 * This is an implementation of the IUserDAO DAO for hibernate. It uses the
 * Spring Framework's template classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class UserDAOHibernate extends HibernateDaoSupport implements IUserDAO {

    /**
     * The result collector factory.
     */
    private ResultCollectorFactory factory
            = new HibernateResultCollectorFactory();

    /**
     * Constructs a new <code>UserDAOHibernate</code>.
     */
    public UserDAOHibernate() {
    }

    /**
     * (non-Javadoc)
     *
     * @see IUserDAO#getByUserName(String)
     */
    @SuppressWarnings("unchecked")
    public List<User> getByUserName(String name) {
        List<User> results = null;
        Session session
                = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = session.getNamedQuery("user.getByUserName");
            query.setString("name", name);
            ResultCollector<IMObject> collector
                    = factory.createIMObjectCollector();
            for (Object object : query.list()) {
                collector.collect(object);
            }
            List objects;
            objects = collector.getPage().getResults();
            results = objects;
        } catch (Exception exception) {
            throw new UserDAOException(
                    UserDAOException.ErrorCode.FailedToFindUserRecordByName,
                    new Object[]{name}, exception);
        } finally {
            session.close();
        }

        return results;
    }

}
