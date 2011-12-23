package com.winvector.lsh;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Base class for what we call weak sparse vectors implemented without objects (see SparseIIVec for an example).
 * Weak in the sense we can interate over keys and values, but not quickly lookup from keys to values.
 * Specialized to allow low object count implementations (usually a parallel list of primative keys and primiative values). 
 * @author johnmount
 *
 * @param <K> key type (usually something with a primitive representation like Integer)
 * @param <V> value type (usually something with a primitive representation lint Integer or Double)
 */
public abstract class SparseVecBase<K extends Comparable<K>,V extends Number> implements Comparable<SparseVecBase<K,V>> { 

	public SparseVecBase(final SortedMap<K,V> x) {
		final ArrayList<K> nzIndices = new ArrayList<K>(x.size());
		for(final Map.Entry<K,V> me: x.entrySet()) {
			final V vi = me.getValue();
			if((vi!=null)&&(vi.doubleValue()!=0.0)) {
				nzIndices.add(me.getKey());
			}
		}
		final int nindices = nzIndices.size();
		alloc(nindices);
		for(int index=0;index<nindices;++index) {
			final K kv = nzIndices.get(index);
			setKey(index,kv);
			setValue(index,x.get(kv));
		}
	}
	
	public SortedMap<K,V> toMap() {
		final SortedMap<K,V> mp = new TreeMap<K,V>();
		final int k = size();
		for(int i=0;i<k;++i) {
			mp.put(getKey(i),getValue(i));
		}
		return mp;
	}
	
	protected abstract void alloc(int n);
	protected abstract void setKey(int index, K k);
	protected abstract void setValue(int index, V v);
	
	public abstract int size();
	public abstract K getKey(int index);
	public abstract V getValue(int index);
	
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("{");
		final int n = size();
		for(int i=0;i<n;++i) {
			if(i>0) {
				b.append(",");
			}
			b.append("" + getKey(i) + ":" + getValue(i));
		}
		b.append("}");
		return b.toString();
	}


	public double dot(final SparseVecBase<K,V> o) {
		final int n1 = size();
		final int n2 = o.size();
		double tot = 0;
		int i1 = 0;
		int i2 = 0;
		out:
		while((i1<n1)&&(i2<n2)) {
			// advance indexes to common key
			while(getKey(i1).compareTo(o.getKey(i2))!=0) {
				while(getKey(i1).compareTo(o.getKey(i2))<0) {
					++i1;
					if(i1>=n1) {
						break out; // ran out, no more matches possible
					}
				}
				while(o.getKey(i2).compareTo(getKey(i1))<0) {
					++i2;
					if(i2>=n2) {
						break out; // ran out, not more matches possible
					}
				}
			}
			// now have i1<n1, i2<n2 getKey(i1]==getKey(i2]
			tot += getValue(i1).doubleValue()*o.getValue(i2).doubleValue();
			++i1;
			++i2;
		}
		return tot;
	}


	@Override
	public int compareTo(final SparseVecBase<K,V> o) {
		final int n = size();
		if(n!=o.size()) {
			if(n>=o.size()) {
				return 1;
			} else {
				return -1;
			}
		}
		for(int i=0;i<n;++i) {
			final int cmp = getKey(i).compareTo(o.getKey(i));
			if(cmp!=0) {
				return cmp;
			}
		}
		for(int i=0;i<n;++i) {
			final double diff = getValue(i).doubleValue()-o.getValue(i).doubleValue();
			if(diff>0) {
				return 1;
			} else if(diff<0) {
				return -1;
			}
		}
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object o) {
		return compareTo((SparseVecBase<K,V>)o)==0;
	}
}

