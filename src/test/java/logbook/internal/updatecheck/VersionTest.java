package logbook.internal.updatecheck;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class VersionTest {

    /**
     * {@link logbook.internal.updatecheck.Version#Version(int, int, int)} のためのテスト・メソッド。
     */
    @Test
    public void testVersionIntIntInt() {
        Version v = new Version(3, 4, 5);
        assertEquals(3, v.getMajor());
        assertEquals(4, v.getMinor());
        assertEquals(5, v.getRevision());
    }

    /**
     * {@link logbook.internal.updatecheck.Version#Version(java.lang.String)} のためのテスト・メソッド。
     */
    @Test
    public void testVersionString() {
        {
            Version v = new Version("5");
            assertEquals(5, v.getMajor());
            assertEquals(0, v.getMinor());
            assertEquals(0, v.getRevision());
        }
        {
            Version v = new Version("5.6");
            assertEquals(5, v.getMajor());
            assertEquals(6, v.getMinor());
            assertEquals(0, v.getRevision());
        }
        {
            Version v = new Version("5.6.7");
            assertEquals(5, v.getMajor());
            assertEquals(6, v.getMinor());
            assertEquals(7, v.getRevision());
        }
        {
            Version v = new Version("5.0.0");
            assertEquals(5, v.getMajor());
            assertEquals(0, v.getMinor());
            assertEquals(0, v.getRevision());
        }
    }

    /**
     * {@link logbook.internal.updatecheck.Version#compareTo(logbook.internal.updatecheck.Version)} のためのテスト・メソッド。
     */
    @Test
    public void testCompareTo() {
        {
            Version v1 = new Version("1.2.2");
            Version v2 = new Version("2.2.2");

            assertEquals(-1, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("1.2.2");
            Version v2 = new Version("1.2.2");

            assertEquals(0, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("2.2.2");
            Version v2 = new Version("1.2.2");

            assertEquals(1, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("2.1.2");
            Version v2 = new Version("2.2.2");

            assertEquals(-1, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("2.2.2");
            Version v2 = new Version("2.1.2");

            assertEquals(1, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("2.2.1");
            Version v2 = new Version("2.2.2");

            assertEquals(-1, v1.compareTo(v2));
        }
        {
            Version v1 = new Version("2.2.2");
            Version v2 = new Version("2.2.1");

            assertEquals(1, v1.compareTo(v2));
        }
    }

    /**
     * {@link logbook.internal.updatecheck.Version#getCurrent()} のためのテスト・メソッド。
     */
    @Test
    public void testGetCurrent() {
        Version expected = Version.UNKNOWN;

        assertEquals(expected, Version.getCurrent());
    }

}
