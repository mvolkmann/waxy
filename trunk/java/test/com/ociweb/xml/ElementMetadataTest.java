package com.ociweb.xml;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

public class ElementMetadataTest {

    private final boolean checkMe = true;

    @Test
    public void testClearAttributeDataWhenStartElementIsVerified()
    throws Exception {
        final ElementMetadata elementMetadata = newElementMetadata("Element");
        assertEquals(0, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.defineAttribute("ns", "attr1", true);
        assertEquals(1, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.defineAttribute("ns", "attr2", true);
        assertEquals(2, getDefinedAttributeNamesSet(elementMetadata).size());
        elementMetadata.defineNamespace("ns", "http://www.ociweb.com/ns");

        elementMetadata.verifyOutstandingNamespacePrefixes();

        assertEquals(0, getDefinedAttributeNamesSet(elementMetadata).size());
    }

    @SuppressWarnings("unchecked")
    private static Set<String> getDefinedAttributeNamesSet(
            final ElementMetadata elementMetadata) //
            throws NoSuchFieldException, IllegalAccessException //
    {
        final Field field = ElementMetadata.class
                .getDeclaredField("definedAttributeNames");
        field.setAccessible(true);
        return (Set<String>) field.get(elementMetadata);
    }

    private ElementMetadata newElementMetadata(final String name) {
        final String prefix = null;
        final boolean isCommentElement = false;
        final ElementMetadata parent = null;

        return new ElementMetadata(
                prefix, name, isCommentElement, parent, checkMe);
    }
}
