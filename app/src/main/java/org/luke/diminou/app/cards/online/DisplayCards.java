package org.luke.diminou.app.cards.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.beans.User;

public class DisplayCards extends HBox {
    private final PlayerCard[] cards = new PlayerCard[4];

    public DisplayCards(App owner, boolean host) {
        super(owner);

        setClipChildren(false);
        setClipToPadding(false);
        setClipToOutline(false);

        for(int i = 0; i < 4; i++) {
            cards[i] = new PlayerCard(owner, host, i);
            cards[i].setHolder(this);
            addView(cards[i]);
            if(i < 3) {
                addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
            }
        }
    }

    public PlayerCard getLast() {
        for(PlayerCard card : cards) {
            if(!card.isLoaded()) {
                return card;
            }
        }
        return null;
    }

    public void swap(int a, int b) {
        cards[a].swap(cards[b]);
    }

    public void fix() {
        for(int i = 0; i < 4; i++) {
            PlayerCard card1 = cards[i];
            if(!card1.isLoaded()) {
                for(int j = i + 1; j < 4; j++) {
                    PlayerCard card2 = cards[j];
                    if(card2.isLoaded()) {
                        card1.loadPlayer(
                                card2.getUser()
                        );
                        card2.unloadPlayer();
                        break;
                    }
                }
            }
        }
    }

    public void unloadAll() {
        for(PlayerCard card : cards) {
            card.unloadPlayer();
        }
    }

    public void unloadPlayer(User user) {
        for(PlayerCard card : cards) {
            if(card.getUser() == user) {
                Platform.runLater(card::unloadPlayer);
            }
        }
    }

    public int size() {
        int size = 0;
        for(PlayerCard card : cards) {
            if(card.isLoaded()) {
                size++;
            }
        }
        return size;
    }

    public PlayerCard getAt(int i) {
        return cards[i];
    }

    public void forEach(ObjectConsumer<PlayerCard> o) {
        for(PlayerCard card : cards) {
            try {
                o.accept(card);
            } catch (Exception e) {
                ErrorHandler.handle(e, "running cards foreach");
            }
        }
    }
}
