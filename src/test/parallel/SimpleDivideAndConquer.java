package test.parallel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class SimpleDivideAndConquer implements WordSplit
{
    private static final int PARALLEL_LEVEL = Integer.parseInt(System.getProperty("PARALLEL_LEVEL", "4"));

    private final Worker[] workers = new Worker[PARALLEL_LEVEL];

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
            startWorker(workers, text, countDownLatch, i, from, to);
        }
        startWorker(workers, text, countDownLatch, lastWorkerIndex, lastWorkerIndex * chunk, size);

        waitForWorkersToFinish(countDownLatch);

        return buildResult(workers, PARALLEL_LEVEL);
    }

    static void startWorker(final Worker[] workers,
                            final String text,
                            final CountDownLatch countDownLatch,
                            final int index, final int from, final int to)
    {
        workers[index] = new Worker(text, from, to, countDownLatch);
        new Thread(workers[index]).start();
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

    static Iterable<String> buildResult(final Worker[] workers, final int numOfWorkers)
    {
        int sz = 0;
        for (int i = 0; i < numOfWorkers; i++)
        {
            sz += workers[i].segment.size();
        }
        workers[numOfWorkers - 1].lastIsAChunk = false;
        List<String> result = new ArrayList<String>(sz);
        String lastChunk = null;
        for (int i = 0; i < numOfWorkers; i++)
        {
            if (lastChunk != null)
            {
                if (workers[i].firstIsAChunk)
                {
                    result.add(lastChunk + workers[i].segment.remove(0));
                }
                else
                {
                    result.add(lastChunk);
                }
            }
            result.addAll(workers[i].segment);
            lastChunk = (workers[i].lastIsAChunk) ? result.remove(result.size() - 1) : null;
        }
        return result;
    }

    static class Worker implements Runnable
    {
        private final String text;
        private final int from;
        private final int to;
        private final CountDownLatch countDownLatch;
        private List<String> segment = new LinkedList<String>();
        private boolean firstIsAChunk;
        private boolean lastIsAChunk;

        Worker(final String text, final int from, final int to, final CountDownLatch countDownLatch)
        {
            this.text = text;
            this.from = from;
            this.to = to;
            this.countDownLatch = countDownLatch;
        }

        public void run()
        {
            words();
            countDownLatch.countDown();
        }

        private void words()
        {
            int i = to;
            int lastChar = -1;
            char c;

            lastIsAChunk = text.charAt(to - 1) != ' ';
            while (--i >= from)
            {
                c = text.charAt(i);
                if (lastChar == -1 && c != ' ')
                {
                    lastChar = i;
                }
                else if (lastChar != -1)
                {
                    if (c == ' ')
                    {
                        segment.add(0, text.substring(i + 1, lastChar + 1));
                        lastChar = -1;
                    }
                }
            }
            if (lastChar != -1)
            {
                segment.add(0, text.substring(from, lastChar + 1));
            }
            firstIsAChunk = (text.charAt(from) != ' ');
        }
    }
}
