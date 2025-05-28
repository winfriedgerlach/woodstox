package winne;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, batchSize = 1000)
@Measurement(iterations = 10, time = 1, batchSize = 1000)
public class IsNameChar {

//    @Param({"1000", "100", "10", "5", "1"})
//    private int size;
//    private char[] original = new char[10_000];
    private Random random;
    private BitSet bitSet;
    private boolean[] booleans;
    private long[] longs;

    @Setup(Level.Iteration)
    public void setup() {
        random = new Random(System.currentTimeMillis());

        bitSet = new BitSet();
        booleans = new boolean[256];
        longs = new long[256];
        IntStream.rangeClosed(0x61, 0x7A).forEach(i -> { // a-z
            bitSet.set(i);
            booleans[i] = true;
            longs[i] = 1;
        });
        IntStream.rangeClosed(0x41, 0x5A).forEach(i -> { // A-Z
            bitSet.set(i);
            booleans[i] = true;
            longs[i] = 1;
        });
        IntStream.rangeClosed(0x30, 0x39).forEach(i -> { // 0-9
            bitSet.set(i);
            booleans[i] = true;
            longs[i] = 1;
        });
        bitSet.set('.');
        booleans['.'] = true;
        longs['.'] = 1;
        bitSet.set('-');
        booleans['-'] = true;
        longs['-'] = 1;
        bitSet.set('_');
        booleans['_'] = true;
        longs['_'] = 1;
    }

    @Benchmark
    public boolean ManyBranches() {
        char c = (char) random.nextInt(256);
        // First, let's handle 7-bit ascii range
        if (c <= 0x7A) { // 'z' or earlier
            if (c >= 0x61) { // 'a' - 'z' are ok
                return true;
            }
            if (c <= 0x5A) {
                if (c >= 0x41) { // 'A' - 'Z' ok too
                    return true;
                }
                // As are 0-9, '.' and '-'
                return (c >= 0x30 && c <= 0x39) || (c == '.') || (c == '-');
            }
            return (c == 0x5F); // '_' is ok too
        }
        return false;
    }

    @Benchmark
    public boolean bitSet() {
        char c = (char) random.nextInt(256);
        // First, let's handle 7-bit ascii range
        if (c <= 0x7A) { // 'z' or earlier
            return bitSet.get(c);
        }
        return false;
    }

    @Benchmark
    public boolean withBooleans() {
        char c = (char) random.nextInt(256);
        // First, let's handle 7-bit ascii range
        if (c <= 0x7A) { // 'z' or earlier
            return booleans[c];
        }
        return false;
    }

    @Benchmark
    public boolean withLongs() {
        char c = (char) random.nextInt(256);
        // First, let's handle 7-bit ascii range
        if (c <= 0x7A) { // 'z' or earlier
            return longs[c] > 0;
        }
        return false;
    }

}
