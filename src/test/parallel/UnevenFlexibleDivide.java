package test.parallel;

import java.util.concurrent.CountDownLatch;

public final class UnevenFlexibleDivide implements WordSplit
{
    private static final int PARALLEL_LEVEL = Integer.parseInt(System.getProperty("PARALLEL_LEVEL", "4"));

    private final FlexibleDivide.Worker[] workers = new FlexibleDivide.Worker[PARALLEL_LEVEL];
    private final Iterable<String>[] results = new Iterable[PARALLEL_LEVEL - 1];

    public Iterable<String> words(final String text)
    {
        final int numOfWorkers = PARALLEL_LEVEL - 1;
        final CountDownLatch countDownLatch = new CountDownLatch(numOfWorkers);

        final int size = text.length();
        final int chunk = size / PARALLEL_LEVEL;
        final int lastWorkerIndex = numOfWorkers - 1;
        workers[0] = new FlexibleDivide.Worker(results, 0, text, 0, chunk * 2, countDownLatch, false);
        for (int i = 1; i < lastWorkerIndex; i++)
        {
            final int from = (i + 1) * chunk;
            final int to = from + chunk;
            FlexibleDivide.startWorker(workers, results, text, countDownLatch, i, from, to, true);
        }
        FlexibleDivide.startWorker(workers, results,
                text, countDownLatch, lastWorkerIndex, numOfWorkers * chunk, size, true);

        workers[0].run();

        FlexibleDivide.waitForWorkersToFinish(countDownLatch);

        return FlexibleDivide.buildResults(results);
    }
}
