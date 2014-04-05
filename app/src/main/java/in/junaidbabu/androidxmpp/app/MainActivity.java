package in.junaidbabu.androidxmpp.app;

import android.app.Activity;
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
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

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

    //Called by settings when connection is established
    public void setConnection (XMPPConnection connection) {
        this.connection = connection;
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
