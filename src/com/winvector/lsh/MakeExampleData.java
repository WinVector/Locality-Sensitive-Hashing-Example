package com.winvector.lsh;

import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;


public final class MakeExampleData {

	/**
	 * 
	 * @param rand
	 * @return 0/1 random data
	 */
	public static SparseIIVec[] buildExample(final Random rand, final int dim) {
		final int weightTarget = 20; // number of 1s we expect
		final int m = 10000;         // number of data rows to write
		final int nearDiffTarget = 3; // rough number of differences in near vector

		// build random data
		final Set<SparseIIVec> vecs = new TreeSet<SparseIIVec>();
		while(vecs.size()<m/2) {
			// build a vector with weightTarget 1 uniformly at random (assumes weightTarget<<dim)
			final SortedMap<Integer,Integer> rowAsMap = new TreeMap<Integer,Integer>();
			while(rowAsMap.size()<weightTarget) {
				final int index = rand.nextInt(dim);
				rowAsMap.put(index,1);
			}
			{
				final SparseIIVec vec = new SparseIIVec(rowAsMap);
				vecs.add(vec);
			}
		}

		{ // salt in some near neighbors
			final SparseIIVec[] arr = vecs.toArray(new SparseIIVec[vecs.size()]);		// build access to initial vectors
			while(vecs.size()<m) {
				final SparseIIVec v = arr[rand.nextInt(arr.length)];
				final SortedMap<Integer,Integer> rowAsMap = v.toMap();
				for(int j=0;j<nearDiffTarget;++j) { 
					final int index = rand.nextInt(dim);
					rowAsMap.put(index,1);
				}
				final SparseIIVec vec = new SparseIIVec(rowAsMap);
				vecs.add(vec);
			}
		}
		return vecs.toArray(new SparseIIVec[vecs.size()]);
	}
}
