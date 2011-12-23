package com.winvector.lsh;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;


public final class BuildNbhds {

	private static final class SimRec implements Comparable<SimRec> {
		public final int nbhdId1;
		public final int nbhdId2;
		public final double sim;
		public final int randv;  // used to break ties on distance comparison
		
		public SimRec(final int nbhdId1, final int nbhdId2, final double sim, final int randv) {
			this.nbhdId1 = Math.min(nbhdId1,nbhdId2);
			this.nbhdId2 = Math.max(nbhdId1,nbhdId2);
			this.sim = sim;
			this.randv = randv;
		}

		@Override
		public int compareTo(final SimRec o) {
			if(sim!=o.sim) {
				if(sim>=o.sim) {
					return 1;
				} else {
					return -1;
				}
			}
			if(randv!=o.randv) {
				if(randv>=o.randv) {
					return 1;
				} else {
					return -1;
				}
			}
			if(nbhdId1!=o.nbhdId1) {
				if(nbhdId1>=o.nbhdId1) {
					return 1;
				} else {
					return -1;
				}
			}
			if(nbhdId2!=o.nbhdId2) {
				if(nbhdId2>=o.nbhdId2) {
					return 1;
				} else {
					return -1;
				}
			}
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			return compareTo((SimRec)o)==0;
		}
		
	}

	// config
	public int nreps = 1;
	public int nbhdTarget = 2;
	public int largeThreshold = 100;
	public int maxPopCount = 20;
	public int partialInsepctionSize = 0;
		
	private final class NBHDrec implements Comparable<NBHDrec> {
		public final int nbhd_id;
		public double threshold;
		public long sortKey = 0;
		public final SparseIIVec target;
		public final double norm;
		// these two structures must be altered in parallel
		public final Set<Integer> nbhdIds; // used to prevent same record from entering nbhg more than once to to floating point non-determinism
		public final SortedSet<SimRec> nbhd = new TreeSet<SimRec>();
		
		/**
		 * 
		 * @param nbhd_id
		 * @param target
		 * @param scale altered (normalized) on way in, don't normalize before
		 */
		public NBHDrec(final int nbhd_id, final SparseIIVec target) {
			this.nbhd_id = nbhd_id;
			this.target = target;
			nbhdIds = new HashSet<Integer>(2*nbhdTarget+13); // prevents dups due to non-determinism of floating point comparisons
			threshold = 0.0;
			norm = Math.sqrt(target.dot(target));
		}
		
		public int otherNbhdID(final SimRec s) {
			if(s.nbhdId1==nbhd_id) {
				return s.nbhdId2;
			} else {
				return s.nbhdId1;
			}
		}
		
		public boolean want(final double sim, final SparseIIVec other, final int oNbhdId) {
			return (oNbhdId!=nbhd_id)&&(sim>=threshold)&&(!nbhdIds.contains(oNbhdId));
		}
		
		public double sim(final NBHDrec o) {
			final double dot = target.dot(o.target);
			if(dot<=0.0) {
				return 0.0;
			} else {
				return dot/(norm*o.norm);
			}
		}
		
		/**
		 * assumes want() is true
		 * @param sim
		 * @param s
		 * @param other
		 */
		public void take(final SimRec s) {
			final int oNbhdIdS = otherNbhdID(s);
			if(!nbhdIds.contains(oNbhdIdS)) {
				nbhd.add(s);
				nbhdIds.add(oNbhdIdS);
				if(nbhd.size()>nbhdTarget) {
					// order is increasing sim (larger sim closer and more desired)
					final SimRec victim = nbhd.first(); 
					final int oNbhdIdV = otherNbhdID(victim); 
					nbhd.remove(victim);
					nbhdIds.remove(oNbhdIdV);
					final SimRec v1 = nbhd.first(); 
					threshold = Math.max(threshold,v1.sim);
				}
			}
		}
		
		@Override
		public int compareTo(final NBHDrec o) {
			if(sortKey!=o.sortKey) {
				if(sortKey>=o.sortKey) {
					return 1;
				} else {
					return -1;
				}
			}
			if(nbhd_id!=o.nbhd_id) {
				if(nbhd_id>=o.nbhd_id) {
					return 1;
				} else {
					return -1;
				}
			}
			return 0;
		}
	}
	

