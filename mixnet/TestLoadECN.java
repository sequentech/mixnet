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

import java.lang.reflect.*;

/**
 * Implements a class that tries to load the native code of
 * ECN. This is used by the configure script.
 *
 * @author Douglas Wikstrom
 */
public class TestLoadECN {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

	Class klass = null;

	try {
	    klass = Class.forName("jecn.ECN");
	    if (args.length > 0) {
		Method method = klass.getMethod("loadNative");
		method.invoke((Object)null, new Object[0]);
	    }
	} catch (ClassNotFoundException cnfe) {
	    System.out.println("Can not locate the class jecn.ECN!");
	} catch (NoSuchMethodException nsme) {
	    // Never happens.
	} catch (InvocationTargetException ite) {
	    // Never happens.
	} catch (SecurityException se) {
	    System.out.print("Not allowed to load the native library " +
			     "(libgmp.{la,so,a}) needed by " +
			     "jecn.ECN to run in native mode!");
	} catch (UnsatisfiedLinkError ule) {
	    System.out.print("Library (libgmp.{la,so,a}) does not " +
			     "exist, which is needed by " +
			     "jecn.ECN to run in native mode!");
	} catch (IllegalAccessException iae) {
	    // Never happens.
	} catch (IllegalArgumentException iare) {
	    // Never happens.
	} catch (NullPointerException npe) {
	    // This can not happen.
	}
    }
}
