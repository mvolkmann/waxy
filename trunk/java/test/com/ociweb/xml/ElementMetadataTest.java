package com.ociweb.xml;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

public class ElementMetadataTest {

    @SuppressWarnings("unchecked")
    private static Set<String> getDefinedAttributeNamesSet(
            final ElementMetadata elementMetadata)
            throws NoSuchFieldException, IllegalAccessException
    {
        final Field field = ElementMetadata.class
                .getDeclaredField("definedAttributeNames");
        field.setAccessible(true);
        return (Set<String>) field.get(elementMetadata);
    }

    private static boolean getVerifyUsageValue(
            final ElementMetadata elementMetadata)
            throws NoSuchFieldException, IllegalAccessException
    {
        final Field field = ElementMetadata.class
                .getDeclaredField("verifyUsage");
        field.setAccessible(true);
        return ((Boolean) field.get(elementMetadata)).booleanValue();
    }

    private ElementMetadata newElementMetadata(
            final ElementMetadata parent,
            final String name) {
        final StringWriter sw = new StringWriter();

        final boolean verifyUsage = true;
        final XMLWriter out = new XMLWriter(sw, verifyUsage);
        final String prefix = null;
        final boolean isCommentElement = false;

        return new ElementMetadata(out, verifyUsage, parent, prefix,
                name, isCommentElement);
    }

    @Test
    public void testClearAttributeDataWhenStartElementIsVerified()
    throws Exception {
        final ElementMetadata elementMetadata = newElementMetadata(null, "Element");
        assertEquals(0, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.writeAttributeEqualsValue("ns", "attr1", "value1", false, false);
        assertEquals(1, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.writeAttributeEqualsValue("ns", "attr2", "value2", false, false);
        assertEquals(2, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.writeNamespaceDeclaration("ns", "http://www.ociweb.com/ns",
                "http://www.ociweb.com/xml/ns.xsd");

        elementMetadata.verifyOutstandingNamespacePrefixes();

        assertEquals(0, getDefinedAttributeNamesSet(elementMetadata).size());
    }

    /**
     * In current usage, there's no technical need for the <code>trustMe</code>/
     * <code>checkMe</code>/<code>verifyUsage</code> flag to propagate to parent
     * instances, or to even exist in those instances at all. But we're ensuring
     * that all copies of this flag maintain the same value to avoid <b>really
     * nasty surprises</b> should an unsuspecting developer find a need to use
     * one of these flags in future development.
     */
    @Test
    public void testSetTrustMePropigatesToParents() throws Exception {
        final ElementMetadata parent = newElementMetadata(null, "Parent");
        final ElementMetadata child = newElementMetadata(parent, "Child");
        assertTrue(getVerifyUsageValue(parent));

        child.setTrustMe(true);

        assertFalse("'setTrustMe' method should work.",
                getVerifyUsageValue(child));
        assertFalse(
                "'setTrustMe' method should propigate to parent class(es).",
                getVerifyUsageValue(parent));
    }

    @Test
    public void testTrustMeFlagToCurrentObjectForAttrNamespaceValidation() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.setTrustMe(true);

        // NO IllegalArgumentException thrown here:
        wax.attr(" bad namespace ", "goodName", "value");
    }

    @Test
    public void testTrustMeFlagToCurrentObjectForAttrNameValidation() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.setTrustMe(true);

        // NO IllegalArgumentException thrown here:
        wax.attr(" bad name ", "value");
    }

    @Test
    public void testTrustMeFlagToCurrentObjectForNamespaceValidation() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.setTrustMe(true);

        // NO IllegalArgumentException thrown here:
        wax.namespace("foo", " bad URL ");
    }

}
