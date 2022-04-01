
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Vfork.
 *
 * Vfork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Vfork is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Vfork.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package mixnet.eio;

import java.io.*;
import java.util.*;
import java.nio.channels.FileChannel;

import mixnet.ui.*;

/**
 * Provides utility functions for file related operations.
 *
 * @author Douglas Wikstrom
 */
public class ExtIO {

    /**
     * Number of bytes in buffer used for copying.
     */
    final static int COPY_BUFFER_SIZE = 2048;

    /**
     * Returns a new array containing the same data as the input, but
     * prepended with the length of the input array as an integer
     * represented by four bytes.
     *
     * @param data Original array.
     * @return Array containing the length.
     */
    public static byte[] lengthEmbedded(byte[] data) {
        byte[] res = new byte[data.length + 4];
        ExtIO.writeInt(res, 0, data.length);
        System.arraycopy(data, 0, res, 4, data.length);
        return res;
    }

    /**
     * Removes length embedding, but in a verifiable way.
     *
     * @param data Original array.
     * @return Array without the length as a prefix.
     * @throws EIOException if the embedded length is inconsistent
     * with the actual length of the input.
     */
    public static byte[] lengthDebedded(byte[] data)
    throws EIOException {
        int len = ExtIO.readInt(data, 0);
        if (len > data.length - 4) {
            throw new EIOException("Embedded length is too large!");
        }
        return Arrays.copyOfRange(data, 4, len + 4);
    }

    /**
     * Writes an <code>int</code> as four bytes.
     *
     * @param result Destination array.
     * @param offset Index where to start writing.
     * @param n Value to write.
     */
    public static void writeInt(byte[] result, int offset, int n) {
        result[offset++] = (byte)(n >>> 24 & 0xff);
        result[offset++] = (byte)(n >>> 16 & 0xff);
        result[offset++] = (byte)(n >>> 8 & 0xff);
        result[offset] = (byte)(n & 0xff);
    }

    /**
     * Writes an <code>int[]</code> as a <code>byte[]</code>.
     *
     * @param result Destination array.
     * @param roffset Index where to start writing.
     * @param ints Values to write.
     * @param ioffset Index where to start reading.
     * @param len Number of integers to read.
     */
    public static void writeInts(byte[] result, int roffset,
                                 int[] ints, int ioffset,
                                 int len) {
        for (int i = ioffset; i < len; i++) {
            result[roffset++] = (byte)((ints[i] >> 24) & 0xFF);
            result[roffset++] = (byte)((ints[i] >> 16) & 0xFF);
            result[roffset++] = (byte)((ints[i] >> 8) & 0xFF);
            result[roffset++] = (byte)(ints[i] & 0xFF);
        }
    }

    /**
     * Reads an <code>int</code> from a <code>byte[]</code>.
     *
     * @param bytes Source of the integer.
     * @param offset Index where to start reading.
     * @return Integer that is read.
     */
    public static int readInt(byte[] bytes, int offset) {
        int n = bytes[offset++] & 0xFF;
        n <<= 8;
        n |= bytes[offset++] & 0xFF;
        n <<= 8;
        n |= bytes[offset++] & 0xFF;
        n <<= 8;
        n |= bytes[offset] & 0xFF;
        return n;
    }

    /**
     * Reads an <code>int[]</code> from a <code>byte[]</code>.
     *
     * @param result Destination array.
     * @param roffset Index where to start writing.
     * @param bytes Values to write.
     * @param boffset Index where to start reading.
     * @param len Number of bytes to read.
     */
    public static void readInts(int[] result, int roffset,
                                byte[] bytes, int boffset,
                                int len) {
        for (int i = roffset; i < len; i++) {

            int n = bytes[boffset++] & 0xFF;
            n <<= 8;
            n |= bytes[boffset++] & 0xFF;
            n <<= 8;
            n |= bytes[boffset++] & 0xFF;
            n <<= 8;
            n |= bytes[boffset++] & 0xFF;

            result[i] = n;
        }
    }

