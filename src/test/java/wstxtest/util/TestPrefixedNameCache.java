package wstxtest.util;

import com.ctc.wstx.util.PrefixedNameCache;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PrefixedNameCache}.
 */
public class TestPrefixedNameCache
    extends wstxtest.BaseJUnit4Test
{
    @Test
    public void testNames()
    {
        PrefixedNameCache cache = new PrefixedNameCache();
        // Many more names than cache slots, so they will also collide
        String[] prefixes = new String[20];
        String[] localNames = new String[50];
        for (int i = 0; i < prefixes.length; ++i) {
            prefixes[i] = "p" + i;
        }
        for (int i = 0; i < localNames.length; ++i) {
            localNames[i] = "name" + i;
        }
        for (int round = 0; round < 2; ++round) {
            for (String prefix : prefixes) {
                for (String ln : localNames) {
                    assertEquals(prefix + ":" + ln, cache.get(prefix, ln));
                }
            }
        }
    }

    @Test
    public void testSameInstanceWhenCached()
    {
        PrefixedNameCache cache = new PrefixedNameCache();
        String prefix = "soap";
        String ln = "Envelope";
        String name = cache.get(prefix, ln);
        assertEquals("soap:Envelope", name);
        assertSame(name, cache.get(prefix, ln));
    }

    // Equal but different String instances must still give the right name
    @Test
    public void testNonCanonicalInput()
    {
        PrefixedNameCache cache = new PrefixedNameCache();
        assertEquals("a:b", cache.get("a", "b"));
        assertEquals("a:b", cache.get(new String("a"), new String("b")));
        assertEquals("a:c", cache.get(new String("a"), "c"));
    }
}
