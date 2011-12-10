package test.parallel;

import java.util.LinkedList;
import java.util.List;

public final class BruteForce implements WordSplit
{
    public Iterable<String> words(final String text)
    {
        List<String> result = new LinkedList<String>();

        int i = text.length();
        int lastChar = -1;

        while (--i != -1)
        {
            if (lastChar == -1 && text.charAt(i) != ' ')
            {
                lastChar = i;
            }
            else if (lastChar != -1)
            {
                if (text.charAt(i) == ' ')
                {
                    result.add(0, text.substring(i + 1, lastChar + 1));
                    lastChar = -1;
                }
            }
        }
        if (lastChar != -1)
        {
            result.add(0, text.substring(0, lastChar + 1));
        }

        return result;
    }
}
