package in.junaidbabu.androidxmpp.app;


import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.File;
//settings get input and then connection is established
/**
 * Created by neo on 30/3/14.
 */

public class xmppsettings extends Dialog implements android.view.View.OnClickListener  {
    private MainActivity chatClient;

    public xmppsettings(MainActivity chatClient){
        super(chatClient);
        this.chatClient = chatClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settings);
        setTitle("Connection Settings");
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }
    ConnectionConfiguration connectionConfig;
    XMPPConnection connection;
    String host, port, service, username, password;
    public void onClick(View v) {
        host = getText(R.id.host);
        port = getText(R.id.port);
        service = getText(R.id.service);
        username = getText(R.id.userid);
        password = getText(R.id.password);
        //Log.i("FYI", host+port+service+username+password);
        // Create connection

        connectionConfig = new ConnectionConfiguration(host, Integer.parseInt(port), service);

        connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled); // -\__ Required for OpenFire login
        connectionConfig.setSocketFactory(new DummySSLSocketFactory());                 // _/


       // connectionConfig.setSASLAuthenticationEnabled(false); // required for talk.google.com
        //connectionConfig.setTruststoreType("BKS");            // required for talk.google.com
        connection = new XMPPConnection(connectionConfig);
        new ConnectToXmpp().execute();
    }
    private class ConnectToXmpp extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                connection.connect();
                Log.i("Log", "Connection established, yaay!");
            } catch (XMPPException ex) {
                Log.i("Log", "Unable to connect to the server");
                chatClient.setConnection(null);
            }
            try {
                connection.login(username, password);

                // Set status to online / available
                Presence presence = new Presence(Presence.Type.available);
                connection.sendPacket(presence);
                chatClient.setConnection(connection);
                Log.i("Log", "Life is peace :D");
            } catch (XMPPException ex) {
                Log.e("Log", "Pain only");
                ex.printStackTrace();
                chatClient.setConnection(null);
            } catch (Exception e){
                e.printStackTrace();
               // Toast.makeText(getContext(), "Oops", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("OpnPostExe", "Here");
            //MainActivity.connection = connection;
            dismiss();
        }

    }
    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }

}