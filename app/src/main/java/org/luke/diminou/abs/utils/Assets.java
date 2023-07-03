package org.luke.diminou.abs.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Assets {
    public static String readAsset(Context context, String path) {
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)))) {
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }catch(IOException x) {
            ErrorHandler.handle(x, "reading asset at ".concat(path));
            return null;
        }
    }
}
