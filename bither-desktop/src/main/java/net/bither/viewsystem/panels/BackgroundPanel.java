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

package net.bither.viewsystem.panels;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.bither.viewsystem.components.ImageDecorator;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {

    /**
     * Image will be scaled to fill the panel
     */
    public static final int SCALED = 0;
    /**
     * Image will be tiled to fill the background
     */
    public static final int TILED = 1;
    /**
     * Image will be rendered as is (see alignment for positioning)
     */
    public static final int ACTUAL = 2;

    private Optional<Paint> painter = Optional.absent();
    private Optional<Image> image = Optional.absent();

    private int style = ACTUAL;

    private float alignmentX = 0.5f;
    private float alignmentY = 0.55f;

    private float alpha = 1.0f;

    private Composite originalComposite;

    /**
     * <p>Set image as the background with the SCALED style</p>
     *
     * @param image The image
     */
    public BackgroundPanel(Image image) {

        this(image, SCALED);

    }

    /**
     * <p>Set image as the background with the specified style</p>
     *
     * @param image The image
     * @param style The style to use
     */
    public BackgroundPanel(Image image, int style) {

        this(image, style, 0.5f, 0.5f);

    }

    /**
     * <p>Set image as the background with the specified style and alignment</p>
     * <p>By default the image will be centered in the panel</p>
     *
     * @param image      The image
     * @param style      The style to use
     * @param alignmentX The horizontal alignment (0.0 to 1.0 representing left to right)
     * @param alignmentY The vertical alignment (0.0 to 1.0 representing top to bottom)
     */
    public BackgroundPanel(Image image, int style, float alignmentX, float alignmentY) {

        setImage(image);
        setStyle(style);

        setImageAlignmentX(alignmentX);
        setImageAlignmentY(alignmentY);

        setLayout(new BorderLayout());

        this.applyComponentOrientation(this.getComponentOrientation());

    }

    /**
     * <p>Use the Paint API to paint a solid background with no image</p>
     *
     * @param painter The painter
     */
    public BackgroundPanel(Paint painter) {

        setPaint(painter);
        setLayout(new BorderLayout());

    }

    @Override
    public Dimension getPreferredSize() {

        if (image.isPresent()) {
            return new Dimension(image.get().getWidth(null), image.get().getHeight(null));
        } else {
            return super.getPreferredSize();
        }

    }

    /**
     * <p>Custom painting code for drawing a SCALED image as the background</p>
     */
    private void drawScaled(Graphics g) {

        Preconditions.checkState(image.isPresent(), "'image' must be present");

        Graphics2D g2 = (Graphics2D) g;

        Dimension d = getSize();

        applyAlphaComposite(g2);

        g2.drawImage(image.get(), 0, 0, d.width, d.height, null);

        removeAlphaComposite(g2);
    }

    /**
     * <p>Custom painting code for drawing TILED images as the background</p>
     */
    private void drawTiled(Graphics g) {

        Preconditions.checkState(image.isPresent(), "'image' must be present");

        Graphics2D g2 = (Graphics2D) g;

        Dimension d = getSize();

        int width = image.get().getWidth(null);
        int height = image.get().getHeight(null);

        for (int x = 0; x < d.width; x += width) {
            for (int y = 0; y < d.height; y += height) {

                applyAlphaComposite(g2);

                g2.drawImage(image.get(), x, y, null, null);

                removeAlphaComposite(g2);
            }
        }

    }

    /**
     * <p>Custom painting code for drawing the ACTUAL image as the background.</p>
     * <p>The image is positioned in the panel based on the horizontal and
     * vertical alignments specified.</p>
     */
    private void drawActual(Graphics g) {

        Preconditions.checkState(image.isPresent(), "'image' must be present");

        Graphics2D g2 = (Graphics2D) g;

        Dimension d = getSize();
        Insets insets = getInsets();

        int width = d.width - insets.left - insets.right;
        int height = d.height - insets.top - insets.left;

        // Handle left to right orientation
        float x;
        if (this.getComponentOrientation().isLeftToRight()) {
            x = (width - image.get().getWidth(null)) * alignmentX;
        } else {
            //x = (width - image.get().getWidth(null)) * alignmentX;
            x = (width - image.get().getWidth(null)) * (1 - alignmentX);
        }
        float y = (height - image.get().getHeight(null)) * alignmentY;

        applyAlphaComposite(g2);

        g2.drawImage(image.get(), (int) x + insets.left, (int) y + insets.top, this);

        removeAlphaComposite(g2);
    }

    /**
     * <p>Apply the alpha composite to the given graphics context</p>
     *
     * @param g2 The graphics context
     */
    private void applyAlphaComposite(Graphics2D g2) {

        originalComposite = g2.getComposite();

        int rule = AlphaComposite.SRC_OVER;
        Composite alphaComposite = AlphaComposite.getInstance(rule, alpha);
        g2.setComposite(alphaComposite);

    }

    /**
     * <p>Remove the alpha composite from the given graphics context</p>
     *
     * @param g2 The graphics context
     */
    private void removeAlphaComposite(Graphics2D g2) {

        g2.setComposite(originalComposite);

    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // Use the painter for the background
        if (painter.isPresent()) {

            Dimension d = getSize();
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHints(ImageDecorator.smoothRenderingHints());
            g2.setPaint(painter.get());
            g2.fill(new Rectangle(0, 0, d.width, d.height));

        }

        // Draw the image
        if (image == null) return;

        switch (style) {
            case SCALED:
                drawScaled(g);
                break;

            case TILED:
                drawTiled(g);
                break;

            case ACTUAL:
                drawActual(g);
                break;

            default:
                drawActual(g);
        }
    }

    /**
     * <p>Set the image used as the background</p>
     */
    public void setImage(Image image) {

        this.image = Optional.of(image);

        repaint();

    }

    /**
     * <p>Set the style used to paint the background image</p>
     */
    public void setStyle(int style) {

        this.style = style;

        repaint();
    }

    /**
     * <p>Set the Paint object used to paint the background</p>
     */
    public void setPaint(Paint painter) {

        this.painter = Optional.of(painter);

        repaint();
    }

    /**
     * @param alignmentX The horizontal alignment (0.0 to 1.0 representing left to right) in ACTUAL style
     */
    public void setImageAlignmentX(float alignmentX) {

        this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;

        repaint();
    }

    /**
     * @param alignmentY The vertical alignment (0.0 to 1.0 representing top to bottom) in ACTUAL style
     */
    public void setImageAlignmentY(float alignmentY) {

        this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;

        repaint();
    }

    /**
     * @param alpha The transparency 0.0 (transparent) to 1.0 (opaque)
     */
    public void setAlpha(float alpha) {

        this.alpha = alpha > 1.0f ? 1.0f : alpha < 0.0f ? 0.0f : alpha;

        repaint();
    }

}