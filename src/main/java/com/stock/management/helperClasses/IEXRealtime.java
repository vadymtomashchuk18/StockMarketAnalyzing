package com.stock.management.helperClasses;

/*
 * The MIT License
 *
 * Copyright 2017 Cerudite Techonologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *
 * @author Thomas Praill <tom@cerudite.com>
 */
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.simple.JSONObject;

public class IEXRealtime implements Runnable
{

    private static final int CONNECTION_TIMEOUT = 1000 * 5; // 5 seconds
    private static final int HEARTBEAT_INTERVAL = 1000 * 20; // 20 seconds
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String URI_REALTIME
            = "wss://realtime.intrinio.com:443/socket/websocket?vsn=1.0.0&token=";
    private static final String URL_AUTH = "https://realtime.intrinio.com/auth";

    private static boolean debug = false;

    private JSONObject heartbeat;
    private String username;
    private String password;
    private TextMessageHandler handler;
    private WebSocket socket;

    public IEXRealtime(
            String username,
            String password,
            boolean debug,
            TextMessageHandler handler)
    {
        this.username = username;
        this.password = password;
        this.handler = handler;
        setDebug(debug);

        heartbeat = new JSONObject();
        heartbeat.put("topic", "phoenix");
        heartbeat.put("event", "heartbeat");
        heartbeat.put("payload", "{}");
        heartbeat.put("ref", null);

        try {
            URI realtime = new URI(URI_REALTIME + getToken());

            socket = connect(realtime);
            new Thread(this).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run()
    {
        try {
            while (socket.isOpen()) {
                socket.sendText(heartbeat.toJSONString());
                Thread.sleep(HEARTBEAT_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void join(String[] tickers)
    {
        if (socket.isOpen()) {
            for (int i = 0; i < tickers.length; i++) {
                JSONObject join = new JSONObject();
                join.put("topic", "iex:securities:" + tickers[i]);
                join.put("event", "phx_join");
                join.put("payload", "{}");
                join.put("ref", null);

                socket.sendText(join.toJSONString());
            }
        }
    }

    public void leave(String[] tickers)
    {
        if (socket.isOpen()) {
            for (int i = 0; i < tickers.length; i++) {
                JSONObject leave = new JSONObject();
                leave.put("topic", "iex:securities:" + tickers[i]);
                leave.put("event", "phx_leave");
                leave.put("payload", "{}");
                leave.put("ref", null);

                socket.sendText(leave.toJSONString());
            }
        }
    }

    public void disconnect()
    {
        socket.disconnect();
    }

    public void setDebug(boolean bool)
    {
        this.debug = bool;
    }

    private String getToken() throws Exception
    {
        return sendIntrinioGetRequest(new URL(URL_AUTH));
    }

    private String sendIntrinioGetRequest(URL url) throws Exception
    {
        String credsB64 = Base64.encode(new String(username + ":" + password).getBytes());

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        //add headers
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Basic " + credsB64);

        debug("Sending 'GET' request to URL : " + url.toString());
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseStr = response.toString();

        debug(url.toString() + " GET returned: " + responseStr);
        return responseStr;
    }

    private WebSocket connect(URI host) throws Exception
    {
        if (host == null) {
            System.err.println("Host is null.");
            return null;
        } else {
            return new WebSocketFactory()
                    .setConnectionTimeout(CONNECTION_TIMEOUT)
                    .createSocket(host)
                    .addListener(new WebSocketAdapter()
                    {
                        public void onTextMessage(WebSocket websocket, String text) throws Exception
                        {
                            handler.handleMessage(text);
                        }
                    })
                    .connect();
        }
    }

    public static void debug(String str)
    {
        if (debug) {
            System.out.println(str);
        }
    }

    public interface TextMessageHandler
    {

        public void handleMessage(String msg);
    }

}
