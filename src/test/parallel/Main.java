package test.parallel;

import java.util.Iterator;

public final class Main
{
    public static void main(String[] args) throws Exception
    {
        String text = WordSplitPerfTest.readAlice();
        Iterable<String> bruteForce = new BruteForce().words(text);
        Iterable<String> simple = new SimpleDivideAndConquer().words(text);
        Iterable<String> unevenSimple = new UnevenSimpleDivideAndConquer().words(text);
        Iterable<String> flexible = new FlexibleDivide().words(text);
        Iterable<String> unevenFlexible = new UnevenFlexibleDivide().words(text);

        checkEquality(bruteForce, simple, unevenSimple, flexible, unevenFlexible);
    }

    private static void checkEquality(final Iterable<String>... iterables)
    {
        int i = 0;
        Iterable<String> toCompareTo = null;
        for (Iterable<String> iterable : iterables)
        {
            System.out.println(i);
            if (toCompareTo == null)
            {
                toCompareTo = iterable;
            }
            else
            {
                Iterator<String> iterator = toCompareTo.iterator();
                for (String word : iterable)
                {
                    if (!iterator.hasNext() || !word.equals(iterator.next()))
                    {
                        throw new IllegalStateException("NOT_IDENTICAL_RESULTS ["+ i +"]");
                    }
                }
            }
            i++;
        }
    }
}
