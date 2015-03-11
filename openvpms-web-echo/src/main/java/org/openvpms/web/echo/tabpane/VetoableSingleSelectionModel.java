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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.tabpane;

import echopointng.model.DefaultSingleSelectionModel;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.Vetoable;

/**
 * A {@code SingleSelectionModel} that allows selection to be vetoed by an {@link VetoListener}.
 *
 * @author Tim Anderson
 */
public class VetoableSingleSelectionModel extends DefaultSingleSelectionModel {

    /**
     * The listener.
     */
    private VetoListener listener;

    /**
     * Registers the veto listener.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setVetoListener(VetoListener listener) {
        this.listener = listener;

    }

    /**
     * Sets the selected index. If a {@link VetoListener} is registered, this may choose to veto the change.
     */
    @Override
    public void setSelectedIndex(int index) {
        if (listener != null) {
            listener.onVeto(new Change(getSelectedIndex(), index));
        } else {
            super.setSelectedIndex(index);
        }
    }

    public class Change implements Vetoable {

        private final int oldIndex;

        private final int newIndex;

        public Change(int oldIndex, int newIndex) {
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }


        /**
         * Returns the previous selection index.
         *
         * @return the previous selected index, or {@code -1} to indicate no selection
         */
        public int getOldIndex() {
            return oldIndex;
        }

        /**
         * Returns the new selection index.
         *
         * @return the new selection index
         */
        public int getNewIndex() {
            return newIndex;
        }

        /**
         * Indicates whether the action should be vetoed or not.
         * <p/>
         * If the action is vetoed, the {@link #getOldIndex()} will be the selected index. <br/>
         * If the action is allowed, the {@link #getNewIndex()} will be the selected index.
         *
         * @param veto if {@code true}, veto the action, otherwise allow it
         */
        @Override
        public void veto(boolean veto) {
            if (!veto) {
                VetoableSingleSelectionModel.super.setSelectedIndex(newIndex);
            }
        }
    }
}
