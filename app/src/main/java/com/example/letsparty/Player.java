package com.example.letsparty;

import java.io.Serializable;

public class Player implements Serializable {
    private String id;
    private String nickname;

    public Player(String id) {
        this.id = id;
        this.nickname = "Player";
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
}
