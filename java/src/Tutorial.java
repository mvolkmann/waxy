import com.ociweb.xml.Version;
import com.ociweb.xml.WAX;

/**
 * This class serves as a tutorial for using WAX.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class Tutorial {

    public static void main(String[] args) {
        // When the no-arg WAX constructor is used, XML is written to stdout.
        // There are also WAX constructors that take
        // a java.io.OutputStream and a java.io.Writer object.
        WAX wax = new WAX();

        out("Only a root element:");
        wax.start("car").close();
        // <car/>

        // After a WAX object is closed,
        // a new one must be created to write more XML.
        wax = new WAX();

        out("A root element with some text inside:");
        wax.start("car").text("Prius").close();
        // <car>Prius</car>

        out("Text inside a child element:");
        wax = new WAX();
        wax.start("car").start("model").text("Prius").close();
        // <car>
        //   <model>Prius</model>
        // </car>

        out("The same with the \"child\" convenience method:");
        wax = new WAX();
        wax.start("car").child("model", "Prius").close();
        // <car>
        //   <model>Prius</model>
        // </car>

        out("Text in a CDATA section:");
        wax = new WAX();
        wax.start("car").start("model").cdata("1<2>3&4'5\"6").close();
        // <car>
        //   <model>
        //     <![CDATA[1<2>3&4'5\"6]]>
        //   </model>
        // </car>

        out("Without indentation, on a single line:");
        wax = new WAX();
        wax.noIndentsOrCRs();
        wax.start("car").child("model", "Prius").close();
        // <car><model>Prius</model></car>

        out("Indent with four spaces instead of the default of two:");
        wax = new WAX();
        wax.setIndent("    ");
        wax.start("car").child("model", "Prius").close();
        // <car>
        //     <model>Prius</model>
        // </car>

        out("Add an attribute:");
        wax = new WAX();
        wax.start("car").attr("year", 2008).child("model", "Prius").close();
        // <car year="2008">
        //   <model>Prius</model>
        // </car>

        out("XML declaration:");
        wax = new WAX(Version.V1_0);
        wax.start("car").attr("year", 2008)
           .child("model", "Prius").close();
        // <?xml version="1.0" encoding="UTF-8"?>
        // <car year="2008">
        //   <model>Prius</model>
        // </car>

        out("Comment:");
        wax = new WAX();
        wax.comment("This is a hybrid car.")
           .start("car").child("model", "Prius").close();
        // <!-- This is a hybrid car. -->
        // <car>
        //   <model>Prius</model>
        // </car>

        out("Processing instruction:");
        wax = new WAX();
        wax.processingInstruction("target", "data")
            .start("car").attr("year", 2008)
           .child("model", "Prius").close();
        // <?target data?>
        // <car year="2008">
        //   <model>Prius</model>
        // </car>

        out("Associate an XSLT stylesheet:");
        wax = new WAX();
        wax.xslt("car.xslt")
           .start("car").attr("year", 2008)
           .child("model", "Prius").close();
        // <?xml-stylesheet type="text/xsl" href="car.xslt"?>
        // <car year="2008">
        //   <model>Prius</model>
        // </car>

        out("Associate a default namespace:");
        wax = new WAX();
        wax.start("car").attr("year", 2008)
           .defaultNamespace("http://www.ociweb.com/cars")
           .child("model", "Prius").close();
        // <car year="2008"
        //   xmlns="http://www.ociweb.com/cars">
        //   <model>Prius</model>
        // </car>

        out("Associate a non-default namespace with the XML:");
        wax = new WAX();
        String prefix = "c";
        wax.start(prefix, "car").attr("year", 2008)
           .namespace(prefix, "http://www.ociweb.com/cars")
           .child(prefix, "model", "Prius").close();
        // <c:car year="2008"
        //   xmlns:c="http://www.ociweb.com/cars">
        //   <c:model>Prius</c:model>
        // </c:car>

        out("Associate an XML Schema:");
        wax = new WAX();
        wax.start("car").attr("year", 2008)
           .defaultNamespace("http://www.ociweb.com/cars", "car.xsd")
           .child("model", "Prius").close();
        // <car year="2008"
        //   xmlns="http://www.ociweb.com/cars"
        //   xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
        //   xsi:schemaLocation="http://www.ociweb.com/cars car.xsd">
        //   <model>Prius</model>
        // </car>

        out("Associate multiple XML Schemas:");
        wax = new WAX();
        wax.start("car").attr("year", 2008)
           .defaultNamespace("http://www.ociweb.com/cars", "car.xsd")
           .namespace("m", "http://www.ociweb.com/model", "model.xsd")
           .child("m", "model", "Prius").close();
        // <car year="2008"
        //   xmlns="http://www.ociweb.com/cars"
        //   xmlns:m="http://www.ociweb.com/model"
        //   xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
        //   xsi:schemaLocation="http://www.ociweb.com/cars car.xsd
        //     http://www.ociweb.com/model model.xsd">
        //   <m:model>Prius</m:model>
        // </car>

        out("Associate a DTD:");
        wax = new WAX();
        wax.dtd("car.dtd")
           .start("car").attr("year", 2008)
           .child("model", "Prius").close();
        // <!DOCTYPE car SYSTEM "car.dtd">
        // <car year="2008">
        //   <model>Prius</model>
        // </car>

        out("Entity definitions in DOCTYPE:");
        String url = "http://www.ociweb.com/xml/";
        wax = new WAX();
        wax.entityDef("oci", "Object Computing, Inc.")
           .externalEntityDef("moreData", url + "moreData.xml")
           .start("root")
           .unescapedText("The author works at &oci; in St. Louis, Missouri.", true)
           .unescapedText("&moreData;", true)
           .close();
        //<!DOCTYPE root [
        //  <!ENTITY oci "Object Computing, Inc.">
        //  <!ENTITY moreData SYSTEM "http://www.ociweb.com/xml/moreData.xml">
        //>
        //<root>
        //  The author works at &oci; in St. Louis, Missouri.
        //  &moreData;
        //</root>

        out("Default indentation:");
        wax = new WAX();
        wax.start("foo").text("bar").comment("baz").close();
    }

    private static void out(String text) {
        System.out.println("\n\n" + text);
    }
}
