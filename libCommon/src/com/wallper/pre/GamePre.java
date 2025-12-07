package com.wallper.pre;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GamePre {
    private Preferences preferences;
    private GamePre(){
        this.preferences = Gdx.app.getPreferences("Wallpet");
    }
    private static GamePre gamePre;
    public static GamePre getInstance(){
        if (gamePre == null) {
            gamePre = new GamePre();
        }
        return gamePre;
    }

    public void saveWall(String value){
        preferences.putString("wallValue",value);
        preferences.flush();
    }

    public void savePet(String value){
        preferences.putString("petValue",value);
        preferences.flush();
    }

    public String getWall(){
        return preferences.getString("wallValue","");
    }

    public String getPet(){
        return preferences.getString("petValue","");
    }


}
