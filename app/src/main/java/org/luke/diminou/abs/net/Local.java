package org.luke.diminou.abs.net;

import android.os.Build;

import org.luke.diminou.abs.utils.ErrorHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class Local {
    public static final int PORT = 10203;

    public static List<String> getIpAddress() {
        return getMyIp()
                .stream()
                .map(add -> add.substring(0, add.lastIndexOf(".")))
                .collect(Collectors.toList());
    }

    public static ArrayList<String> getMyIp() {
        ArrayList<String> res = new ArrayList<>();
        if(Build.PRODUCT.toLowerCase().contains("sdk"))
            res.add("192.168.22.161");
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();
                    if (inetAddress.isSiteLocalAddress() && !Objects.requireNonNull(inetAddress.getHostAddress()).contains(":")) {
                        String add = inetAddress.getHostAddress();
                        if(!res.contains(add)) res.add(add);
                    }
                }
            }
        } catch (SocketException e) {
            ErrorHandler.handle(e, "getting ip");
        }
        return res;
    }

    public static List<String> scanNetwork() {
        ArrayList<String> res = new ArrayList<>();

        Semaphore mutex = new Semaphore(1);

        int threadCount = 64;
        int batchSize = 256 / threadCount;
        ArrayList<Thread> threads = new ArrayList<>();
        for(String ip : getIpAddress()) {
            for(int i = 0; i < threadCount; i++) {
                final int batch = i;
                Thread t = new Thread(() -> {
                    for(int j = 0; j < batchSize; j++) {
                        final int id = batch * batchSize + j;
                        String hit = ip + "." + id;
                        try {
                            InetAddress address = InetAddress.getByName(hit);
                            if(address.isReachable(400)) {
                                mutex.acquireUninterruptibly();
                                res.add(hit);
                                mutex.release();
                            }
                        } catch (IOException e) {
                            //IGNORE FAILURE
                        }
                    }
                }, "scanning_thread_" + batch*8 + ":"+(batch+1) * 8);
                threads.add(t);
            }
        }

        threads.forEach(Thread::start);
        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                break;
            }
        }

        return res;
    }
}
