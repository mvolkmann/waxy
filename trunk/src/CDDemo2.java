import com.ociweb.xml.ElementWAX;
import com.ociweb.xml.PrologWAX;
import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;

public class CDDemo2 {

    public static void main(String[] args) {
        // Write to stdout with an XML declaration that specifies version 1.0.
        // If the version is omitted then no XML declaration will be written.
        PrologWAX pw = (PrologWAX) new WAX(WAX.Version.V1_0);

        pw.dtd("http://www.ociweb.com/xml/music.dtd");
        pw.xslt("artist.xslt");

        StartTagWAX stw = pw.start("artist");
        stw.attr("name", "Gardot, Melody");
        // null signifies the default namespace
        stw.namespace(null, "http://www.ociweb.com/music",
            "http://www.ociweb.com/xml/music.xsd");
        stw.namespace("date", "http://www.ociweb.com/date",
            "http://www.ociweb.com/xml/date.xsd");

        pw.comment("This is one of my favorite CDs!");
        stw = pw.start("cd");
        stw.attr("year", 2007);
        ElementWAX ew = stw.child("title", "Worrisome Heart");
        ew = stw.child("date", "purchaseDate", "4/3/2008");

        ew.close(); // terminates all unterminated elements
    }
}