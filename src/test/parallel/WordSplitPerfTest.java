package test.parallel;

import com.lewisd.jmicrobench.PerformanceTest;
import com.lewisd.jmicrobench.PerformanceTestController;
import com.lewisd.jmicrobench.PerformanceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

@RunWith(PerformanceTestRunner.class)
@PerformanceTest(groupName="Parallel")
public class WordSplitPerfTest
{
    private static final String ALGORITHM = System.getProperty("ALGORITHM");
    private static final int NUMBER_OF_BOOKS = Integer.parseInt(System.getProperty("NUMBER_OF_BOOKS", "3"));

    private PerformanceTestController performanceTestController;
    private String text = readAlice();
    private WordSplit algorithm;

    @Before
    public void setUp() throws Exception
    {
        performanceTestController = new PerformanceTestController();
        algorithm = getWordSplit();
    }

    @Test
    public void runPerfTest()
    {
        performanceTestController.startDurationTimer();
        Iterable<String> words = algorithm.words(text);
        performanceTestController.stopDurationTimer();
        performanceTestController.addNumberOfOperations(countWords(words));
    }

    private static int countWords(final Iterable<String> words)
    {
        int counter = 0;
        final Iterator<String> iterator = words.iterator();
        while (iterator.hasNext())
        {
            counter++;
            iterator.next();
        }
        return counter;
    }

    private enum Algorithms
    {
        BruteForce(new BruteForce()),
        SimpleDivideAndConquer(new SimpleDivideAndConquer()),
        UnevenSimpleDivideAndConquer(new UnevenSimpleDivideAndConquer()),
        FlexibleDivide(new FlexibleDivide()),
        UnevenFlexibleDivide(new UnevenFlexibleDivide());

        private final WordSplit wordSplit;

        private Algorithms(final WordSplit wordSplit)
        {
            this.wordSplit = wordSplit;
        }

        public WordSplit getWordSplit()
        {
            return wordSplit;
        }
    }

    private static WordSplit getWordSplit()
    {
        return Algorithms.valueOf(ALGORITHM).getWordSplit();
    }

    static String readAlice()
    {
        try
        {
            char[] buffer = new char[164000 * NUMBER_OF_BOOKS];
            int offset = 0;
            for (int i = 0; i < NUMBER_OF_BOOKS; i++)
            {
                FileReader reader = new FileReader(new File("alice30.txt"));
                int bytesRead;
                int size = 4096;
                while ((bytesRead = reader.read(buffer, offset, size)) > -1)
                {
                    offset += bytesRead;
                    size = Math.min(4096, buffer.length - offset);
                }
            }
            return new String(buffer, 0, offset);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);

        }

    }
}