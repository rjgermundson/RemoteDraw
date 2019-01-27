package com.example.riley.piplace.Messages.Lines;

import android.util.Pair;

import java.util.Set;

/**
 * This class represents a stroke by the user that will be sent from the server
 */
public class Line {
    private int color;
    private Set<Pair> pixels;

    public Line(int color) {
        this.color = color;
    }

    public Line(int color, Set<Pair> pixels) {
        this.color = color;
        this.pixels = pixels;
    }

    /**
     * Set the color of this line
     * @param color The color to set this line to
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Returns the color of this line
     * @return The color of this line
     */
    public int getColor() {
        return color;
    }

    /**
     * Adds the given pixel pair to the list
     * @param pixel The pixel to add
     * @return True if added successfully
     *         False otherwise
     */
    public boolean addPixel(Pair pixel) {
        return pixels.add(pixel);
    }

    /**
     * Returns the number of pixels that are taken up by this line
     * @return The number of pixels in this line
     */
    public int size() {
        return pixels.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Pair p : pixels) {
            builder.append(p.first);
            builder.append(" ");
            builder.append(p.second);
            builder.append(" ");
        }
        return builder.toString();
    }
}
