package in.junaidbabu.androidxmpp.app;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neo on 13/4/14.
 */
public class ListIQ extends IQ
{

    private List<Chat> chats;

    private Set set;

    public ListIQ()
    {
        this.chats = new ArrayList<Chat>();
    }

    public Set getSet()
    {
        return set;
    }

    public void setSet(Set set)
    {
        this.set = set;
    }

    public void addChat(Chat chat)
    {
        chats.add(chat);
    }

    public List<Chat> getChats()
    {
        return chats;
    }

    @Override
    public String getChildElementXML()
    {
        StringBuilder builder = new StringBuilder("<list xmlns=\"urn:xmpp:archive\">");
        for (Chat chat : chats)
        {
            builder.append(chat.toXml());
        }
        builder.append(set.toXml());
        builder.append("</list>");
        return builder.toString();
    }

    public static class Chat {
        private String with;
        private String start;

        public Chat()
        {
        }

        public Chat(String with, String start)
        {
            this.with = with;
            this.start = start;
        }

        public String getWith()
        {
            return with;
        }

        public void setWith(String with)
        {
            this.with = with;
        }

        public String getStart()
        {
            return start;
        }

        public void setStart(String start)
        {
            this.start = start;
        }

        public String toXml()
        {
            StringBuilder builder = new StringBuilder("<chat with=\"");
            builder.append(with).append("\"");
            builder.append(" start=\"");
            builder.append(start);
            builder.append("\"/>");
            return builder.toString();
        }

    }

    public static class Set {
        private int last;
        private int count;
        private int indexAtt;
        private int first;

        public Set()
        {
        }

        public int getLast()
        {
            return last;
        }

        public void setLast(int last)
        {
            this.last = last;
        }

        public int getCount()
        {
            return count;
        }

        public void setCount(int count)
        {
            this.count = count;
        }

        public int getIndexAtt()
        {
            return indexAtt;
        }

        public void setIndexAtt(int indexAtt)
        {
            this.indexAtt = indexAtt;
        }

        public int getFirst()
        {
            return first;
        }

        public void setFirst(int first)
        {
            this.first = first;
        }

        public String toXml()
        {
            StringBuilder builder = new StringBuilder("<set xmlns=\"http://jabber.org/protocol/rsm\">");
            builder.append("<first index=\"").append(indexAtt).append("\">").append(first).append("</first>");
            builder.append("<last>").append(last).append("</last>");
            builder.append("<count>").append(count).append("</count>");
            builder.append("</set>");
            return builder.toString();
        }
    }

}