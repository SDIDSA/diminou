package org.luke.diminou.abs.utils;

import android.os.Handler;
import android.os.Looper;

import org.luke.diminou.abs.utils.functional.BooleanSupplier;

public class Platform {

    public static void runLater(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static void runAfter(Runnable r, long after) {
        new Thread(() -> {
            try {
                Thread.sleep(after);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runLater(() -> {
                try {
                    r.run();
                }catch (Exception x) {
                    ErrorHandler.handle(x, "running later");
                }
            });
        }, "run_after_thread").start();
    }



    public static void waitWhile(BooleanSupplier condition) {
        while(condition.get()) {
            sleep(5);
        }
    }

    public static void waitWhile(BooleanSupplier condition, Runnable post) {
        new Thread(() -> {
            waitWhile(condition);
            Platform.runLater(post);
        }, "waiting_while_thread").start();
    }

    public static void sleep(long duration) {
        try {
            Thread.sleep(Math.max(duration, 0));
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
    }

    public static Thread runBack(Runnable action, Runnable post) {
        Thread t = new Thread(()-> {
            action.run();
            if(post != null) post.run();
        },"run_back_thread");
        t.start();
        return t;
    }

    public static Thread runBack(Runnable action) {
        return runBack(action, null);
    }
}
