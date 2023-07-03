package org.luke.diminou.abs.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    public static void handle(Throwable throwable, String action) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String trace = throwable.getMessage() + " happend while " + action + "\n" + sw;
        Log.e(throwable.getClass().getSimpleName(), trace);

        Store.setLogs(Store.getLogs() + "\n----------------\n" + trace, null);
    }
}
