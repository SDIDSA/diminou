package org.luke.diminou.app.cards;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.local.SocketConnection;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                                card2.getUsername(),
                                card2.getAvatar(),
                                card2.getConnection(),
                                card2.getType()
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

    public void unloadPlayer(SocketConnection connection) {
        for(PlayerCard card : cards) {
            SocketConnection con = card.getConnection();
            if(con != null &&
                    Objects.equals(
                            con.getIp(),
                            connection.getIp())) {
                Platform.runLater(card::unloadPlayer);
            }
        }
    }

    public void unloadExact(SocketConnection connection) {
        for(PlayerCard card : cards) {
            SocketConnection con = card.getConnection();
            if(con == connection) {
                card.unloadPlayer();
                return;
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

    public List<SocketConnection> broadcast() {
        ArrayList<SocketConnection> res = new ArrayList<>();
        for(PlayerCard card : cards) {
            if(card.isLoaded()) {
                if(card.getConnection() != null)
                    res.add(card.getConnection());
            }else {
                return res;
            }
        }
        return res;
    }

    public PlayerCard getAt(int i) {
        return cards[i];
    }

    public String serialize() {
        JSONArray arr = new JSONArray();
        try {
            for(int i = 0; i < 4; i++) {
                JSONObject obj = new JSONObject();
                obj.put("order", i);
                if(cards[i].isLoaded()) {
                    obj.put("username", cards[i].getUsername());
                    obj.put("avatar", cards[i].getAvatar());
                }else {
                    obj.put("empty", true);
                }
                arr.put(obj);
            }
        }catch(JSONException x) {
            ErrorHandler.handle(x, "serializing cards");
        }
        return arr.toString();
    }

    private static final String[] botNames = new String[] {"William","James","Emma","Noah","Oliver","Benjamin","Thomas","Jack","Olivia","Harper","Alexander","Evelyn","Anna","Amelia","Sophia","Lucas","Liam","Jacob","Isabella","Theodore","Abigail","Christopher","Nathan","Logan"};

    public String botName() {
        String name = randomBotName();
        while(botNameUsed(name)) {
            name = randomBotName();
        }
        return name;
    }

    private static String randomBotName() {
        return botNames[(int) (Math.random() * botNames.length)];
    }

    private boolean botNameUsed(String name) {
        for(PlayerCard card : cards) {
            if(card.isLoaded() &&
                    card.getType() == PlayerCard.Type.BOT &&
                    card.getUsername().equals(name)) {
                return true;
            }
        }
        return false;
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
