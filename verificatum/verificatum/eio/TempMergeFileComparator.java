/*
 * Copyright 2011 Chris Culnane
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
package verificatum.eio;

import java.util.Comparator;

/**
 * Provides a comparator for comparing the {@link TempMergeFile} files
 * that point to the sub files. It takes the current line of each file
 * and returns a comparison of them. This is used by the {@link PriorityQueue}
 * to automatically sort the files to determine which one to read 
 * from next
 *
 * @author Chris Culnane
 *
 */
public class TempMergeFileComparator implements Comparator<TempMergeFile> {

	
	@Override
	public int compare(TempMergeFile arg0, TempMergeFile arg1) {
		return arg0.getCurrentLine().compareTo(arg1.getCurrentLine());
	}

}
