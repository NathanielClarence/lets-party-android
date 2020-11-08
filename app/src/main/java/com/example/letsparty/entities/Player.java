package com.example.letsparty.entities;

import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    private String id;
    private String nickname;
    private String token;
    private long score;

    public Player(String id, String nickname,String token) {
        this.id = id;
        this.nickname = nickname;
        this.token = token;
        this.score = 0;
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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id.equals(player.id) &&
                nickname.equals(player.nickname) &&
                token.equals(player.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, token);
    }

    public long getScore()
    {
        return score;
    }

    public void setScore(long score)
    {
        this.score = score;
    }
}
