package in.junaidbabu.androidxmpp.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.util.ArrayList;

import static android.util.Log.wtf;
import static org.jivesoftware.smackx.LastActivityManager.getLastActivity;

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
                final String to = recipient.getText().toString()+"@127.0.0.1";
                String text1 = text.getText().toString();

                Roster roster = connection.getRoster();
                synchronized (roster) {

                }
                Log.e("Roster list", roster.getEntries().toString());

                //Message msg = new Message();
                CustomMessage msg = new CustomMessage();
                Packet packet = new Packet() {
                    @Override
                    public String toXML() {
                        //String xExtension = "<message id=\"V2KJS-13\" to=\"junaid@127.0.0.1\" type=\"chat\" CustomStanza=\"this was a custom text\"><body>here is a message</body></message>";
//                        String xExtension = //"<iq type='get' id='v3'><getservertime xmlns='urn:xmpp:mrpresence'/></iq>";
//                        "<iq type='get' id='id_for_archive'>\n" +
//                                "<list xmlns='urn:xmpp:archive'\n" +
//                                "with='junaid@127.0.0.1'>\n" +
//                                "<set xmlns='http://jabber.org/protocol/rsm'>\n" +
//                                "<max>30</max>\n" +
//                                "</set>\n" +
//                                "</list>\n" +
//                                "</iq>";
                        String xExtension = "<iq type='get' id='archive_id_setmanually'><list xmlns='urn:xmpp:archive' with='junaid@127.0.0.1'><set xmlns='http://jabber.org/protocol/rsm'><max>10</max></set></list></iq>";
                        return xExtension;
                    }
                };
                //packet.set
                msg.setTo(to);
                msg.setType(Message.Type.chat);
                msg.setBody(text1);

                msg.setCustomStanza("this was a custom text");
                Log.i("Custom message about to be sent", msg.toXML());
                Log.i("IQ Sent", packet.toXML());
                connection.sendPacket(msg);
                connection.sendPacket(packet);
                //connection.sendPacket(msg);
                messages.add(connection.getUser() + ":");
                messages.add(text1);
                setListAdapter();
            }
        });


    }
    public void send(View v){
        new ServiceDiscoveryManager(connection);
        try {
            LastActivity lastActivity =  getLastActivity(connection, recipient.getText().toString());
            Log.e("SOme data" ,lastActivity.getStatusMessage().toString()+" "+Long.toString(lastActivity.lastActivity));
            Log.e("Some more data", Long.toString(lastActivity.getIdleTime()));
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        FileTransferManager manager = new FileTransferManager(connection);
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(recipient.getText().toString());

        File file = new File("/storage/sdcard0/Download/calvin.jpg");
        //File file = new File("/storage/emulated/0/temp_photo.jpg");

        try {
            transfer.sendFile(file, "test_file");
            Log.i("Transferring", "The above line to transfer didn't cup. yet!");
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
        this.connection.DEBUG_ENABLED = true;
        ProviderManager pm = ProviderManager.getInstance();
        /*
        *  The following lines are critical for file handling in xmpp
        *
        */
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        ProviderManager.getInstance().addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());

        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
//        pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new IBBProviders.Open());
//        pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new IBBProviders.Close());
//        pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new IBBProviders.Data());
        pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new OpenIQProvider());
        pm.addIQProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());
        pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new CloseIQProvider());
        pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());
        pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());
        //  Offline Message Requests
        pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

//  Offline Message Indicator
        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
//        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XMLPlayerListProvider());

        if (connection != null) {


            Log.i("Log main", "Phew, connection is not null");
            //Packet listener to get messages sent to logged in user
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
//            connection.addPacketListener(new PacketListener() {
//                public void processPacket(Packet packet) {      // For messages with custom item, we need custom Packet itself. No idea how to do that right now. I'll come back later.
//                    Log.i("Packet to xml", packet.toXML());
//
//                    //CustomMessage message = (CustomMessage) packet;
//
//                    Message message = (Message) packet;
//                    if (message.getBody() != null) {
//                        //XStream a;
//                        //  Log.i("Entire XML", message.toXML());   // This should give the entire packet
//                        //  Log.i("Custom text", message.getCustomStanza());
//
//                        String fromName = StringUtils.parseBareAddress(message.getFrom());
//                        messages.add(fromName + ":");
//                        messages.add(message.getBody());
//                        handler.post(new Runnable() {
//                            public void run() {
//                                setListAdapter();
//                            }
//                        });
//                    }
//                }
//            }, filter);
            connection.addPacketListener(new PacketListener()
            {
                @Override
                public void processPacket(Packet p)
                {

                    Log.v("All kinds of packet", p.toXML());
                    Log.e("Extension type", p.getExtension("type").toString());
                    wtf("Asdf", "Asfd");
                    Message message = (Message) p;
                    if (message.getBody() != null) {
                        //XStream a;
                        //  Log.i("Entire XML", message.toXML());   // This should give the entire packet
                        //  Log.i("Custom text", message.getCustomStanza());

                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        handler.post(new Runnable() {
                            public void run() {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    return true;
                }
            });
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

//            FileTransferManager manager = new FileTransferManager(connection);
//            FileTransferNegotiator.setServiceEnabled(connection, true);
//            manager.addFileTransferListener(new FileTransferListener() {
//                public void fileTransferRequest(final FileTransferRequest request) {
//
//                    new Thread(){
//                        @Override
//                        public void run() {
//                            Log.i("We are in the run", "No idea who is running -_- :P");
//                            IncomingFileTransfer transfer = request.accept();
//                            messages.add(request.getRequestor() + ":");
//                            messages.add(request.getFileName());
//                            handler.post(new Runnable(){
//                                public void run() {
//                                    setListAdapter();
//                                }
//                            });
//                            //File mf = Environment.getExternalStorageDirectory();
//                            //File file = new File(mf.getAbsoluteFile()+"/storage/sdcard0/xmpptest/" + transfer.getFileName());
//                            File file = new File("/storage/sdcard0/xmpptest/" + transfer.getFileName());
//                            try{
//                                transfer.recieveFile(file);
//                                while(!transfer.isDone()) {
//                                    try{
//                                        Thread.sleep(1000L);
//                                    }catch (Exception e) {
//                                        Log.e("", e.getMessage());
//                                    }
//                                    if(transfer.getStatus().equals(FileTransfer.Status.error)) {
//                                        Log.e("ERROR!!! ", transfer.getError() + "");
//                                    }
//                                    if(transfer.getException() != null) {
//                                        transfer.getException().printStackTrace();
//                                    }
//                                }
//                            }catch (Exception e) {
//                                Log.e("", e.getMessage());
//                            }
//                        };
//                    }.start();
//                }
//            });
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
