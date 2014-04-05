package in.junaidbabu.androidxmpp.app;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> messages = new ArrayList();
    private Handler handler = new Handler();
    private xmppsettings settings;
    private EditText recipient;
    private EditText text;
    private ListView list;
    public static XMPPConnection connection;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipient = (EditText) this.findViewById(R.id.recipient);
        text = (EditText) this.findViewById(R.id.text);
        list = (ListView) this.findViewById(R.id.messageList);
        setListAdapter();

        //Window for getting settings
        settings = new xmppsettings(this);

        //Listener for chat message
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String to = recipient.getText().toString();
                String text1 = text.getText().toString();

                //Message msg = new Message();
                CustomMessage msg = new CustomMessage();
                msg.setTo(to);
                msg.setType(Message.Type.chat);
                msg.setBody(text1);
                msg.setCustomStanza("this was a custom text");
                Log.i("Custom message about to be sent", msg.toXML());
                connection.sendPacket(msg);
                messages.add(connection.getUser() + ":");
                messages.add(text1);
                setListAdapter();
            }
        });


    }
    public void send(View v){
        new ServiceDiscoveryManager(connection);
        FileTransferManager manager = new FileTransferManager(connection);
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(recipient.getText().toString());
        File file = new File("/storage/sdcard0/Download/calvin.jpg");
        //File file = new File("/storage/emulated/0/temp_photo.jpg");

        try {
            transfer.sendFile(file, "test_file");
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        while(!transfer.isDone()) {
            if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                System.out.println("ERROR!!! " + transfer.getError());
            } else if (transfer.getStatus().equals(FileTransfer.Status.cancelled)
                    || transfer.getStatus().equals(FileTransfer.Status.refused)) {
                System.out.println("Cancelled!!! " + transfer.getError());
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(transfer.getStatus().equals(FileTransfer.Status.refused) || transfer.getStatus().equals(FileTransfer.Status.error)
                || transfer.getStatus().equals(FileTransfer.Status.cancelled)){
            Log.i("File Transfer status", transfer.getStatus().toString());
            System.out.println("refused cancelled error " + transfer.getError());
        } else {
            System.out.println("Success");
        }
    }
    //Called by settings when connection is established
    public void setConnection (XMPPConnection connection) {
        this.connection = connection;
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        if (connection != null) {

            Log.i("Log main", "Phew, connection is not null");
            //Packet listener to get messages sent to logged in user
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() {
                public void processPacket(Packet packet) {      // For messages with custom item, we need custom Packet itself. No idea how to do that right now. I'll come back later.
                    Log.i("Packet to xml", packet.toXML());

                    //CustomMessage message = (CustomMessage) packet;

                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        //XStream a;
                        Log.i("Entire XML", message.toXML());   // This should give the entire packet
                      //  Log.i("Custom text", message.getCustomStanza());

                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        handler.post(new Runnable(){
                            public void run() {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, filter);

            //File receiving
            ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
            if(sdm == null)
            {
                sdm = new ServiceDiscoveryManager(connection);
                sdm.addFeature("http://jabber.org/protocol/disco#info");
                sdm.addFeature("http://jabber.org/protocol/disco#item");
                sdm.addFeature("jabber:iq:privacy");

                XMPPConnection.DEBUG_ENABLED = true;
            }
            FileTransferManager manager = new FileTransferManager(connection);
            FileTransferNegotiator.setServiceEnabled(connection, true);
            manager.addFileTransferListener(new FileTransferListener() {
                public void fileTransferRequest(final FileTransferRequest request) {
                    new Thread(){
                        @Override
                        public void run() {
                            Log.i("We are in the run","No idea who is running");
                            IncomingFileTransfer transfer = request.accept();
                            //File mf = Environment.getExternalStorageDirectory();
                            //File file = new File(mf.getAbsoluteFile()+"/storage/sdcard0/xmpptest/" + transfer.getFileName());
                            File file = new File("/storage/sdcard0/xmpptest/" + transfer.getFileName());
                            try{
                                transfer.recieveFile(file);
                                while(!transfer.isDone()) {
                                    try{
                                        Thread.sleep(1000L);
                                    }catch (Exception e) {
                                        Log.e("", e.getMessage());
                                    }
                                    if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                                        Log.e("ERROR!!! ", transfer.getError() + "");
                                    }
                                    if(transfer.getException() != null) {
                                        transfer.getException().printStackTrace();
                                    }
                                }
                            }catch (Exception e) {
                                Log.e("", e.getMessage());
                            }
                        };
                    }.start();
                }
            });
        }
    }

    private void setListAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.list, messages);
        list.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                settings.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
