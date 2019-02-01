package com.example.riley.piplace.Server;

import android.graphics.Bitmap;

import java.util.concurrent.locks.ReentrantLock;

public class LockedBitmap {
    private static ReentrantLock lock = new ReentrantLock();
    private static Bitmap bitmap;

    public static void setBitmap(Bitmap bm) {
        bitmap = bm;
    }

    public static Bitmap get() {
        lock.lock();
        return bitmap;
    }

    public static void release() {
        lock.unlock();
    }

}
