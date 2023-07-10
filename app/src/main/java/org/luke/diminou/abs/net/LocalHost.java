package org.luke.diminou.abs.net;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.SocketConsumer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class LocalHost {
    private static ServerSocket server;
    private static SocketConsumer onConnected;
    private static boolean hosting = false;
    private static Thread thread;

    public static void host() {
        if(hosting) return;
        thread = new Thread(() -> {
            try {
                server = new ServerSocket(Local.PORT);
                while (hosting) {
                    Socket socket = server.accept();
                    if(onConnected != null)
                        onConnected.accept(socket);
                }
            }catch(SocketException x) {
                //IGNORE
            }catch (Exception e) {
                ErrorHandler.handle(e, "hosting party");
            }
        }, "hosting_thread");
        hosting = true;
        thread.start();
    }

    public static void setOnConnected(SocketConsumer onConnected) {
        LocalHost.onConnected = onConnected;
    }

    public static void stop() {
        if(!hosting) return;
        hosting = false;
        try {
            server.close();
        } catch (IOException e) {
            ErrorHandler.handle(e, "closing host");
        }
        thread.interrupt();
    }
}
