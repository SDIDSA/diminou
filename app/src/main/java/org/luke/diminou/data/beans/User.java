package org.luke.diminou.data.beans;

import org.json.JSONObject;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.property.Property;

public class User extends Bean {

    public static void refresh(int id) {
        Bean.refresh(User.class, id);
    }

    public static void getForId(int id, ObjectConsumer<User> onUser) {
        Bean.getForId(User.class, id, onUser);
    }

    public static User getForIdSync(int id) {
        return Bean.getForIdSync(User.class, id);
    }

    private final Property<Integer> points;
    private final Property<Integer> id;
    private final Property<Integer> coins;
    private final Property<String> password;
    private final Property<String> avatar;
    private final Property<String> username;
    private final Property<String> email;

    private final Property<String> friend;

    private User(JSONObject obj) {
        points = new Property<>();
        id = new Property<>();
        coins = new Property<>();
        password = new Property<>();
        avatar = new Property<>();
        username = new Property<>();
        email = new Property<>();
        friend = new Property<>();
        init(obj);
    }

    public Property<Integer> pointsProperty() {
        return points;
    }

    public Integer getPoints() {
        return points.get();
    }

    public void setPoints(Integer val) {
        points.set(val);
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

    public Property<Integer> coinsProperty() {
        return coins;
    }

    public Integer getCoins() {
        return coins.get();
    }

    public void setCoins(Integer val) {
        coins.set(val);
    }

    public Property<String> passwordProperty() {
        return password;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String val) {
        password.set(val);
    }

    public Property<String> avatarProperty() {
        return avatar;
    }

    public String getAvatar() {
        return avatar.get();
    }

    public void setAvatar(String val) {
        avatar.set(val);
    }

    public Property<String> usernameProperty() {
        return username;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String val) {
        username.set(val);
    }

    public Property<String> emailProperty() {
        return email;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String val) {
        email.set(val);
    }

    public Property<String> friendProperty() {
        return friend;
    }

    public String getFriend() {
        return friend.get();
    }

    public void setFriend(String val) {
        friend.set(val);
    }

    public boolean isSelf() {
        return friend.get().equals("self");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {"
                + "\tpoints : " + points.get()
                + "\tid : " + id.get()
                + "\tcoins : " + coins.get()
                + "\tpassword : " + password.get()
                + "\tavatar : " + avatar.get()
                + "\tusername : " + username.get()
                + "\temail : " + email.get()
                + "}";
    }
}