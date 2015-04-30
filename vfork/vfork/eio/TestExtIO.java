
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import vfork.crypto.PRGHeuristic;
import vfork.crypto.RandomSource;
import vfork.test.TestParameters;
import vfork.util.SimpleTimer;

/**
 * Tests some of the functionality of {@link ExtIO} and associated
 * classes.
 *
 * @author Chris Culnane
 */
public class TestExtIO {

    

    // public static boolean runTestSort(TestParameters tp) throws Exception {

    //     RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());
    //     File tempFile = File.createTempFile("veri", ".temptxt");
    //     tempFile.deleteOnExit();
    //     BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
    //     //Generate file of 2.5 million random 1024 byte stings
    //     	for(int i=0;i<2500000;i++){
    //     	//Create random file of 
    //     	ByteTree bt = generateRandomByteTree(rs, 128);
    //     	bw.write(Hex.toHexString(bt.toByteArray()));
    //     	bw.newLine();
    //     	}
    //     	bw.close();
    //     	File tempNativeSort = File.createTempFile("veri", ".temptxt");
    //     	tempNativeSort.deleteOnExit();
    //     	File tempJavaSort = File.createTempFile("veri", ".temptxt");
    //     tempJavaSort.deleteOnExit();
    //     long startTimeNative = System.currentTimeMillis();
    //     ExtIO.oldsort(tempFile, tempNativeSort);
    //     long endTimeNative = System.currentTimeMillis();
    //     long startTimeJava = System.currentTimeMillis();
    //     	ExtIO.sort(tempFile,tempJavaSort);
    //     	long endTimeJava = System.currentTimeMillis();
    //     	if(!ExtIO.equals(tempNativeSort, tempJavaSort)){
    //     		throw new Exception("Native and Java sort do not create the equal files");
    //     	}
    //     	long totalTimeNative =endTimeNative-startTimeNative;
    //     	long totalTimeJava =endTimeJava-startTimeJava;
    //     	System.out.println("NativeTime:" + totalTimeNative);
    //     	System.out.println("JavaTime:" + totalTimeJava);
    //     	//if(totalTimeJava> (totalTimeNative*4)){
    //     	//	throw new Exception("Java sort is taking more than 4 times as long as native sort");
    //     	//}
    //     	return true;
    //     }

    protected static ByteTree generateRandomByteTree(RandomSource rs,
                                               int totalSize) {
        byte[] content = new byte[totalSize];
        rs.getBytes(content);
        return new ByteTree(content);
    }
}