	private static final MessageDigest md;
	static {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}	
	}

	private static long hashCode(final int maxPop, final Map<Integer,Integer> posnCodes, final SparseIIVec vec) {
		final int n = vec.size();
		final byte[] inbytes = new byte[maxPop];
		// any entries not in vector implicitly left coded to 0
		for(int i=0;i<n;++i) {
			final int p = vec.getKey(i);
			final Integer bitPosn = posnCodes.get(p);
			if(null!=bitPosn) {
				final int v = vec.getValue(i);
				final byte code = (byte)Math.min(v,0x7f);
				inbytes[bitPosn] = code;
			}
		}
		md.reset();
		final byte[] outbytes = md.digest(inbytes);
		final long h = ((long)Arrays.hashCode(outbytes)) + (((long)Arrays.hashCode(inbytes))<<32);
		md.reset();
		return h;
	}
	
	private static void inspectNBHDPair(final Random rand,
			final NBHDrec nbhdLeft, final NBHDrec nbhdRight) {
		final double sim = nbhdLeft.sim(nbhdRight);
		final boolean leftWants = nbhdLeft.want(sim,nbhdRight.target,nbhdRight.nbhd_id);
		final boolean rightWants = nbhdRight.want(sim,nbhdLeft.target,nbhdLeft.nbhd_id);
		if(leftWants||rightWants) {
			final int randv = rand.nextInt();
			final SimRec simRec = new SimRec(nbhdLeft.nbhd_id,nbhdRight.nbhd_id,sim,randv);
			if(leftWants) {
				nbhdLeft.take(simRec);
			}
			if(rightWants) {
				nbhdRight.take(simRec);
			}
		}
	}

	/**
	 * return a uniform random one to one map of a subset of size popcount ids to [0...popcount-1]
	 * @param ids 
	 * @param rand
	 * @param popcount
	 * @return
	 */
	private static SortedMap<Integer, Integer> randMap(final Integer[] ids,
			final Random rand, final int popcount) {
		final int nids = ids.length;
		final SortedMap<Integer,Integer> hashBits = new TreeMap<Integer,Integer>();
		for(int i=0;(i<nids)&&(hashBits.size()<popcount);++i) {
			final int nTakesRemaining = popcount - hashBits.size();
			final int nDonorsRemaining = nids-i;
			final boolean take = (nTakesRemaining>=nDonorsRemaining)||(rand.nextInt(nDonorsRemaining)<nTakesRemaining);
			if(take) {
				hashBits.put(ids[i],hashBits.size());
			}
		}
		return hashBits;
	}

	
	public NBHDrec[] inspectForNearNeighbors(final boolean bruteForce, final SparseIIVec[] vecs, final Random rand) {
		final int nvecs = vecs.length;
		final Integer[] ids;
		{ // get set of active dimension ids
			final Set<Integer> idSet = new TreeSet<Integer>();
			for(final SparseIIVec v: vecs) {
				final int k = v.size();
				for(int ii=0;ii<k;++ii) {
					idSet.add(v.getKey(ii));
				}
			}
			ids = idSet.toArray(new Integer[idSet.size()]);
		}
		final int nids = ids.length;
		// build record keeping structure
		final NBHDrec[] nbhds = new NBHDrec[nvecs];
		for(int i=0;i<nvecs;++i) {
			nbhds[i] = new NBHDrec(i,vecs[i]);			
		}
		if(bruteForce) {
			for(int left=0;left<nvecs-1;++left) {
				final NBHDrec nbhdLeft = nbhds[left];
				for(int right=left+1;right<nvecs;++right) {
					final NBHDrec nbhdRight = nbhds[right];
					inspectNBHDPair(rand, nbhdLeft, nbhdRight);
				}
			}
		} else {
			// try projections of various sizes
			for(int popcount=1;popcount<=Math.min(maxPopCount,nids-1);++popcount) {
				System.out.println("\tworking on pop " + popcount + "\t" + new Date());
				int inspectedSlots = 0;
				final int totalSlots = nreps*nvecs;
				for(int repitition=0;repitition<nreps;++repitition) {
					final SortedMap<Integer, Integer> hashBits = randMap(ids, rand, popcount);								
					// build a hash equivalent sets
					for(final NBHDrec nbhdt: nbhds) {
						nbhdt.sortKey = hashCode(popcount,hashBits,nbhdt.target);
					}
					Arrays.sort(nbhds);
					// stride through blocks of equal sort key
					int leftBound = 0;
					while(leftBound<nvecs) {
						int rightBound = leftBound+1;
						while((rightBound<nvecs)&&(nbhds[leftBound].sortKey==nbhds[rightBound].sortKey)) {
							++rightBound;
						}
						// now leftBound ... rightBound-1 all have same sortKey
						final int nS = rightBound - leftBound;
						if(nS>1) {
							// only completely inspect small neighborhoods
							if(nS<=largeThreshold) {
								inspectedSlots += nS;
								for(int left=leftBound;left<rightBound-1;++left) {
									final NBHDrec nbhdLeft = nbhds[left];
									for(int right=left+1;right<rightBound;++right) {
										final NBHDrec nbhdRight = nbhds[right];
										inspectNBHDPair(rand, nbhdLeft,nbhdRight);
									}
								}								
							} else {
								if(partialInsepctionSize>0) {
									// partial inspect
									for(int left=leftBound;left<rightBound;++left) {
										final NBHDrec nbhdLeft = nbhds[left];
										for(int i=0;i<partialInsepctionSize;++i) {
											final int right = leftBound + rand.nextInt(nS);
											if(left!=right) {
												final NBHDrec nbhdRight = nbhds[right];
												inspectNBHDPair(rand, nbhdLeft,nbhdRight);
											}
										}
									}							
								}
							}
						}
						// next pass
						leftBound = rightBound;
					}						
				}
				// put nbhds back into id order
				for(final NBHDrec nbhdt: nbhds) {
					nbhdt.sortKey = nbhdt.nbhd_id;
				}
				Arrays.sort(nbhds);
				final double inspectedFraction = inspectedSlots/(double)totalSlots;
				System.out.println("\t\tinspected fraction: " + inspectedFraction);
			}
		}
		return nbhds;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		final BuildNbhds bn = new BuildNbhds();
		final Random rand = new Random(2153215);
		final int dim = 100;
		final SparseIIVec[] source = MakeExampleData.buildExample(rand,dim);
		System.out.println("working with " + source.length + " 0/1 vectors of dimension " + dim);
		final Date lhsStart = new Date();
		System.out.println("building LSH solution\t" + lhsStart);
		final NBHDrec[] lshSoln = bn.inspectForNearNeighbors(false, source, rand);
		final Date lhsEnd = new Date();
		final double lhsMS = lhsEnd.getTime() - lhsStart.getTime();
		System.out.println("done LHS\t" + + lhsMS + "MS\t"+ lhsEnd);
		final Date bruteForceStart = new Date();
		System.out.println("building brue force solution\t" + bruteForceStart);
		final NBHDrec[] bruteForceSoln = bn.inspectForNearNeighbors(true, source, rand);
		final Date bruteForceEnd = new Date();
		final double bruteForceMS = bruteForceEnd.getTime() - bruteForceStart.getTime();
		System.out.println("done brute force\t" + bruteForceMS + "MS\t" + bruteForceEnd);
		System.out.println("speedup: " + bruteForceMS/(double)lhsMS);
		final int nvec = source.length;
		//final String sep = "\t";
		//System.out.println("nbhdID" + sep + "bruteForceBestSim" + sep + "LSHBestSim");
		double totalDiff = 0;
		for(int i=0;i<nvec;++i) {
			final SimRec bfN = bruteForceSoln[i].nbhd.last();
			final SimRec lshN = lshSoln[i].nbhd.last();
			// System.out.println("" + i + sep + bfN.sim + sep + lshN.sim);
			final double absDiff = Math.abs(bfN.sim-lshN.sim);
			totalDiff += absDiff;
		}
		final double meanDiff = totalDiff/(double)nvec;
		System.out.println("mean difference in cosine similarity to nearest neighbor: " + meanDiff);
	}

}
