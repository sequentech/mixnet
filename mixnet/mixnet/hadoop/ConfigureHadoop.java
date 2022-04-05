/*
 * Copyright 2011 Eduardo Robles Elvira <edulix AT wadobo DOT com>
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

package mixnet.hadoop;

import mixnet.eio.ByteTree;
import mixnet.eio.GenericIO;
import mixnet.eio.IOWrapper;
import mixnet.arithm.LargeIntegerArray;
import mixnet.arithm.LargeIntegerArrayWrapper;
import mixnet.arithm.HugeArray;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import java.util.UUID;
import java.lang.reflect.*;
import java.io.IOException;

/**
 * Configures Hadoop.
 *
 * @author Eduardo Robles Elvira
 */
public class ConfigureHadoop {

    /**
     * Configure Hadoop.
     */
    public static void configure() {

        GenericIO.getLog().info("inside ConfigureHadoop::configure()");
        try {

            ByteTree.setFileNodeIndexing();
            Configuration conf = new Configuration();

            // The code that follows is equivalent to the following
            // commented code, but using reflection:
            //
            // LargeIntegerArray.
            // setLargeIntegerArrayWrapper(
            //                 new LargeIntegerArrayHadoopWrapper());

            String className =
                "org.mixnet.hadoop.LargeIntegerArrayHadoopWrapper";
            Class<?> cls = Class.forName(className);

            Constructor<?> ct = cls.getConstructor();
            Object arglist[] = {};
            LargeIntegerArrayWrapper wrapper =
                (LargeIntegerArrayWrapper)ct.newInstance();

            LargeIntegerArray.setLargeIntegerArrayWrapper(wrapper);

            String randomPath = "/mixnet-" + UUID.randomUUID().toString();
            HugeArray.init(randomPath);

            // The code that follows is equivalent to the following commented
            // code, but using reflection:
            //
            // HadoopIOWrapper hiow = new HadoopIOWrapper(FileSystem.get(conf));
            // GenericIO.setIOWrapper(hiow);

            Class<?> cls2 =
                Class.forName("org.mixnet.hadoop.HadoopIOWrapper");
            Class<?> partypes2[] = {Configuration.class};
            Constructor<?> ct2 = cls2.getConstructor(partypes2);
            Object arglist2[] = {conf};
            IOWrapper wrapper2 = (IOWrapper)ct2.newInstance(arglist2);

            GenericIO.setIOWrapper(wrapper2);

        } catch (InstantiationException  ie) {
            GenericIO.getLog().info("InstantiationException  ie");
            // Never happens.
        } catch (ClassNotFoundException cnfe) {
            GenericIO.getLog().info("ClassNotFoundException cnfe");
            System.out.println("Can not locate the class " +
                               "org.mixnet.hadoop.LargeIntegerArrayHadoopWrapper!");
        } catch (NoSuchMethodException nsme) {
            GenericIO.getLog().info("NoSuchMethodException nsme");
            // Never happens.
        } catch (InvocationTargetException ite) {
            GenericIO.getLog().info("InvocationTargetException ite");
            // Never happens.
        } catch (SecurityException se) {
            GenericIO.getLog().info("SecurityException se");
            System.out.print("Not allowed to load the hadoop project library " +
                             "(largeintegerarrayoperation.jar) needed to use hadoop!");
        } catch (UnsatisfiedLinkError ule) {
            GenericIO.getLog().info("UnsatisfiedLinkError ule");
            System.out.print("Library largeintegerarrayoperation.jar does " +
                             "not exists and is needed to use hadoop!");
        } catch (IllegalAccessException iae) {
            GenericIO.getLog().info("IllegalAccessException iae");
            // Never happens.
        } catch (IllegalArgumentException iare) {
            GenericIO.getLog().info("IllegalArgumentException iare");
            // Never happens.
        } catch (NullPointerException npe) {
            GenericIO.getLog().info("NullPointerException npe");
            // This can not happen.
        }
    }
}
