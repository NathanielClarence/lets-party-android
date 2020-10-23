package com.example.letsparty.serverconnector;

import com.google.firebase.functions.FirebaseFunctions;

public final class ServerUtil {
    static ServerConnector serverConnector = null;
    public static ServerConnector getServerConnector(){
        if (serverConnector == null){
            FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
            serverConnector = new FirebaseServerConnector(mFunctions);
            //serverConnector = new StubServerConnector();
        }
        return serverConnector;
    }
}
