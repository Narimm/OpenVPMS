/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.statement;

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.archetype.rules.finance.statement.StatementRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link ProcessorListener} interface,
 * for statement events.
 *
 * @author Tim Anderson
 */
public abstract class AbstractStatementProcessorListener
    implements ProcessorListener<Statement> {

    /**
     * The statement rules.
     */
    private final StatementRules rules;

    /**
     * The account rules.
     */
    private final CustomerAccountRules account;


    /**
     * Creates a new {@code AbstractStatementProcessorListener}.
     *
     * @param practice the practice
     */
    public AbstractStatementProcessorListener(Party practice) {
        account = new CustomerAccountRules(ServiceHelper.getArchetypeService());
        rules = new StatementRules(practice);
    }

    /**
     * Returns the parameters to pass to the statement report
     * This includes the statement date and overdue balance.
     *
     * @param statement the statement
     * @return a map of parameters
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Map<String, Object> getParameters(Statement statement) {
        Map<String, Object> result = new HashMap<String, Object>();
        Date date = statement.getStatementDate();
        BigDecimal overdueBalance = account.getOverdueBalance(statement.getCustomer(), date);
        result.put("statementDate", date);
        result.put("overdueBalance", overdueBalance);
        result.put("preview", statement.isPreview());
        return result;
    }

    /**
     * Marks a statement as being printed.
     *
     * @param statement the statement
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void setPrinted(Statement statement) {
        rules.setPrinted(statement.getCustomer(), statement.getStatementDate());
    }

}
