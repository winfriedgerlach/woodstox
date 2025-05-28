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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 10, batchSize = 1000)
@Measurement(iterations = 3, time = 10, batchSize = 1000)
public class StringTest {

    @State(Scope.Thread)
    public static class MyState {

        List<String> strings = new ArrayList<>();
        List<String> strings2 = new ArrayList<>();
        List<char[]> chars = new ArrayList<>();
        List<char[]> chars2 = new ArrayList<>();
        char[] strBuf = new char[4];

        @Setup(Level.Trial)
        public void doSetup() {
            System.out.println("Filling test data");
            Random random = new Random();
            for (int i = 0; i < 1024*1024; i++) {
                int ri = random.nextInt(1_000_000);
                String randomString = Integer.valueOf(ri).toString();
                strings.add(randomString);
                chars2.add(randomString.toCharArray());
                if (random.nextInt(10) < 5) {
                    strings2.add(randomString);
                    chars.add(randomString.toCharArray());
                } else {
                    String otherRandomString = randomString.substring(0, randomString.length() - 1) + "o";
                    strings2.add(otherRandomString);
                    chars.add(otherRandomString.toCharArray());
                }
            }
            System.out.println("Test data generated");
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown");
        }

    }

//    @Benchmark
//    public void testString(MyState state, Blackhole blackhole) {
//        for (int i = 0; i < state.strings.size(); i++) {
//            boolean isEqual = state.strings.get(i).equals(state.strings2.get(i));
////            System.out.println("isEqual? " + isEqual);
//            blackhole.consume(isEqual);
//        }
//
//    }
//
//    @Benchmark
//    public void testChar(MyState state, Blackhole blackhole) {
//        for (int i = 0; i < state.strings.size(); i++) {
//            boolean isEqual = Arrays.equals(state.strings.get(i).toCharArray(), state.chars.get(i));
////            System.out.println("isEqual? " + isEqual);
//            blackhole.consume(isEqual);
//        }
//
//    }
//
//    @Benchmark
//    public void testCharVsChar(MyState state, Blackhole blackhole) {
//        for (int i = 0; i < state.strings.size(); i++) {
//            boolean isEqual = Arrays.equals(state.chars2.get(i), state.chars.get(i));
////            System.out.println("isEqual? " + isEqual);
//            blackhole.consume(isEqual);
//        }
//
//    }

    @Benchmark
    public void testLoopedChar(MyState state, Blackhole blackhole) {
        for (int j = 0; j < state.strings.size(); j++) {
            String sym = state.strings.get(j);
            char[] buf = state.chars.get(j);
            int len = sym.length();

            int i = 0;

            do {
                if (sym.charAt(i) != buf[i]) {
                    break;
                }
            } while (++i < len);
            boolean isEqual = (i == len);
            blackhole.consume(isEqual);
        }

    }

    @Benchmark
    public void testLoopedChar2(MyState state, Blackhole blackhole) {
        for (int j = 0; j < state.strings.size(); j++) {
            String sym = state.strings.get(j);
            char[] buf = state.chars.get(j);
            int len = sym.length();

            boolean isEqual = false;
            for (int i = 0; i < len; i++) {
                if (sym.charAt(i) != buf[i]) {
                    break;
                }
                if (i == len - 1) {
                    isEqual = true;
                    break;
                }
            }
            blackhole.consume(isEqual);
        }

    }

    @Benchmark
    public void testLoopedCharStep2(MyState state, Blackhole blackhole) {
        for (int j = 0; j < state.strings.size(); j++) {
            String sym = state.strings.get(j);
            char[] buf = state.chars.get(j);
            int len = sym.length();
            int remainder = len % 2;
            len -= remainder;

            boolean isEqual = false;
            for (int i = 0; i < len; i+=2) {
                sym.getChars(i, i+2, state.strBuf, 0);
                if (state.strBuf[0] != buf[i] || state.strBuf[1] != buf[i+1]) {
                    break;
                }
                if (i == len - 2) {
                    isEqual = true;
                    break;
                }
            }
            if (isEqual && remainder == 1) {
                isEqual = sym.charAt(len) == buf[len];
            }
            blackhole.consume(isEqual);
        }

    }

    @Benchmark
    public void testLoopedCharJustOne(MyState state, Blackhole blackhole) {
        for (int j = 0; j < state.strings.size(); j++) {
            String sym = state.strings.get(j);
            char[] buf = state.chars.get(j);
            int len = sym.length();

            boolean isEqual = false;
            for (int i = 0; i < len; i++) {
                sym.getChars(i, i+1, state.strBuf, 0);
                if (state.strBuf[0] != buf[i]) {
                    break;
                }
                if (i == len - 1) {
                    isEqual = true;
                    break;
                }
            }
            blackhole.consume(isEqual);
        }
    }

    @Benchmark
    public void testLoopedCharStep4(MyState state, Blackhole blackhole) {
        for (int j = 0; j < state.strings.size(); j++) {
            String sym = state.strings.get(j);
            char[] buf = state.chars.get(j);
            int len = sym.length();
            int remainder = len % 4;
            len -= remainder;
            if (len < 0) len = 0;

            boolean isEqual = false;
            for (int i = 0; i < len; i+=4) {
                sym.getChars(i, i+4, state.strBuf, 0);
                if (state.strBuf[0] != buf[i]
                        || state.strBuf[1] != buf[i+1]
                        || state.strBuf[2] != buf[i+2]
                        || state.strBuf[3] != buf[i+3]) {
                    break;
                }
                if (i == len - 4) {
                    isEqual = true;
                    break;
                }
            }
            if (isEqual) {
                if (remainder == 1) {
                    isEqual = sym.charAt(len) == buf[len];
                } else if (remainder == 2) {
                    isEqual = (sym.charAt(len) == buf[len] && sym.charAt(len+1) == buf[len+1]);
                } else if (remainder == 3) {
                    isEqual = (sym.charAt(len) == buf[len]
                            && sym.charAt(len+1) == buf[len+1]
                            && sym.charAt(len+2) == buf[len+2]);
                }
            }
            blackhole.consume(isEqual);
        }
    }


}
