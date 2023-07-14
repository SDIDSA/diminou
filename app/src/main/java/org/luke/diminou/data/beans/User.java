package org.luke.diminou.data.beans;

import org.json.JSONObject;
import org.luke.diminou.data.property.Property;

public class User extends Bean {
    public static final String USER_ID = "user_id";

    private final Property<Integer> id;
    private final Property<Integer> points;
    private final Property<Integer> coins;
    private final Property<String> username;
    private final Property<String> email;
    private final Property<String> password;
    private final Property<String> avatar;

    public User(JSONObject obj) {
        id = new Property<>();
        points = new Property<>();
        coins = new Property<>();
        username = new Property<>();
        email = new Property<>();
        password = new Property<>();
        avatar = new Property<>();
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

    public Property<Integer> pointsProperty() {
        return points;
    }

    public Integer getPoints() {
        return points.get();
    }

    public void setPoints(Integer val) {
        points.set(val);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {"
                + "\tusername : " + username.get()
                + "\temail : " + email.get()
                + "\tpassword : " + password.get()
                + "\tavatar : " + avatar.get()
                + "}";
    }
}