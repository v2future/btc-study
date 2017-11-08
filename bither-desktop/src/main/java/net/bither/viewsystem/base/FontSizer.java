/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.base;

import java.awt.*;

public enum FontSizer {
    INSTANCE;


    private Font adjustedDefaultFont;

    public void initialise() {

        adjustedDefaultFont = createAdjustedDefaultFont();
    }

    private Font createAdjustedDefaultFont() {
        String fontSizeString = null;

        int unadjustedFontSize = ColorAndFontConstants.BITHER_DEFAULT_FONT_SIZE;

        if (fontSizeString != null && !"".equals(fontSizeString)) {
            try {
                unadjustedFontSize = Integer.parseInt(fontSizeString);
            } catch (NumberFormatException nfe) {
                // use default
            }
        }

        String fontStyleString = null;
        int fontStyle = ColorAndFontConstants.BITHER_DEFAULT_FONT_STYLE;

        try {
            fontStyle = Integer.parseInt(fontStyleString);
        } catch (NumberFormatException nfe) {
            // use default
        }

        String fontName = null;
        if (fontName == null || "".equals(fontName)) {
            fontName = ColorAndFontConstants.BITHER_DEFAULT_FONT_NAME;
        }

        return new Font(fontName, fontStyle, unadjustedFontSize);
    }

    public Font getAdjustedDefaultFont() {
        return adjustedDefaultFont;
    }

    /**
     * Get the required scaled font using the currently specified font size plus a delta
     *
     * @param delta Delta from default font, in point size
     */
    public Font getAdjustedDefaultFontWithDelta(int delta) {
        return adjustedDefaultFont.deriveFont(adjustedDefaultFont.getStyle(), adjustedDefaultFont.getSize() + delta);
    }
}