package winne;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Random;

import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 5, batchSize = 1000)
@Measurement(iterations = 5, time = 5, batchSize = 1000)
public class MaskVsOr {

    @Param({"1000", "100", "5"})
    private int size;
    private int[] types;
    private static final int MASK_COMMENT_OR_INSTRUCTION =
            (1 << COMMENT) | (1 << PROCESSING_INSTRUCTION);

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random(System.currentTimeMillis());
        types = new int[size];
        for (int i = 0; i < types.length; i++) {
            types[i] = random.nextInt(15) + 1;
        }
    }

    @Benchmark
    public void usingOr(Blackhole blackhole) {
        for (int type : types) {
            blackhole.consume(type == COMMENT || type == PROCESSING_INSTRUCTION);
        }
    }

    @Benchmark
    public void usingMask(Blackhole blackhole) {
        for (int type : types) {
            blackhole.consume(((1 << type) & MASK_COMMENT_OR_INSTRUCTION) != 0);
        }
    }
}
