package com.example.riley.piplace.Messages.Lines;

import android.util.Pair;

import com.example.riley.piplace.Utility;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a stroke by the user that will be sent from the server
 */
public class Line {
    private static final int INTEGER_BYTE_COUNT = 4;
    private int id;
    private int color;
    private Set<Pair<Integer, Integer>> pixels;

    public Line(int color, int id) {
        this.id = id;
        this.color = color;
        this.pixels = new HashSet<>();
    }

    public Line(int color, int id, Set<Pair<Integer, Integer>> pixels) {
        this(color, id);
        this.pixels = pixels;
    }

    /**
     * Returns the id of the client that submitted this line
     * @return The id of the client that submitted this line
     */
    public int getID() {
        return id;
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
    public boolean addPixel(Pair<Integer, Integer> pixel) {
        return pixels.add(pixel);
    }

    /**
     * Returns the set of pixels this line covers
     * @return The set of pixels this line covers
     */
    public Set<Pair<Integer, Integer>> getPixels() {
        return pixels;
    }

    /**
     * Returns the number of pixels that are taken up by this line
     * @return The number of pixels in this line
     */
    public int size() {
        return pixels.size();
    }

    /**
     * Set this line to be the line between the two given points
     * @param px Starting x coordinate
     * @param py Starting y coordinate
     * @param x End x coordinate
     * @param y End y coordinate
     */
    public void setLine(double px, double py, int x, int y) {
        double hypotenuse = Math.sqrt(Math.pow((x - px), 2) + Math.pow((y - py), 2));
        if (Double.isNaN(hypotenuse)) {
            pixels.add(new Pair<>(x, y));
            return;
        }
        double dx = (x - px) / (hypotenuse);
        double dy = (y - py) / (hypotenuse);
        int currX;
        int currY;
        do {
            currX = ((int) Math.round(px));
            currY = ((int) Math.round(py));
            pixels.add(new Pair<>(currX, currY));
            if (currX != x) {
                px += dx;
            }
            if (currY != y) {
                py += dy;
            }
        } while (currX != x || currY != y);
    }

    public byte[] getBytes() {
        byte[] result = new byte[INTEGER_BYTE_COUNT * 2 + pixels.size() * INTEGER_BYTE_COUNT * 2];
        System.arraycopy(Utility.intToBytes(color), 0, result, 0, INTEGER_BYTE_COUNT);
        System.arraycopy(Utility.intToBytes(pixels.size()), 0, result, INTEGER_BYTE_COUNT, INTEGER_BYTE_COUNT);
        int count = 0;
        for (Pair<Integer, Integer> pixel : pixels) {
            System.arraycopy(Utility.intToBytes(pixel.first), 0, result, INTEGER_BYTE_COUNT * (2 + count), INTEGER_BYTE_COUNT);
            count++;
            System.arraycopy(Utility.intToBytes(pixel.second), 0, result, INTEGER_BYTE_COUNT * (2 + count), INTEGER_BYTE_COUNT);
            count++;
        }

        return result;
    }
}
