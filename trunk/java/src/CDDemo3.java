import com.ociweb.xml.Version;
import com.ociweb.xml.WAX;

public class CDDemo3 {

    public static void main(String[] args) {
        // Write to stdout with an XML declaration that specifies version 1.0.
        // If the version is omitted then no XML declaration will be written.
        WAX wax = new WAX(Version.V1_0);

        wax.xslt("artist.xslt");
        wax.dtd("http://www.ociweb.com/xml/music.dtd");

        wax.start("artist");
        wax.attr("name", "Gardot, Melody");
        wax.defaultNamespace("http://www.ociweb.com/music",
            "http://www.ociweb.com/xml/music.xsd");
        wax.namespace("date", "http://www.ociweb.com/date",
            "http://www.ociweb.com/xml/date.xsd");

        wax.comment("This is one of my favorite CDs!");
        wax.start("cd");
        wax.attr("year", 2007);

        wax.child("title", "Worrisome Heart");
        wax.child("date", "purchaseDate", "4/3/2008");

        wax.close(); // terminates all unterminated elements
    }
}
