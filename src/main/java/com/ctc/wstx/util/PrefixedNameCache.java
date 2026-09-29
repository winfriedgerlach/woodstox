package com.ctc.wstx.util;

/**
 * Small cache of "prefix:localName" Strings, to avoid building them over and
 * over again for the same names (like SAX qualified names of elements and
 * attributes).
 *<p>
 * Prefixes and local names are expected to be canonical (coming from the
 * same symbol table), so they are compared by identity; for others, the
 * name just gets built again. Not thread-safe.
 */
public final class PrefixedNameCache
{
    // Power of two
    private final static int SIZE = 64;

    private final String[] mPrefixes = new String[SIZE];

    private final String[] mLocalNames = new String[SIZE];

    private final String[] mPrefixedNames = new String[SIZE];

    /**
     * @return "prefix:localName" for given prefix and local name
     */
    public String get(String prefix, String localName)
    {
        int ix = (31 * prefix.hashCode() + localName.hashCode()) & (SIZE - 1);
        if (mPrefixes[ix] == prefix && mLocalNames[ix] == localName) {
            return mPrefixedNames[ix];
        }
        String name = prefix + ":" + localName;
        mPrefixes[ix] = prefix;
        mLocalNames[ix] = localName;
        mPrefixedNames[ix] = name;
        return name;
    }
}
