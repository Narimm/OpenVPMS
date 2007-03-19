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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLObjectDAO {

    private SessionFactory factory;

    public ETLObjectDAO(Properties properties) {
        this(new Configuration().addProperties(properties));
    }

    protected ETLObjectDAO(Configuration config) {
        config.addClass(ETLObject.class);
        config.addClass(ETLNode.class);
        factory = config.buildSessionFactory();
    }

    public ETLObjectDAO() {
        this(new Configuration());
    }

    public void save(ETLObject object) {
        save(Arrays.asList(object));
    }

    public void save(Iterable<ETLObject> objects) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (ETLObject object : objects) {
                session.saveOrUpdate(object);
            }
            tx.commit();
        } catch (RuntimeException exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            session.close();
        }
    }

    public ETLObject get(long objectId) {
        ETLObject result = null;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select o from ");
        queryString.append(ETLObject.class.getName());
        queryString.append(" as o where o.objectId = :objectId");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("objectId", objectId);
            List set = query.list();
            if (!set.isEmpty()) {
                result = (ETLObject) set.get(0);
            }
        } finally {
            session.close();
        }
        return result;
    }
}
