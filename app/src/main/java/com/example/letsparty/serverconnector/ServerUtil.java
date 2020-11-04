package com.example.letsparty.serverconnector;

import android.content.Context;

import com.google.firebase.functions.FirebaseFunctions;

public final class ServerUtil {
    static ServerConnector serverConnector = null;
    public static ServerConnector getServerConnector(Context context){
        if (serverConnector == null){
            //FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
            //serverConnector = new FirebaseServerConnector(mFunctions);
            serverConnector = new StubServerConnector(context);
        }
        return serverConnector;
    }
}
