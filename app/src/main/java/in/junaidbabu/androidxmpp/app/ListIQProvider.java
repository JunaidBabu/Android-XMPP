package in.junaidbabu.androidxmpp.app;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by neo on 13/4/14.
 */
public class ListIQProvider implements IQProvider {

    public ListIQProvider()
    {
    }

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        //Logger.d(String.format("Received iq packet, namespace[%s], name[%s]", parser.getNamespace(), parser.getName()));
        ListIQ iq = new ListIQ();
        Set set = new Set();
        boolean done = false;

        String with = "", start = "";
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("chat")) {
                    with = parser.getAttributeValue("", "with");
                    start = parser.getAttributeValue("", "start");
                    iq.addChat(new ListIQ.Chat(with, start));
                } else if (parser.getName().equals("first")) {
                    int index = parseInt(parser.getAttributeValue("", "index"));
                    set.setIndexAtt(index);
                    int first = parseInt(parser.nextText());
                    set.setFirst(first);
                } else if (parser.getName().equals("last")) {
                    int last = parseInt(parser.nextText());
                    set.setLast(last);
                } else if (parser.getName().equals("count")) {
                    int count = parseInt(parser.nextText());
                    set.setCount(count);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("list")) {
                    iq.setSet(set);
                    done = true;
                }
            }
        }

        return iq;
    }

    private int parseInt(String integer)
    {
        return Integer.parseInt((integer != null ? integer : "0"));
    }

    public static class Set extends ListIQ.Set {
        public Set()
        {
            super();
        }

    }

}