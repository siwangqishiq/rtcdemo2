package panyi.xyz.rtcdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements ClientWebSocket.OnMessageCallback {
    private static final String TAG = "MainActivity";

    private ClientWebSocket mWebSocketClient;

    private EditText mInputEditText;
    private Button mSendBtn;
    private TextView mMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initWebSocket();
    }

    private void initView(){
        mInputEditText = findViewById(R.id.input_ed);
        mSendBtn = findViewById(R.id.send_btn);
        mMessageText = findViewById(R.id.content_text);

        mSendBtn.setOnClickListener((v)->{
            onClickSendBtn();
        });
    }

    public void onClickSendBtn(){
        final String inputContent = mInputEditText.getText().toString().trim();
        Log.i(TAG , "send content : " + inputContent);
        if(mWebSocketClient != null){
            mWebSocketClient.sendMessage(inputContent);
        }
    }

    private void initWebSocket(){
        try {
            mWebSocketClient = new ClientWebSocket();
            mWebSocketClient.setMessageCallback(this);
            mWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        mMessageText.setText(message);
    }

    @Override
    protected void onDestroy() {
        if(mWebSocketClient != null){
            mWebSocketClient.close();
        }
        super.onDestroy();
    }

    private void webrtcInit(){
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101" , audioSource );

        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);

        VideoCapturer videoCapturer = createCameraCapturer();
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        SurfaceViewRenderer localView = findViewById(R.id.localView);
        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("101", videoSource);
        // display in localView
        videoTrack.addSink(localView);
    }

    private VideoCapturer createCameraCapturer(){
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

}