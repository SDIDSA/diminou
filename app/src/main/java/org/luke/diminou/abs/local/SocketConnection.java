package org.luke.diminou.abs.local;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Threaded;
import org.luke.diminou.abs.utils.functional.StringConsumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class SocketConnection {
    private final HashMap<String, StringConsumer> listeners = new HashMap<>();

    private DataInputStream din;
    private DataOutputStream dout;

    private final Thread thread;

    private final Socket socket;

    private Runnable onError;

    private final Semaphore readMutex = new Semaphore(1);
    public SocketConnection(Socket socket) {
        this.socket = socket;
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            ErrorHandler.handle(e, "initializing socket connection");
        }

        thread = new Thread(() -> {
            try {
                String line;
                while (!Thread.currentThread().isInterrupted() && (line = din.readUTF()) != null ) {
                    readMutex.acquireUninterruptibly();
                    JSONObject obj = new JSONObject(line);
                    String action = obj.getString("action");
                    if (listeners.containsKey(action)) {
                        Objects.requireNonNull(listeners.get(action)).accept(obj.getString("data"));
                    }
                    readMutex.release();
                }
            } catch (Exception e) {
                ErrorHandler.handle(e, "listening to socket");
                try {
                    socket.close();
                } catch (IOException x) {
                    ErrorHandler.handle(x, "closing socket");
                }
                if(onError != null)
                    onError.run();
            }
        }, "socket_connection_thread");
    }

    public void setOnError(Runnable onError) {
        this.onError = onError;
    }

    public boolean isRunning() {
        emit("test", "");
        Threaded.sleep(100);
        return !socket.isClosed();
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {
            socket.close();
            socket.setReuseAddress(true);
        } catch (IOException e) {
            ErrorHandler.handle(e, "stopping socket");
        }
    }

    public void on(String action, StringConsumer listener) {
        listeners.put(action, listener);
    }

    private final Semaphore emitMutex = new Semaphore(1);
    public synchronized void emit(String action, String data) {
        Threaded.runBack(() -> {
            emitMutex.acquireUninterruptibly();
            JSONObject obj = new JSONObject();
            try {
                obj.put("action", action);
                obj.put("data", data);

                String toSend = obj + "\n";
                dout.writeUTF(toSend);
                dout.flush();
                emitMutex.release();
            }catch(JSONException | IOException x) {
                ErrorHandler.handle(x, "emitting action " + action);
                if(onError != null) onError.run();
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorHandler.handle(e, "closing socket");
                }
            }
        });
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public void emit(String action, JSONObject data) {
        emit(action, data.toString());
    }

    public void emit(String action, int value) {
        emit(action, String.valueOf(value));
    }
}
