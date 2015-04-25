
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Verificatum.
 *
 * Verificatum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Verificatum is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Verificatum.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package verificatum.util;

/**
 * Provides a simple way of dividing work performed component-wise on
 * one or more arrays on multiple cores. {@link LargeInteger} contains
 * examples of how this is used.
 *
 * @author Douglas Wikstrom
 */
public abstract class ArrayWorker {

    /**
     * Size of input arrays.
     */
    protected int size;

    /**
     * Creates an instance with the given size.
     *
     * @param size Size of instance.
     */
    public ArrayWorker(int size) {
        this.size = size;
    }

    /**
     * Performs the work delegated to a given core.
     *
     * @param start Starting index for the work delegated to the
     * active core.
     * @param end Ending index for the work delegated to the active core.
     */
    public abstract void work(int start, int end);

    /**
     * Performs the work of this instance.
     */
    public void work() {
        ArrayWorker.divideWork(this, size, 0);
    }

    /**
     * Performs the work of this instance.
     *
     * @param threshold Threshold below which no delegation of work to
     * several cores takes place.
     */
    public void work(int threshold) {
        ArrayWorker.divideWork(this, size, threshold);
    }

    /**
     * Determines the number of cores on the machine and divides the
     * work encapsulated in <code>worker</code> on this number of
     * threads.
     *
     * @param worker Encapsulation of work to be done.
     * @param size Length of arrays.
     * @param threshold Threshold value of number of array length,
     * where threading is used.
     */
    public static void divideWork(final ArrayWorker worker,
                                  final int size,
                                  int threshold) {

        // We only thread for large arrays.
        if (threshold > 0 && size <= threshold) {

            worker.work(0, size);
            return;

        } else {

            // This (supposedly) returns the number of cores available
            // to the JVM.
            int cores =
                Math.min(Runtime.getRuntime().availableProcessors(), size);

            final int perCore = size / cores;
            Thread[] threads = new Thread[cores];

            // Start a thread for each core.
            for (int l = 0; l < cores; l++) {

                int start = l * perCore;
                int end = (l + 1) * perCore;
                if (l == cores - 1) {
                    end = size;
                }
                threads[l] = new WorkerThread(worker, start, end) {};
                threads[l].setPriority(java.lang.Thread.MAX_PRIORITY);
                threads[l].start();
            }

            // Wait for all threads to complete before returning.
            for (int l = 0; l < threads.length; l++) {
                try {
                    threads[l].join();
                } catch (InterruptedException ie) {
                    throw new Error("Failed to join threads!", ie);
                }
            }
        }
    }
}

class WorkerThread extends Thread {
    int start;
    int end;
    ArrayWorker worker;

    WorkerThread(ArrayWorker worker, int start, int end) {
        this.start = start;
        this.end = end;
        this.worker = worker;
    }

    public void run() {
        worker.work(start, end);
    }
}