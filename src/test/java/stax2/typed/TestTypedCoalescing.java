package stax2.typed;

import javax.xml.stream.*;

import org.codehaus.stax2.XMLStreamReader2;

import stax2.BaseStax2Test;
import org.junit.jupiter.api.Test;

/**
 * Verifies that typed access to element content includes all of its
 * text and CDATA segments, also when the reader coalesces text: with
 * coalescing, segments adjacent to the first text event are part of it,
 * and must not be skipped.
 */
public class TestTypedCoalescing
    extends BaseStax2Test
{
    final static String[] INT_DOCS = new String[] {
        "<root>12<![CDATA[3]]></root>",
        "<root><![CDATA[1]]>23</root>",
        "<root>1<!--comment-->2<![CDATA[3]]></root>",
    };

    final static String[] INT_ARRAY_DOCS = new String[] {
        "<root>1 2<![CDATA[ 3]]></root>",
        "<root>1 <!--comment-->2<![CDATA[ 3]]></root>",
    };

    @Test
    public void testElementAsInt() throws XMLStreamException
    {
        for (boolean coalescing : new boolean[] { false, true }) {
            for (String doc : INT_DOCS) {
                XMLStreamReader2 sr = getRootReader(doc, coalescing);
                assertEquals("Value of "+doc+" (coalescing: "+coalescing+")",
                        123, sr.getElementAsInt());
                assertTokenType(END_ELEMENT, sr.getEventType());
                assertTokenType(END_DOCUMENT, sr.next());
                sr.close();
            }
        }
    }

    @Test
    public void testElementAsIntArray() throws XMLStreamException
    {
        for (boolean coalescing : new boolean[] { false, true }) {
            for (String doc : INT_ARRAY_DOCS) {
                XMLStreamReader2 sr = getRootReader(doc, coalescing);
                int[] result = new int[10];
                assertEquals("Count of "+doc+" (coalescing: "+coalescing+")",
                        3, sr.readElementAsIntArray(result, 0, result.length));
                assertEquals(1, result[0]);
                assertEquals(2, result[1]);
                assertEquals(3, result[2]);
                sr.close();
            }
        }
    }

    // With coalescing, text and CDATA form one event, so its tokens can span both
    @Test
    public void testElementAsIntArrayTokenSpanningCData() throws XMLStreamException
    {
        XMLStreamReader2 sr = getRootReader("<root>1<![CDATA[2]]></root>", true);
        int[] result = new int[10];
        assertEquals(1, sr.readElementAsIntArray(result, 0, result.length));
        assertEquals(12, result[0]);
        sr.close();
    }

    private XMLStreamReader2 getRootReader(String doc, boolean coalescing)
        throws XMLStreamException
    {
        XMLInputFactory f = getInputFactory();
        setCoalescing(f, coalescing);
        XMLStreamReader2 sr = constructStreamReader(f, doc);
        assertTokenType(START_ELEMENT, sr.next());
        return sr;
    }
}
