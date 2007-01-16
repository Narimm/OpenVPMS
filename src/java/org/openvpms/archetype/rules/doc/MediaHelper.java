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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.doc;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.*;

import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import java.math.BigDecimal;


/**
 * Helper for providing conversions from archetype paper sizes and print tray's
 * to their corresponding media types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MediaHelper {

    /**
     * Helper to convert a paper size to a {@link MediaSizeName}.
     *
     * @param sizeName the size name
     * @param width    the page width. Only applicable if size is 'CUSTOM'.
     * @param height   the page height. Only applicable if size is 'CUSTOM'.
     * @param units    the units. One of 'MM' or 'INCH'.
     * @return the media size, or <code>null</code> if none is defined
     * @throws DocumentException if any argument is invalid
     */
    public static MediaSizeName getMedia(String sizeName, BigDecimal width,
                                         BigDecimal height, String units) {
        MediaSizeName media;
        if (PaperSize.CUSTOM.name().equals(sizeName)) {
            media = getMedia(width, height, units);
        } else {
            media = PaperSize.getMediaSizeName(sizeName);
        }
        return media;
    }

    /**
     * Helper to convert a custom paper size to a {@link MediaSizeName}.
     *
     * @param width  the page width
     * @param height the page height
     * @param units  the units. One of 'MM' or 'INCH'.
     * @throws DocumentException if the paper size is invalid
     */
    public static MediaSizeName getMedia(BigDecimal width, BigDecimal height,
                                         String units) {
        int unitCode = Units.getUnits(units);

        try {
            return MediaSize.findMedia(width.floatValue(), height.floatValue(),
                                       unitCode);
        } catch (IllegalArgumentException exception) {
            throw new DocumentException(InvalidPaperSize,
                                        formatSize(width, height, units));
        }
    }

    /**
     * Returns the paper orientation given its code.
     *
     * @param code the orientation code
     * @return the orientation corresponding to <code>code</code>
     * @throws DocumentException if <code>code<code> is invalid
     */
    public static OrientationRequested getOrientation(String code) {
        return Orientation.getOrientation(code);
    }

    /**
     * Returns the media tray given its code.
     *
     * @param code the tray code
     * @return the media tray corresponding to <code>code</code>
     * @throws DocumentException if <code>code<code> is invalid
     */
    public static MediaTray getTray(String code) {
        return Tray.getTray(code);
    }

    /**
     * Formats the page size for error reporting purposes.
     *
     * @param width  the page width
     * @param height the page height
     * @param units  the units
     * @return a formatted string
     */
    private static String formatSize(BigDecimal width, BigDecimal height,
                                     String units) {
        return width.toString() + "x" + height.toString() + " " + units;
    }

    /**
     * Provides a mapping between supported paper sizes and
     * {@link MediaSizeName}.
     */
    private enum PaperSize {
        A4(MediaSizeName.ISO_A4),
        A5(MediaSizeName.ISO_A5),
        LETTER(MediaSizeName.NA_LETTER),
        CUSTOM(null);

        private PaperSize(MediaSizeName name) {
            mediaName = name;
        }

        public MediaSizeName getMediaSizeName() {
            return mediaName;
        }

        public static MediaSizeName getMediaSizeName(String name) {
            for (PaperSize size : values()) {
                if (size.name().equals(name)) {
                    return size.getMediaSizeName();
                }
            }
            throw new DocumentException(InvalidPaperSize, name);
        }

        private final MediaSizeName mediaName;
    }

    /**
     * Provides a mapping between paper size units and corresponding
     * values defined in {@link Size2DSyntax}.
     */
    private enum Units {
        MM(Size2DSyntax.MM),
        INCH(Size2DSyntax.INCH);

        private Units(int units) {
            this.units = units;
        }

        public int getUnits() {
            return units;
        }

        public static int getUnits(String units) {
            for (Units u : Units.values()) {
                if (u.name().equals(units)) {
                    return u.getUnits();
                }
            }
            throw new DocumentException(InvalidUnits, units);
        }

        private final int units;
    }

    /**
     * Provides a mapping between supported orientations and values defined in
     * @{link OrientationRequested}.
     */
    private enum Orientation {
        PORTRAIT(OrientationRequested.PORTRAIT),
        LANDSCAPE(OrientationRequested.LANDSCAPE);

        private Orientation(OrientationRequested orientation) {
            this.orientation = orientation;
        }

        public OrientationRequested getOrientation() {
            return orientation;
        }

        public static OrientationRequested getOrientation(String orientation) {
            for (Orientation o : Orientation.values()) {
                if (o.name().equals(orientation)) {
                    return o.getOrientation();
                }
            }
            throw new DocumentException(InvalidOrientation, orientation);
        }

        private final OrientationRequested orientation;
    }

    /**
     * Provides a mapping between supported media trays and values defined in
     * @{link MediaTray}.
     */
    private enum Tray {
        TOP(MediaTray.TOP),
        MIDDLE(MediaTray.MIDDLE),
        BOTTOM(MediaTray.BOTTOM),
        ENVELOPE(MediaTray.ENVELOPE),
        MANUAL(MediaTray.MANUAL),
        LARGE_CAPACITY(MediaTray.LARGE_CAPACITY),
        MAIN(MediaTray.MAIN),
        SIDE(MediaTray.SIDE);

        private Tray(MediaTray tray) {
            this.tray = tray;
        }

        public MediaTray getTray() {
            return tray;
        }

        public static MediaTray getTray(String name) {
            for (Tray tray : Tray.values()) {
                if (tray.name().equals(name)) {
                    return tray.getTray();
                }
            }
            throw new DocumentException(InvalidMediaTray, name);
        }

        private final MediaTray tray;
    }

}
