package org.openvpms.web.app.customer;

import java.math.BigDecimal;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;


/**
 * Renders customer summary information.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CustomerSummary {


    /**
     * Returns summary information for a customer.
     *
     * @param customer the customer. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(Party customer) {
        Component result = null;
        if (customer != null) {
            Label title = LabelFactory.create("customer.account.balance");
            Label balance = LabelFactory.create();
            BigDecimal value = getBalance(customer);
            balance.setText(NumberFormatter.format(value));
            result = RowFactory.create("CellSpacing", title, balance);
        }
        return result;
    }

    private static BigDecimal getBalance(Party customer) {
        ActResultSet set = new ActResultSet(customer.getObjectReference(),
                                            "act", "customerAccountCharges*",
                                            null, null, "Posted", 50, null);
        BigDecimal balance = BigDecimal.ZERO;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            balance = ActHelper.sum(balance, acts.getRows(), "amount");
        }
        return balance;
    }

}
