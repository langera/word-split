package test.parallel;

import com.google.common.collect.Iterables;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class FlexibleDivide implements WordSplit
{
    private static final int PARALLEL_LEVEL = Integer.parseInt(System.getProperty("PARALLEL_LEVEL", "4"));

    private final Worker[] workers = new Worker[PARALLEL_LEVEL];
    private final Iterable<String>[] results = new Iterable[PARALLEL_LEVEL];

    public Iterable<String> words(final String text)
    {
        final CountDownLatch countDownLatch = new CountDownLatch(PARALLEL_LEVEL);

        final int size = text.length();
        final int lastWorkerIndex = PARALLEL_LEVEL - 1;
        final int chunk = size / PARALLEL_LEVEL;
        for (int i = 0; i < lastWorkerIndex; i++)
        {
            final int from = i * chunk;
            final int to = from + chunk;
            startWorker(workers, results, text, countDownLatch, i, from, to, i > 0);
        }
        startWorker(workers, results, text, countDownLatch, lastWorkerIndex, lastWorkerIndex * chunk, size, true);

        waitForWorkersToFinish(countDownLatch);

        return buildResults(results);
    }

    static void waitForWorkersToFinish(final CountDownLatch countDownLatch)
    {
        boolean done = false;

        while (!done)
        {
            try
            {
                countDownLatch.await();
                done = true;
            }
            catch (InterruptedException e)
            {
                // do nothing
            }
        }
    }

    static void startWorker(final Worker[] workers, final Iterable<String>[] results,
                            final String text, final CountDownLatch countDownLatch,
                            final int index, final int from, final int to, final boolean ignoreFirst)
    {
        workers[index] = new Worker(results, index, text, from, to, countDownLatch, ignoreFirst);
        new Thread(workers[index]).start();
    }

    static Iterable<String> buildResults(final Iterable<String>[] results)
    {
        return Iterables.concat(results);
    }

    static class Worker implements Runnable
    {
        private final String text;
        private final int from;
        private final int to;
        private final CountDownLatch countDownLatch;
        private final boolean ignoreFirstChunk;
        private List<String> segment = new LinkedList<String>();

        Worker(final Iterable<String>[] results, final int index,
               final String text, final int from, final int to,
               final CountDownLatch countDownLatch, final boolean ignoreFirstChunk)
        {
            this.text = text;
            this.from = from;
            this.to = to;
            this.countDownLatch = countDownLatch;
            this.ignoreFirstChunk = ignoreFirstChunk;
            results[index] = segment;
        }

        public void run()
        {
            words();
            countDownLatch.countDown();
        }

        private void words()
        {
            int i = from;
            int firstChar = -1;
            char c;

            final int length = text.length();
            while (i < to || (firstChar != -1 && i < length))
            {
                c = text.charAt(i);
                if (firstChar == -1 && c != ' ')
                {
                    firstChar = i;
                }
                else if (firstChar != -1 && c == ' ')
                {
                    segment.add(text.substring(firstChar, i));
                    firstChar = -1;
                }
                i++;
            }
            if (i >= length)
            {
                segment.add(text.substring(firstChar, length));
            }
            if (ignoreFirstChunk && text.charAt(from) != ' ')
            {
                segment.remove(0);
            }
        }
    }

}
