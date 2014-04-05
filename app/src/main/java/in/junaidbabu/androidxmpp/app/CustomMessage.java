package in.junaidbabu.androidxmpp.app;

import org.jivesoftware.smack.packet.Message;

/**
 * Created by neo on 5/4/14.
 */
public class CustomMessage extends org.jivesoftware.smack.packet.Message {
    public CustomMessage() {
        super();
    }

    private String customStanza="blank set by me";

    public CustomMessage(String to, Type chat) {
        this.setTo(to);
        this.setType(chat);
    }

    /**
     * @param customStanza
     *            the customStanza to set
     */
    public void setCustomStanza(String customStanza) {
        this.customStanza = customStanza;
    }
    public String getCustomStanza(){
        return this.customStanza;
    }

    @Override
    public String toXML() {
        String XMLMessage = super.toXML();
        String XMLMessage1 = XMLMessage.substring(0, XMLMessage.indexOf(">"));
        String XMLMessage2 = XMLMessage.substring(XMLMessage.indexOf(">"));
        if (this.customStanza != null) {
            XMLMessage1 += " CustomStanza=\"" + this.customStanza + "\"";
        }

        return XMLMessage1 + XMLMessage2;
    }
}