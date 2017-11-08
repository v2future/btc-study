/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.utils;

import com.google.common.base.Strings;
import net.bither.languages.Languages;


/**
 * <p>Utilities to provide the following to application:</p>
 * <ul>
 * <li>Decorating text with HTML</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class HtmlUtils {

    /**
     * Utilities have private constructor
     */
    private HtmlUtils() {
    }


    /**
     * @param lines The lines to wrap in HTML
     * @return A single block of HTML that provides appropriate text alignment (LTR or RTL) and line breaks for the locale
     */
    public static String localiseWithLineBreaks(String[] lines) {

        final StringBuilder sb;

        if (Languages.isLeftToRight()) {
            sb = new StringBuilder("<html><body style='width: 100%'><div align=left>");
        } else {
            sb = new StringBuilder("<html><body style='width: 100%'><div align=right>");
        }

        // Wrap in paragraphs to ensure word wrap
        boolean first = true;
        for (String line : lines) {
            if (!first) {
                sb.append("<br>");
            }
            sb.append("<p>")
                    .append(line)
                    .append("</p>");
            first = false;
        }
        sb.append("</div></body></html>");

        return sb.toString();
    }

    /**
     * @param lines The lines to wrap in HTML
     * @return A single block of HTML that provides centered text alignment and line breaks for the locale
     */
    public static String localiseWithCenteredLinedBreaks(String[] lines) {

        final StringBuilder sb = new StringBuilder("<html><body style='width: 100%'><div align=center>");

        // Wrap in paragraphs to ensure word wrap
        boolean first = true;
        for (String line : lines) {
            if (!first) {
                sb.append("<br>");
            }
            sb.append("<p>")
                    .append(line)
                    .append("</p>");
            first = false;
        }
        sb.append("</div></body></html>");

        return sb.toString();
    }

    /**
     * @param fragment   The text fragment to use as the basis for emboldened text
     * @param sourceText The source text containing the fragment
     * @return The source text with HTML markup to embolden the matching fragments preserving the source case
     */
    public static String applyBoldFragments(String fragment, String sourceText) {

        if (Strings.isNullOrEmpty(fragment) || Strings.isNullOrEmpty(sourceText)) {
            return "<html>" + sourceText + "</html>";
        }

        String lowerFragment = fragment.toLowerCase();
        String lowerSource = sourceText.toLowerCase();

        // Find the match locations within the source text
        int sourceIndex = 0;
        int matchIndex;
        StringBuilder sb = new StringBuilder("<html>");
        do {

            // Match using case-insensitivity
            matchIndex = lowerSource.indexOf(lowerFragment, sourceIndex);

            if (matchIndex > -1) {

                // Decorate the original source text to preserve case
                sb.append(sourceText.substring(sourceIndex, matchIndex))
                        .append("<b>")
                        .append(sourceText.substring(matchIndex, matchIndex + fragment.length()))
                        .append("</b>");

                sourceIndex = matchIndex + fragment.length();
            }

        } while (matchIndex > -1);

        sb.append(sourceText.substring(sourceIndex));
        sb.append("</html>");

        return sb.toString();
    }

}
