package org.luke.diminou.app.pages.game.offline.piece;

import org.luke.diminou.app.pages.game.piece.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class OfflineStock {
    private final ArrayList<Piece> stock;
    private final Semaphore stockMutex = new Semaphore(1);

    public OfflineStock() {
        stock = Piece.pack();
    }

    public Piece getOne() {
        stockMutex.acquireUninterruptibly();
        Piece toReturn = stock.get(0);
        stock.remove(0);
        stockMutex.release();

        return toReturn;
    }

    public List<Piece> deal() {
        stockMutex.acquireUninterruptibly();
        ArrayList<Piece> res = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            res.add(stock.get(i));
        }
        stock.removeAll(res);
        stockMutex.release();
        return res;
    }

    public boolean isEmpty() {
        stockMutex.acquireUninterruptibly();
        stockMutex.release();
        return stock.isEmpty();
    }
}
