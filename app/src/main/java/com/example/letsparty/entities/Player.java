package com.example.letsparty.entities;

import java.io.Serializable;

public class Player implements Serializable {
    private String id;
    private String nickname;
    private String token;

    public Player(String id, String nickname,String token) {
        this.id = id;
        this.nickname = "Player-" + id;
        this.token = token;
    }

    public String getId(){
        return id;
    }

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }
    public String getToken(){
        return token;
    }
}
