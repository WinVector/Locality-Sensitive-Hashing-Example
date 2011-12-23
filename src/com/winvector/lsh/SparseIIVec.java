package com.winvector.lsh;

import java.util.SortedMap;


public final class SparseIIVec extends SparseVecBase<Integer,Integer> {
	private int[] keys;
	private int[] values;
	
	public SparseIIVec(final SortedMap<Integer, Integer> x) {
		super(x);
	}

	@Override
	protected void alloc(int n) {
		keys = new int[n];
		values = new int[n];
	}

	@Override
	public int size() {
		return keys.length;
	}

	@Override
	protected void setKey(int index, Integer k) {
		keys[index] = k;
	}

	@Override
	protected void setValue(int index, Integer v) {
		values[index] = v;
	}

	@Override
	public Integer getKey(int index) {
		return keys[index];
	}

	@Override
	public Integer getValue(int index) {
		return values[index];
	}
}
