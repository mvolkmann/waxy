import com.ociweb.xml.Version;
import com.ociweb.xml.WAX;

public class CDDemo {

    public static void main(String[] args) {
        // Write to System.out with an XML declaration
        // that specifies version 1.0.
        // If the version is omitted then no XML declaration will be written.
        WAX wax = new WAX(Version.V1_0);

        wax.xslt("artist.xslt")
           .dtd("http://www.ociweb.com/xml/music.dtd")
           .start("artist")
           .attr("name", "Gardot, Melody")
           // null signifies the default namespace
           .namespace(null, "http://www.ociweb.com/music",
               "http://www.ociweb.com/xml/music.xsd")
           .namespace("date", "http://www.ociweb.com/date",
               "http://www.ociweb.com/xml/date.xsd")

           .comment("This is one of my favorite CDs!")
           .start("cd").attr("year", 2007)
           .child("title", "Worrisome Heart")
           .child("date", "purchaseDate", "4/3/2008")

           .close(); // terminates all unterminated elements
    }
}