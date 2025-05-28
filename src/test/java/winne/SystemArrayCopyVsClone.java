package winne;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, batchSize = 1000)
@Measurement(iterations = 10, time = 1, batchSize = 1000)
public class SystemArrayCopyVsClone {

    @Param({"1000", "100", "10", "5", "1"})
    private int size;
    private char[] original;
    private char[] dest;

    @Setup
    public void setup() {
        Random random = new Random(System.currentTimeMillis());
        original = new char[size];
        for (int i = 0; i < size; i++) {
            original[i] = (char) random.nextInt(1_000_000);
        }
        dest = new char[size];
    }

    @Benchmark
    public char[] SystemArrayCopy() {
        final int length = size;
        char[] destination = new char[length];
        System.arraycopy(original, 0, destination, 0, length);
        return destination;
    }

    @Benchmark
    public char[] SystemArrayCopyCache() {
        System.arraycopy(original, 0, dest, 0, original.length);
        return dest;
    }

    @Benchmark
    public char[] arrayClone() {
        return original.clone();
    }

    @Benchmark
    public char[] arraysCopyOf() {
        return Arrays.copyOf(original, original.length);
    }

//        public static void main(String[] args) throws RunnerException {
//            Options opt = new OptionsBuilder()
//                    .include(ArrayCopyTest.class.getSimpleName())
//                    .build();
//
//            new Runner(opt).run();
//        }
}
