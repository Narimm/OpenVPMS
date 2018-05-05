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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks stock on hand for multiple products.
 *
 * @author Tim Anderson
 */
public class StockOnHand {

    /**
     * The stock rules.
     */
    private final StockRules rules;

    /**
     * Tracks stock by product and stock location.
     */
    private Map<Key, Stock> onHand = new HashMap<>();

    /**
     * Tracks acts and the stock quantities they consume.
     */
    private Map<FinancialAct, State> states = new HashMap<>();

    /**
     * Constructs a {@link StockOnHand}.
     *
     * @param rules the ztock rules
     */
    public StockOnHand(StockRules rules) {
        this.rules = rules;
    }

    /**
     * Returns the stock available for an act.
     * <p/>
     * This uses the on-hand quantity, excluding any uncommitted changes.
     *
     * @param act the act
     * @return the available stock, or {@code null} if the act doesn't have a stock location and product
     */
    public BigDecimal getAvailableStock(FinancialAct act) {
        ActBean bean = new ActBean(act);
        IMObjectReference product = bean.getNodeParticipantRef("product");
        IMObjectReference stockLocation = bean.getNodeParticipantRef("stockLocation");
        State state = getState(act);
        Key key = null;
        if (product != null && stockLocation != null) {
            key = new Key(product, stockLocation);
        }
        if (state.key == null) {
            state.key = key;
        } else if (key == null || !state.key.equals(key)) {
            Stock stock = onHand.get(state.key);
            if (stock != null) {
                stock.remove(state);
            }
            state.key = key;
        }
        if (key != null) {
            Stock stock = getStock(key);
            stock.add(state);
            return stock.getAvailableStock();
        }
        return null;
    }

    /**
     * Returns the stock for a product and stock location.
     * <p/>
     * This returns the on-hand quantity, ignoring any uncommitted changes.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the on-hand stock
     */
    public BigDecimal getStock(IMObjectReference product, IMObjectReference stockLocation) {
        Key key = new Key(product, stockLocation);
        Stock stock = getStock(key);
        return stock.getStock();
    }

    /**
     * Removes an act.
     * <p/>
     * Any stock it used or returned will no longer be included in stock calculations.
     *
     * @param act the act to remove
     */
    public void remove(FinancialAct act) {
        State state = states.remove(act);
        if (state != null) {
            Stock stock = onHand.get(state.key);
            if (stock != null) {
                stock.remove(state);
            }
        }
    }

    /**
     * Clears the stock.
     */
    public void clear() {
        states.clear();
        onHand.clear();
    }

    /**
     * Returns the state for an act.
     *
     * @param act the act
     * @return the state
     */
    private State getState(FinancialAct act) {
        State state = states.get(act);
        if (state == null) {
            state = new State(act);
            states.put(act, state);
        }
        return state;
    }

    /**
     * Returns the stock for a product and stock location.
     *
     * @param key the product and stock location
     * @return the stock
     */
    private Stock getStock(Key key) {
        Stock stock = onHand.get(key);
        if (stock == null) {
            stock = new Stock(rules.getStock(key.product, key.stockLocation));
            onHand.put(key, stock);
        }
        return stock;
    }

    /**
     * Helper to link a product and stock location for use as a map key.
     */
    private static class Key {

        private IMObjectReference product;
        private IMObjectReference stockLocation;

        public Key(Product product, IMObjectReference stockLocation) {
            this(product.getObjectReference(), stockLocation);
        }

        public Key(IMObjectReference product, IMObjectReference stockLocation) {
            this.product = product;
            this.stockLocation = stockLocation;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(product.hashCode()).append(stockLocation.hashCode()).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Key) {
                Key key = (Key) obj;
                return product.equals(key.product) && stockLocation.equals(key.stockLocation);
            }
            return false;
        }
    }

    private static class State {
        private final FinancialAct act;
        private BigDecimal saved;

        private Key key;

        public State(FinancialAct act) {
            this.act = act;
            saved = act.isNew() ? BigDecimal.ZERO : getCurrent();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof State && ((State) obj).act == act;
        }

        @Override
        public int hashCode() {
            return act.hashCode();
        }

        public BigDecimal getSaved() {
            return saved;
        }

        public BigDecimal getQuantity() {
            return getCurrent().subtract(saved);
        }

        public BigDecimal getCurrent() {
            BigDecimal current = act.getQuantity();
            if (current == null) {
                current = BigDecimal.ZERO;
            }
            return current;
        }
    }

    /**
     * The stock for a particular product.
     */
    private static class Stock {

        /**
         * The persistent stock.
         */
        private BigDecimal stock;

        /**
         * The acts that use the stock.
         */
        private Set<State> states = new HashSet<>();

        /**
         * Constructs a {@link Stock}.
         *
         * @param stock the persistent quantity
         */
        public Stock(BigDecimal stock) {
            this.stock = stock;
        }

        /**
         * Adds an act.
         *
         * @param state the act state
         */
        public void add(State state) {
            states.add(state);
        }

        /**
         * Removes an act.
         *
         * @param state the act state
         */
        public void remove(State state) {
            stock = stock.add(state.getSaved());
            states.remove(state);
        }

        /**
         * Returns the stock, excluding any uncommitted changes.
         *
         * @return the stock
         */
        public BigDecimal getStock() {
            return stock;
        }

        /**
         * Returns the available stock.
         * <p/>
         * This is the stock minus uncommitted changes
         *
         * @return the available stock
         */
        public BigDecimal getAvailableStock() {
            BigDecimal result = stock;
            for (State state : states) {
                result = result.subtract(state.getQuantity());
            }
            return result;
        }
    }
}
