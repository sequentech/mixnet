/*
 * Copyright 2011 Chris Culnane
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

package vfork.eio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Provides a reference to a subfile of the MergeSort. Reads in one
 * line at a time so the files can be recombined in a sorted order.
 *
 * @author Chris Culnane
 */
public class TempMergeFile {

    /**
     * Name of underlying temporary file.
     */
    private File tempFile;

    /**
     * Reader of underlying file.
     */
    private BufferedReader br;

    /**
     * Current line.
     */
    private String currentLine="";

    /**
     * Takes reference to temp subfile of the the MergeSort.
     *
     * @param tempFile file to read in one line at a time
     * @throws IOException if the file does not exist or is
     * not readable
     */
    public TempMergeFile(File tempFile) throws IOException {

        this.tempFile = tempFile;
        br = new BufferedReader(new FileReader(tempFile));

        // Read first line
        currentLine = br.readLine();

        // File could be empty if main file sub-divides perfectly,
        // in which case close it immediately.
        if (currentLine == null){
            br.close();
            tempFile.delete();
        }
    }

    /**
     * Gets the current line of this file, does not do a read
     * operation and therefore does not move to the next line in the
     * file. Use this when doing comparisons between subfiles. Use
     * {@link TempMergeFile#useCurrentLine()} to move to the next
     * line.
     *
     * @return the current line
     */
    public String getCurrentLine(){
        return currentLine;
    }

    /**
     * Moves to the next line in the file and returns that.  It is
     * assumed that you have already got a copy of what was the
     * current line, having read it during the comparison stage.
     *
     * @return the next line
     * @throws IOException
     */
    public String useCurrentLine() throws IOException{
        if (currentLine == null){

            return null;

        } else {

            currentLine = br.readLine();
            if (currentLine == null){
                br.close();
                tempFile.delete();
                return null;
            }
            return currentLine;
        }
    }
}
