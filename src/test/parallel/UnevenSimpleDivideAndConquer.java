package test.parallel;

import java.util.concurrent.CountDownLatch;

public final class UnevenSimpleDivideAndConquer implements WordSplit
{
    private static final int PARALLEL_LEVEL = Integer.parseInt(System.getProperty("PARALLEL_LEVEL", "4"));

    private final SimpleDivideAndConquer.Worker[] workers = new SimpleDivideAndConquer.Worker[PARALLEL_LEVEL];

    public Iterable<String> words(final String text)
    {
        final int numOfWorkers = PARALLEL_LEVEL - 1;
        final CountDownLatch countDownLatch = new CountDownLatch(numOfWorkers);

        final int size = text.length();
        final int chunk = size / PARALLEL_LEVEL;
        final int lastWorkerIndex = numOfWorkers - 1;
        workers[0] = new SimpleDivideAndConquer.Worker(text, 0, chunk * 2, countDownLatch);
        for (int i = 1; i < lastWorkerIndex; i++)
        {
            final int from = (i + 1) * chunk;
            final int to = from + chunk;
            SimpleDivideAndConquer.startWorker(workers, text, countDownLatch, i, from, to);
        }
        SimpleDivideAndConquer.startWorker(workers, text, countDownLatch, lastWorkerIndex, numOfWorkers * chunk, size);

        workers[0].run();

        SimpleDivideAndConquer.waitForWorkersToFinish(countDownLatch);

        return SimpleDivideAndConquer.buildResult(workers, numOfWorkers);
    }
}
