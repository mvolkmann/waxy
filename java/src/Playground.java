import com.ociweb.xml.WAX;

public class Playground {

    public static void main(String[] args) {
        WAX.newInstance()
           .xslt("foo.xslt")
           .dtd("root.dtd")
           .start("root");
    }
}