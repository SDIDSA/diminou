package org.luke.diminou.data.beans;

import org.json.JSONObject;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.property.Property;

public class Friend extends Bean {

    public static void getForId(int id, ObjectConsumer<Friend> onFriend) {
        Bean.getForId(Friend.class, id, onFriend);
    }

    public static Friend getForIdSync(int id) {
        return Bean.getForIdSync(Friend.class, id);
    }

    private final Property<Integer> id;
    private final Property<Integer> sender;
    private final Property<Integer> receiver;
    private final Property<Boolean> accepted;

    private Friend(JSONObject obj) {
        id = new Property<>();
        sender = new Property<>();
        receiver = new Property<>();
        accepted = new Property<>();
        init(obj);
    }

    public Property<Integer> idProperty() {
        return id;
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer val) {
        id.set(val);
    }

    public Property<Integer> senderProperty() {
        return sender;
    }

    public Integer getSender() {
        return sender.get();
    }

    public void setSender(Integer val) {
        sender.set(val);
    }

    public Property<Integer> receiverProperty() {
        return receiver;
    }

    public Integer getReceiver() {
        return receiver.get();
    }

    public void setReceiver(Integer val) {
        receiver.set(val);
    }

    public Property<Boolean> acceptedProperty() {
        return accepted;
    }

    public Boolean isAccepted() {
        return accepted.get();
    }

    public void setAccepted(Boolean val) {
        accepted.set(val);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {"
                + "\tid : " + id.get()
                + "\tsender : " + sender.get()
                + "\treceiver : " + receiver.get()
                + "\taccepted : " + accepted.get()
                + "}";
    }
}