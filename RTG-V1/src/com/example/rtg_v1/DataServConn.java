package com.example.rtg_v1;

//http://www.marioalmeida.eu/2014/02/21/how-to-do-android-ipc-using-messenger-to-a-remote-service/

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.rtg_v1.MainActivity;

public class DataServConn {

	/** Command to the service to register client binder */
    static final int MSG_REGISTER = 1;
    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 2;
    static final int MSG_STOP_START = 3;
    static final int MSG_STOP_SERVICE = 4;
    static final int MSG_ADD_SOURCE = 5;

    /** Messenger for sending messages to the service. */
    Messenger mServiceMessenger = null;
    /** Messenger for receiving messages from the service. */
    Messenger mClientMessenger = null;

    /**
     * Target we publish for clients to send messages to IncomingHandler. Note
     * that calls to its binder are sequential!
     */
    private final IncomingHandler handler;

    /**
     * Handler thread to avoid running on the main thread (UI)
     */
    private final HandlerThread handlerThread;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /** Context of the activity from which this connector was launched */
    private Context mCtx;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {

        public IncomingHandler(HandlerThread thr) {
            super(thr.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SAY_HELLO:
                Toast.makeText(mCtx.getApplicationContext(),
                        "Client : Service said hello!", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);

            
            
            Log.i("RTG", String.format("onServiceConnected --> %s", className.getClassName()));
            
            
            
            
            // Now that we have the service messenger, lets send our messenger
            Message msg = Message.obtain(null, MSG_REGISTER, 0, 0);
            msg.replyTo = mClientMessenger;

            /*
             * In case we would want to send extra data, we could use Bundles:
             * Bundle b = new Bundle(); b.putString("key", "hello world");
             * msg.setData(b);
             */

            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
            mBound = false;
        }
    };

    public DataServConn(Context ctx) {
        mCtx = ctx;
        handlerThread = new HandlerThread("IPChandlerThread");
        handlerThread.start();
        handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);
    }

    /**
     * Method used for binding with the service
     */
    public boolean bindService() {
        /*
         * Note that this is an implicit Intent that must be defined in the
         * Android Manifest.
         */
        Intent i = new Intent("com.example.rtg_v1.ACTION_BIND");

        return mCtx.getApplicationContext().bindService(i, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (mBound) {
            mCtx.getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
    }
    
    public void closeConn() {
    	
    	Log.i("RTG", "DataServConn: closeConn");
    	handlerThread.quitSafely();
    	Log.i("RTG", "DataServConn: closeConn EIXT");
    }

    public void sayHello() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, MSG_SAY_HELLO, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopStart() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, MSG_STOP_START, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopService() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, MSG_STOP_SERVICE, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void addSource(int devId, int chIdx) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_ADD_SOURCE,devId, chIdx);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void delSource(int devId, int chIdx) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_DEL_SOURCE, devId, chIdx);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}