    /**
     * Writes a <code>short</code> as two bytes.
     *
     * @param result Destination array.
     * @param i Index where to start writing.
     * @param n The <code>short</code> value to write.
     */
    public static void writeShort(byte[] result, int i, short n) {
        result[i++] = (byte)(n >>> 8 & 0xff);
        result[i] = (byte)(n & 0xff);
    }

    /**
     * Reads a <code>short</code> from a <code>byte[]</code>. It is
     * assumed that there is a multiple of 2 bytes in the input.
     *
     * @param source Source array.
     * @param i Index where to start reading.
     * @return Integer value at index <code>i</code> in
     * <code>result</code>.
     */
    public static short readShort(byte[] source, int i) {
        short n = 0;
        n |= source[i++] & 0xFF;
        n <<= 8;
        n |= source[i] & 0xFF;
        return n;
    }

    /**
     * Attempts to close the given object, and if this fails it throws
     * an error. Recall that by convention closable objects can be
     * closed any number of times. Thus, this catches only actual
     * errors occurring during closing and not multiple calls.
     *
     * @param closeable Object that can be closed.
     * @throws IOError If the object throws an
     * <code>IOException</code> during closing.
     */
    public static void strictClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }
    }

    /**
     * Copies a file.
     *
     * @param sourceFile Contents to be copied.
     * @param destinationFile Destination of copied content.
     *
     * @throws FileNotFoundException If one of the files do not exist,
     * is a directory rather than a regular file, or for some other
     * reason cannot be opened for reading or writing.
     * @throws SecurityException If a security manager exists and its
     * <code>checkRead</code>/<code>checkWrite</code> method denies
     * access to a file.
     * @throws IOException If some other I/O error occurs.
     */
    public static void copyFile(File sourceFile, File destinationFile)
        throws FileNotFoundException, SecurityException, IOException {

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(destinationFile);
            copyFile(sourceFile, fos);

        } finally {
            ExtIO.strictClose(fos);
        }
    }

    /**
     * Writes the contents of the source file to the destination
     * stream.
     *
     * @param sourceFile Contents to be copied.
     * @param destStream Destination of copied content.
     *
     * @throws FileNotFoundException If the source file does not
     * exist, is a directory rather than a regular file, or for some
     * other reason cannot be opened for reading.
     * @throws SecurityException If a security manager exists and its
     * <code>checkRead</code>/<code>checkWrite</code> method denies
     * access to a file.
     * @throws IOException If some other I/O error occurs.
     */
    public static void copyFile(File sourceFile, OutputStream destStream)
        throws FileNotFoundException, SecurityException, IOException {

        FileInputStream fis = null;
        try {

            fis = new FileInputStream(sourceFile);

            byte[] buf = new byte[COPY_BUFFER_SIZE];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                destStream.write(buf, 0, len);
            }

        } finally {
            ExtIO.strictClose(fis);
        }
    }

    /**
     * Returns the complete contents of the file as a string. It is
     * the responsibility of the programmer to make sure that the
     * contents of the file is not too large.
     *
     * @param file Location of string.
     * @return Contents of file.
     *
     * @throws FileNotFoundException If the file does not exist, is a
     * directory rather than a regular file, or for some other reason
     * cannot be opened for reading.
     * @throws IOException If an I/O error occurs.
     */
    public static String readString(File file)
        throws FileNotFoundException, IOException {

        StringBuilder sb = new StringBuilder();

        FileInputStream fis = null;
        try {

            fis = new FileInputStream(file);

            byte[] buf = new byte[COPY_BUFFER_SIZE];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }

        } finally {
            ExtIO.strictClose(fis);
        }

        return sb.toString();
    }

    /**
     * Write string to file.
     *
     * @param file Destination of string.
     * @param s String to be written.
     *
     * @throws IOException If a file can not be opened or written as
     * needed.
     */
    public static void writeString(File file, String s) throws IOException {

        Writer bw = new BufferedWriter(new FileWriter(file));
        try {
            bw.write(s);
        } finally {
            bw.close();
        }
    }

    /**
     * Atomic write of a string to file. This is "atomic" in the sense
     * that the contents first are written to the temporary file,
     * which is then renamed. Renaming may not be atomic.
     *
     * @param file Final destination of string.
     * @param tmpFile Temporary file used to implement atomic write.
     * @param s String to be written.
     *
     * @throws IOException If a file can not be opened or written as
     * needed.
     */
    public static void atomicWriteString(File tmpFile, File file, String s)
    throws IOException {

        // Delete target file if it exists.
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Unable to delete file! (" + file + ")");
            }
        }

        // Write contents to temporary file.
        writeString(tmpFile, s);

        // Rename temporary file to target file.
        if (!tmpFile.renameTo(file)) {
            throw new IOException("Unable to rename temporary file! (" +
                                  tmpFile + " to " + file + ")");
        }
    }

    /**
     * Returns a random access file containing the contents of the
     * input stream. No changes made to the random access file are
     * forwarded to the source file. It is the responsibility of the
     * programmer to make sure that the contents are not too large.
     *
     * @param is Stream to be converted.
     * @return Random access file.
     *
     * @throws IOException If the translation fails.
     */
    public static RandomAccessFile asRandomAccessFile(InputStream is)
        throws IOException {

        File tmpFile = File.createTempFile("isc", "tmp");
        RandomAccessFile raf = new RandomAccessFile(tmpFile, "rwd");

        byte[] buf = new byte[COPY_BUFFER_SIZE];
        int len = 0;

        while ((len = is.read(buf)) != -1) {
            raf.write(buf, 0, len);
        }

        raf.seek(0);

        return raf;
    }

    /**
     * Writes the line-wise sorted content of the input file to the
     * output file.
     *
     * @param input Contains the content to be copied.
     * @param output Destination of sorted lines.
     *
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is
     * interrupted by another thread.
     */
    public static void sort(File input, File output)
        throws IOException, InterruptedException {

    	// Create reader for main file
    	BufferedReader br = new BufferedReader(new FileReader(input));

    	// This will hold the references to the subsorted files
    	ArrayList<TempMergeFile> tempMergeFiles =
            new ArrayList<TempMergeFile>();

        // Array to hold the strings to do the in memory subsort
        ArrayList<String> inMemorySort = new ArrayList<String>();

        // Setup threshold - how big each subsort is. I've currently
        // fixed this at 60mb, I've tried dynamic allocation, but the
        // performance was worse. The bottleneck is in file i/o.
        int inMemoryByteCount = 0;

        // (int) Math.round(Runtime.getRuntime().totalMemory()*0.8);
        // 1048576 * 100;
        int inMemoryByteThreshold = 1048576 * 60;

        // Read each line of the files
        String line;
        while ((line = br.readLine()) != null) {

            // Add the length of the line to the counter
            inMemoryByteCount = inMemoryByteCount + line.getBytes().length;

            // Add the line to the array
            inMemorySort.add(line);

            // If we are over the threshold, sort the in memory data
            // and write it out to a temp file
            if (inMemoryByteCount > inMemoryByteThreshold) {

                Collections.sort(inMemorySort);
                writeToFile(inMemorySort, tempMergeFiles);

                //reset the counter
                inMemoryByteCount = 0;

            }
        }

        // We've reached the end of the file, now sort and write out
        // the remaining data that is in memory
        if (inMemorySort.size() > 0) {
            Collections.sort(inMemorySort);
            writeToFile(inMemorySort,tempMergeFiles);
            inMemoryByteCount = 0;
        }

        //Close the stream
        br.close();

        // Create a PriorityQueue contain a reference to each
        // subfile. Pass the custom comparator which will sort those
        // files based on the top line of each file
        PriorityQueue<TempMergeFile> pq =
            new PriorityQueue<TempMergeFile>(tempMergeFiles.size(),
                                             new TempMergeFileComparator());

        // Add all the references to the temp files, this will
        // create sort them at the same time.
        pq.addAll(tempMergeFiles);
        TempMergeFile current;

        //Create an outputfile for the sorted output
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));

        // Loop through until the PriorityQueue is empty (once all
        // data from all files has been read). The poll method removes
        // the top most file
        while ((current = pq.poll()) != null) {

            // If the currentline is null we have reached the end of
            // that file, do nothing, since we have already removed it
            // from the PriorityQueue
            if (current.getCurrentLine() != null) {

                // Write out the current line to the output file
                bw.write(current.getCurrentLine());
                bw.newLine();

                // Get the next line from this file, if it is null we
                // have reached the end of the file, so do nothing if
                // it is not null place it back into the PriorityQueue
                // which will automatically sort it into the correct
                // location
                if (current.useCurrentLine() != null) {
                    pq.add(current);
                }
            }
        }
        bw.close();
    }

    /**
     * Provides a utility method for the java merge sort to write
     * the contents of a subsort to a file. This has been branched
     * off into a separate method to try and improve performance.
     *
     * @param tempOutputFolder the folder where this file should be saved
     * @param inMemorySort the array of strings to be written
     * @param tempMergeFiles the array of subfiles that we want to add
     * the created file to
     * @throws IOException if there is an error writing to the file
     */
    private static void writeToFile(ArrayList<String> inMemorySort,
                                    ArrayList<TempMergeFile> tempMergeFiles)
        throws IOException {

        // // Create a temp file with suffic and prefix
    	// File tempOutputFile = File.createTempFile("verimergesort", ".msort");

    	// // Mark the file for deletion at the end of the execution
    	// // (note, this will be the end of the JVM execution, not this
    	// // method or class.
    	// tempOutputFile.deleteOnExit();

        File tempOutputFile = TempFile.getFile();

    	// Create a file writer and buffer
        FileWriter fw = new FileWriter(tempOutputFile);
        BufferedWriter bw = new BufferedWriter(fw);

        // Iterate through the string in the array and write them out.
        Iterator<String> it = inMemorySort.iterator();
        while (it.hasNext()) {
            bw.write(it.next());
            bw.newLine();
        }

        // Flush and close the file
        bw.flush();
        bw.close();
        fw.close();

        // Add a reference to this new sub merge file and clear the in
        // memory array.
        tempMergeFiles.add(new TempMergeFile(tempOutputFile));
        inMemorySort.clear();
    }

    // NATIVE
    /**
     * Writes the line-wise sorted content of the input file to the
     * output file.
     *
     * @param input Contains the content to be copied.
     * @param output Destination of sorted lines.
     *
     * @throws SecurityException If a security manager exists and
     * doesn't allow creation of a subprocess.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is
     * interrupted by another thread.
     */
    public static void oldsort(File input, File output)
        throws IOException, InterruptedException {

        Runtime runtime = Runtime.getRuntime();

        // String command = "sort --output=" + output.toString() +
        //     " " + input.toString();

        String command = "vsort " + input.toString() + " " + output.toString();

        Process proc = runtime.exec(command);

        proc.getErrorStream().close();
        proc.getInputStream().close();
        proc.getOutputStream().close();

        // Make sure "sort" has finished processing before returning.
        proc.waitFor();
    }

    /**
     * Copies a directory recursively.
     *
     * @param inputDir Contains the content to be copied.
     * @param outputDir Destination directory.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException This should be removed
     */
    public static void recursiveCopyDir(File inputDir, File outputDir)
    throws IOException, InterruptedException {

    	// Create a stack to store the directories requiring copying
    	// We use our own stack instead of recursion to avoid the
    	// danger of stack overflow error.
    	Stack<File> dirStack = new Stack<File>();

    	// Place the top most directory onto the stack.
    	dirStack.push(inputDir);
    	File currentFile;

    	// Loop until there are no more directories to copy.
    	while (!dirStack.isEmpty()) {

            // Get the next directory.
            currentFile = dirStack.pop();

            // Avoid copying to ourselves.
            if (!currentFile.equals(outputDir)) {

                // Create the output directory we will copy to
                // Replaces the top level input directory with the top
                // level output directory
                String absPath = currentFile.getAbsolutePath();
                File copyToDirectory =
                    new File(absPath.replaceFirst(inputDir.getAbsolutePath(),
                                                  outputDir.getAbsolutePath()));

                // Create the directory
                copyToDirectory.mkdirs();

                // Copy the contents of the directory
                processDirectory(currentFile, copyToDirectory, dirStack);
            }
    	}
    }

    /**
     * Does the actual copying of files in the directory.
     *
     * @param dir The directory to process.
     * @param outputDir The directory to copy to.
     * @param dirStack The stack of directories that still need processing.
     * @throws IOException If an I/O error occurs
     */
    private static void processDirectory(File dir, File outputDir,
                                         Stack<File> dirStack)
        throws IOException {

    	// Loop through all files (they could also be additional
    	// directories). If they are directories add them to the stack
    	// to be copied later. If it is a file copy it.

    	for (File f: dir.listFiles()) {

            if (f.isDirectory()) {

                dirStack.push(f);

            } else {

                // Create FileChannel to do the actual copying This
                // should be quicker than using FileOutputStream
                // directly as it should be handled by the underlying
                // OS.
                FileChannel fc = new FileInputStream(f).getChannel();

                String fileName =
                    outputDir.getAbsolutePath().concat("/" + f.getName());
                FileChannel oc =
                    new FileOutputStream(fileName).getChannel();

                fc.transferTo(0, f.length(), oc);

                fc.close();
                oc.close();
            }
    	}
    }

    /**
     * Returns true or false depending on the contents of the two
     * files are identical or not.
     *
     * @param file1 First file.
     * @param file2 First file.
     * @return <code>true</code> or <code>false</code> depending on if
     * the contents of the input files are identical or not.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static boolean equals(File file1, File file2)
    throws IOException {

        if (file1.length() != file2.length()) {
            return false;
        }

        BufferedInputStream bis1 =
            new BufferedInputStream(new FileInputStream(file1));
        BufferedInputStream bis2 =
            new BufferedInputStream(new FileInputStream(file2));

        int byte1;
        int byte2;
        do {

            byte1 = bis1.read();
            byte2 = bis2.read();

        } while (byte1 == byte2 && byte1 != -1);

        return byte1 == -1;
    }

    // NATIVE
    /**
     * Returns true or false depending on the contents of the two
     * files are identical or not.
     *
     * @param file1 First file.
     * @param file2 First file.
     * @return <code>true</code> or <code>false</code> depending on if
     * the contents of the input files are identical or not.
     *
     * @throws SecurityException If a security manager exists and
     * doesn't allow creation of a subprocess.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is
     * interrupted by another thread.
     */
    public static boolean oldequals(File file1, File file2)
        throws IOException, InterruptedException {

        Runtime runtime = Runtime.getRuntime();

        String command = "diff " + file2.toString() + " " + file1.toString();

        Process proc = runtime.exec(command);

        proc.getErrorStream().close();
        proc.getInputStream().close();
        proc.getOutputStream().close();

        // Make sure "diff" has finished processing before returning.
        return proc.waitFor() == 0;
    }

    /**
     * Deletes a file or a directory and all its contents.
     *
     * @param path File or directory to be deleted.
     */
    public static boolean delete(File path) {

        if (!path.exists()) {
            return true;
        }

        if (path.isFile()) {
            return path.delete();
        }

        String[] list = path.list();

        if (list == null) {
            return path.delete();
        }

        for (int i = 0; i < list.length; i++) {

            File childPath = new File(path, list[i]);

            if (childPath.isDirectory()) {

                if (!ExtIO.delete(childPath)) {
                    return false;
                }

            } else {

                if (!childPath.delete()) {
                    return false;
                }
            }
        }
        return path.delete();
    }
}
