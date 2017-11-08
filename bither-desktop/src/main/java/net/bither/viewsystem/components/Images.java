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

package net.bither.viewsystem.components;

import net.bither.BitherUI;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.viewsystem.themes.Themes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Utility to provide the following to UI:</p>
 * <ul>
 * <li>Provision of images</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class Images {

    private static Font imageFont = new JLabel().getFont().deriveFont(Font.PLAIN, BitherUI.BALANCE_TRANSACTION_NORMAL_FONT_SIZE);

    /**
     * Utilities have no public constructor
     */
    private Images() {
    }

    /**
     * @return A new "qr code" image icon that's nicer than the Font Awesome version
     */
    public static Icon newQRCodeIcon() {

        try {
            InputStream is = Images.class.getResourceAsStream("/assets/images/qrcode.png");

            // Transform the mask color into the current themed text
            BufferedImage qrCodePng = ImageDecorator.applyColor(
                    ImageIO.read(is),
                    Themes.currentTheme.buttonText()
            );

            return new ImageIcon(qrCodePng);

        } catch (IOException e) {
            throw new IllegalStateException("The QR code image is missing");
        }

    }

    /**
     * @return A new "logo" image icon
     */
    public static BufferedImage newLogoImage() {

        try {
            InputStream is = Images.class.getResourceAsStream("/assets/images/multibit128.png");

            // Transform the mask color into the current themed text
            return ImageIO.read(is);

        } catch (IOException e) {
            throw new IllegalStateException("The logo image is missing");
        }

    }

    /**
     * @param code The 2-letter language code (e.g. "EN") - will be uppercase
     * @return A new "language" image icon suitable for use in combo boxes etc
     */
    public static ImageIcon newLanguageCodeIcon(String code) {

        BufferedImage image = new BufferedImage(26, 20, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = image.createGraphics();

        // Fill the background
        g2.setColor(Themes.currentTheme.readOnlyComboBox());
        g2.fillRect(0, 0, 26, 20);

        // Write the language code (looks better with white lowercase)
        g2.setRenderingHints(ImageDecorator.smoothRenderingHints());
        g2.setColor(Color.WHITE);
        g2.setFont(imageFont);
        g2.drawString(code, 3, 16);

        g2.dispose();

        return new ImageIcon(image);

    }

    /**
     * @return A new "no network" contact image icon suitable for use in tables and labels
     */
    public static ImageIcon newNoNetworkContactImageIcon() {

        final Icon icon;

        icon = AwesomeDecorator.createIcon(
                AwesomeIcon.USER,
                Themes.currentTheme.fadedText(),
                BitherUI.LARGE_ICON_SIZE
        );

        return ImageDecorator.toImageIcon(icon);

    }


    public static ImageIcon newConfirmationIcon(int confirmationCount, boolean isCoinbase, int iconSize) {

        // The arc angle is the extent in degrees (e.g. 90 is a quarter of a circle)
        final int arcAngle;
        if (isCoinbase) {
            arcAngle = confirmationCount * 3 >= 360 ? 360 : confirmationCount * 3;
        } else {
            arcAngle = confirmationCount * 60 >= 360 ? 360 : confirmationCount * 60;
        }

        // Check for non-circular icon
        if (arcAngle >= 360) {
            return ImageDecorator.toImageIcon(AwesomeDecorator.createIcon(
                    AwesomeIcon.CHECK,
                    Themes.currentTheme.statusGreen(),
                    iconSize));
        }
        if (arcAngle < 0) {
            // Depth of -1 indicates a payment request has been paid
            // Note that he underlying transaction(s) may not have confirmed
            // but these are shown separately
            return ImageDecorator.toImageIcon(AwesomeDecorator.createIcon(
                    AwesomeIcon.CHECK,
                    Themes.currentTheme.statusGreen(),
                    iconSize));
        }

        // Have an icon size 20% bigger for the pie pieces for better visual effect
        iconSize = (int) (iconSize * 1.2);

        BufferedImage background = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = background.createGraphics();

        g2.setRenderingHints(ImageDecorator.smoothRenderingHints());

        // Ensure we start from "12 o'clock"
        int startAngle = 90;

        // Create a segment with a small inset
        g2.setColor(Themes.currentTheme.statusGreen());
        g2.fillArc(1, 1, iconSize - 2, iconSize - 2, startAngle, -arcAngle);

        // Add a border to the arc with the same inset
        g2.setColor(Themes.currentTheme.statusGreen().darker());
        g2.drawArc(1, 1, iconSize - 2, iconSize - 2, startAngle, -arcAngle);

        // Draw the interior border (allowing a single line for 0 confirmations)

        int center = (int) (iconSize * 0.5);
        int diameter = center - 1;

        // Draw vertical interior border
        g2.drawLine(center, center, center, 1);

        // Draw angled interior border
        int xFinish = (int) (center + diameter * Math.cos(Math.toRadians(90 - arcAngle)));
        int yFinish = (int) (center - diameter * Math.sin(Math.toRadians(90 - arcAngle)));

        g2.drawLine(center, center, xFinish, yFinish);

        g2.dispose();

        return ImageDecorator.toImageIcon(background);

    }

}