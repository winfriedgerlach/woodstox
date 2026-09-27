package failing;

import java.io.StringReader;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLStreamLocation2;

import wstxtest.stream.BaseStreamTest;
import org.junit.jupiter.api.Test;

/**
 * Reproducer for wrong locations of tokens at the boundaries of internal
 * entity expansions (woodstox issue #TBD). Locations of tokens within an
 * expansion are otherwise reported in coordinates of the entity value,
 * with the location of the reference as context; but
 *<ul>
 * <li>the first token of an expansion that directly follows markup gets
 *   the location of the entity reference instead, and</li>
 * <li>the first token after an expansion gets the location where the
 *   replacement text ended, in coordinates of the entity declaration.</li>
 *</ul>
 * External entities are not affected.
 */
public class InternalEntityLocationTest
    extends BaseStreamTest
{
    // Entity value starts at line 2, column 13
    final static String DOC =
        "<!DOCTYPE root [\n"
        +"<!ENTITY e '<a/><b/>'>\n"
        +"]>\n"
        +"<root>&e;</root>";

    // Value of 'in' starts at line 2, column 14; that of 'out' at line 3, column 15
    final static String NESTED_DOC =
        "<!DOCTYPE root [\n"
        +"<!ENTITY in '<i/>text<j/>'>\n"
        +"<!ENTITY out '<o>&in;</o><p/>'>\n"
        +"]>\n"
        +"<root>&out;</root>";

    @Test
    public void testFirstTokenOfExpansion() throws XMLStreamException
    {
        XMLStreamReader sr = constructReader(DOC);
        skipToStart(sr, "b");
        // Control: second token of the expansion is fine
        assertLocation("<b/>", sr.getLocation(), 2, 17, 4);

        sr = constructReader(DOC);
        skipToStart(sr, "a");
        // but the first one gets [4,7], location of "&e;"
        assertLocation("<a/>", sr.getLocation(), 2, 13, 4);
    }

    @Test
    public void testFirstTokenAfterExpansion() throws XMLStreamException
    {
        XMLStreamReader sr = constructReader(DOC);
        skipToEnd(sr, "root");
        // Gets [2,21], where the entity value ends in the declaration
        assertLocation("</root>", sr.getLocation(), 4, 10, -1);
    }

    @Test
    public void testFirstTokenOfNestedExpansion() throws XMLStreamException
    {
        XMLStreamReader sr = constructReader(NESTED_DOC);
        skipToStart(sr, "i");
        // Gets [3,18], location of "&in;" within value of 'out'
        assertLocation("<i/>", sr.getLocation(), 2, 14, 3);
    }

    @Test
    public void testFirstTokenAfterNestedExpansion() throws XMLStreamException
    {
        XMLStreamReader sr = constructReader(NESTED_DOC);
        skipToEnd(sr, "o");
        // Gets [2,26], where value of 'in' ends in its declaration
        assertLocation("</o>", sr.getLocation(), 3, 22, 5);
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////////
     */

    private XMLStreamReader constructReader(String xml) throws XMLStreamException
    {
        XMLInputFactory f = getNewInputFactory();
        f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        return f.createXMLStreamReader(new StringReader(xml));
    }

    private void skipToStart(XMLStreamReader sr, String localName) throws XMLStreamException
    {
        while (sr.next() != START_ELEMENT || !localName.equals(sr.getLocalName())) { }
    }

    private void skipToEnd(XMLStreamReader sr, String localName) throws XMLStreamException
    {
        while (sr.next() != END_ELEMENT || !localName.equals(sr.getLocalName())) { }
    }

    /**
     * @param contextLine Line of the location's context (the entity reference),
     *    or -1 if it is to have no context
     */
    private void assertLocation(String token, Location loc, int line, int col,
            int contextLine)
    {
        String desc = token + " at [" + loc.getLineNumber() + "," + loc.getColumnNumber() + "]";
        assertEquals("Line of " + desc, line, loc.getLineNumber());
        assertEquals("Column of " + desc, col, loc.getColumnNumber());
        XMLStreamLocation2 ctxt = ((XMLStreamLocation2) loc).getContext();
        if (contextLine < 0) {
            assertNull("Context of " + desc, ctxt);
        } else {
            assertNotNull("Context of " + desc, ctxt);
            assertEquals("Context line of " + desc, contextLine, ctxt.getLineNumber());
        }
    }
}
