import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;


public class TestTrie {
    @Test
    public void testTrie() {
        TrieSet s = new TrieSet(128);
        s.put("sam", "Sam");
        s.put("same", "Same");
        s.put("shazam", "Shazam");
        s.put("apple", "Apple");
        ArrayList<String> result = s.getAll("s");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("Sam");
        expected.add("Same");
        expected.add("Shazam");
        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    public void testEmptyTrie() {
        TrieSet s = new TrieSet(128);
        ArrayList<String> result = s.getAll("s");
        assertEquals(0, result.size());
    }

    @Test
    public void testNonMatching() {
        TrieSet s = new TrieSet(128);
        s.put("sam", "Sam");
        s.put("same", "Same");
        s.put("apple", "Apple");
        ArrayList<String> result = s.getAll("b");
        assertEquals(0, result.size());
    }

}
