package panyi.xyz.rtcdemo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLParameters;

public class ClientWebSocket extends WebSocketClient  {
    public static final String TAG = "WebSocketClient";

    private Handler handler = new Handler(Looper.getMainLooper());

    public interface OnMessageCallback{
        void onMessage(String message);
    }

    public void setMessageCallback(OnMessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    private OnMessageCallback messageCallback;

    public ClientWebSocket() throws URISyntaxException {
//        super(new URI("wss://voiptest.netease.im/voip/sipproxy") , new Draft_6455());
        //ws://10.242.142.129:9999/signal
        super(URI.create("ws://10.242.142.129:9999/signal") , new Draft_6455());
//        super(URI.create("ws://49.234.18.41:8866") , new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG , "onOpen ");
    }

    @Override
    public void onMessage(final String message) {
        Log.i(TAG , "on Message " + message);

        if(messageCallback != null){
            handler.post(()->{
                messageCallback.onMessage(message);
            });
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG , "onClose " +"  reason : " + reason +"  remote : " + remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.i(TAG , "onError " + ex);
    }

    public void sendMessage(final String content){
        send(content);
    }
}
