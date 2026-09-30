package wstxtest.wstream;

import java.io.*;

import javax.xml.stream.*;

import org.codehaus.stax2.*;

import com.ctc.wstx.api.WstxOutputProperties;
import org.junit.jupiter.api.Test;

/**
 * This unit test suite verifies Woodstox-specific output-side
 * character escaping options
 */
public class TestEscaping
    extends BaseWriterTest
{
    @Test
    public void testCrHandlingEscaping()
        throws XMLStreamException
    {
        doTestCrHandling(true, "Cr: \r.", "Cr: \r.", "Cr: \r.");
        doTestCrHandling(true, "CrLF: \r\n.", "CrLF: \r\n.", "CrLF: \r\n.");
    }

    @Test
    public void testCrHandlingNonEscaping()
        throws XMLStreamException
    {
        doTestCrHandling(false, "Cr: \r.", "Cr: \n.", "Cr:  .");
        // attribute output is same, but parser handling differs, so:
        doTestCrHandling(false, "CrLF: \r\n.", "CrLF: \n.", "CrLF:  \n.");
    }

    // Attribute values longer than the output buffer, with chars that need
    // escaping at shifting positions, must round-trip unchanged
    @Test
    public void testLongAttrValueEscaping()
        throws XMLStreamException
    {
        final String specials = "\"&<>\t\n\u00e9\u3000\ud83d\ude00";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; sb.length() < 3000; ++i) {
            sb.append("abcdefghij".substring(0, i % 10));
            char c = specials.charAt(i % specials.length());
            if (Character.isHighSurrogate(c)) {
                sb.append(c).append(specials.charAt(specials.length()-1));
            } else if (!Character.isLowSurrogate(c)) {
                sb.append(c);
            }
        }
        final String value = sb.toString();

        for (String enc : new String[] { "UTF-8", "ISO-8859-1", "US-ASCII" }) {
            for (int type = 0; type < 3; ++type) {
                XMLOutputFactory2 f = getFactory(type, true);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XMLStreamWriter sw = f.createXMLStreamWriter(out, enc);
                sw.writeStartDocument();
                sw.writeStartElement("root");
                for (int i = 0; i < 20; ++i) {
                    sw.writeStartElement("e");
                    sw.writeAttribute("a", value.substring(pairStart(value, i * 37)));
                    sw.writeCharacters(value.substring(0, pairStart(value, i * 11)));
                    sw.writeEndElement();
                }
                sw.writeEndElement();
                sw.writeEndDocument();
                sw.close();

                XMLStreamReader2 sr = constructNsStreamReader(new ByteArrayInputStream(out.toByteArray()), true);
                assertTokenType(START_ELEMENT, sr.next());
                for (int i = 0; i < 20; ++i) {
                    assertTokenType(START_ELEMENT, sr.next());
                    assertEquals("Attribute value #"+i+" (encoding: "+enc+", writer type: "+type+")",
                            value.substring(pairStart(value, i * 37)), sr.getAttributeValue(0));
                    sr.getElementText();
                }
                sr.close();
            }
        }
    }

    /*
    ////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////
     */

    // Moves an index off the second half of a surrogate pair
    private static int pairStart(String str, int index) {
        return Character.isLowSurrogate(str.charAt(index)) ? index + 1 : index;
    }

    private void doTestCrHandling(boolean escaping, String input,
                                  String elemOutput, String attrOutput)
        throws XMLStreamException
    {
        // Let's try out 2 main encoding types:
        String[] ENC = new String[] { "UTF-8", "ISO-8859-1", "US-ASCII" };
        for (String enc : ENC) {
            // And 3 writer types:
            for (int type = 0; type < 3; ++type) {
                XMLOutputFactory2 f = getFactory(type, escaping);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XMLStreamWriter sw = f.createXMLStreamWriter(out, enc);
                writeDoc(sw, input);
                sw.close();

                // Ok, do we get what we should?
                verifyDoc(escaping, enc, out.toByteArray(),
                          elemOutput, attrOutput, sw);
            }
        }
    }

    private void writeDoc(XMLStreamWriter sw, String text)
        throws XMLStreamException
    {
        sw.writeStartDocument();
        sw.writeStartElement("root");
        sw.writeAttribute("attr", text);
        sw.writeCharacters(text);
        sw.writeEndElement();
        sw.writeEndDocument();
    }

    private void verifyDoc(boolean escaping, String encoding, byte[] data,
                           String expElem, String expAttr, XMLStreamWriter sw)
        throws XMLStreamException
    {
        XMLStreamReader2 sr = constructNsStreamReader(new ByteArrayInputStream(data), true);
        String actualText;
        assertTokenType(START_ELEMENT, sr.next());
        assertEquals("root", sr.getLocalName());

        assertEquals(1, sr.getAttributeCount());
        actualText = sr.getAttributeValue(0);
        if (!expAttr.equals(actualText)) {
            failStrings("Attribute value incorrect (CR-escaping: "+escaping+", encoding: "+encoding+"; writer: "+sw+")", expAttr, actualText);
        }

        assertTokenType(CHARACTERS, sr.next());
        actualText = getAndVerifyText(sr);
        if (!expElem.equals(actualText)) {
            failStrings("Element value incorrect (CR-escaping: "+escaping+", encoding: "+encoding+", writer "+sw+")", expElem, actualText);
        }
        assertTokenType(END_ELEMENT, sr.next());
        assertEquals("root", sr.getLocalName());
    }

    private XMLOutputFactory2 getFactory(int type, boolean escapeCr)
        throws XMLStreamException
    {
        XMLOutputFactory2 f = getOutputFactory();
        // type 0 -> non-ns, 1 -> ns, non-repairing, 2 -> ns, repairing
        setNamespaceAware(f, type > 0); 
        setRepairing(f, type > 1); 

        f.setProperty(WstxOutputProperties.P_OUTPUT_ESCAPE_CR, escapeCr ? Boolean.TRUE : Boolean.FALSE);

        return f;
    }
}

