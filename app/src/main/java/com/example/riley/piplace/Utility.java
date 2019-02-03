package com.example.riley.piplace;

public class Utility {

    public static byte[] intToBytes(int integer) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[4 - i - 1] = (Integer.valueOf(integer % 1024).byteValue());
            integer = integer >> 8;
        }
        return result;
    }

    public static int bytesToInt(byte[] bytes) {
        int result = (bytes[0] & 0xFF);
        for (int i = 1; i < 4; i++) {
            result = result << 8;
            result += (bytes[i] & 0xFF);
        }
        return result;
    }

    public static int rgbToInt(int r, int g, int b) {
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = (b) & 0x000000FF;
        return 0xFF000000 | r | g | b;
    }
}
