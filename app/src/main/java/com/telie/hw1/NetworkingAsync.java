package com.telie.hw1;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NetworkingAsync extends AsyncTask<URL, Void, String> {
    @Override
    protected String doInBackground(URL... inURLs) {
        String ret = "";
        try {
            for(URL u: inURLs) {
                URL reqUrl = u;
                HttpURLConnection con = (HttpURLConnection) reqUrl.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                if (con.getResponseCode() > 400) {
                    throw new IOException();
                }
                Scanner res = new Scanner(reqUrl.openStream());
                while (res.hasNext()) {
                    ret += res.nextLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return ret;
    }
}
