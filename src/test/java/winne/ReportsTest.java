package winne;

import com.ctc.wstx.sax.WstxSAXParserFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReportsTest {

    static final String PARTICIPANT_NS = "http://standards.ieee.org/downloads/11073/11073-10207-2017/participant";

    @State(Scope.Thread)
    public static class MyState {

        byte[] mdibBytes;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        WstxSAXParserFactory saxParserFactory = new WstxSAXParserFactory();

        @Setup(Level.Trial)
        public void doSetup() throws URISyntaxException, IOException {
            mdibBytes = Files.readAllBytes(Paths.get(getClass().getResource("/winne/mdib.xml").toURI()));
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            saxParserFactory.setNamespaceAware(true);
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown");
        }

    }

    @Benchmark
    public void testParseMdib(MyState state, Blackhole blackhole) throws XMLStreamException {

        XMLEventReader xmlEventReader = state.xmlInputFactory.createXMLEventReader(new ByteArrayInputStream(state.mdibBytes));

        while (xmlEventReader.hasNext()) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                if (startElement.getName().getNamespaceURI().equals(PARTICIPANT_NS)
                        && startElement.getName().getLocalPart().equals("Type")) {
                    Attribute code = startElement.getAttributeByName(new QName("Code"));
                    //System.out.println("found type with code " + code.getValue());
                    blackhole.consume(code.getValue());
                }
            }
        }
    }
}
