package ddejonge.ggp.prover.mutex;

import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlSentence;

public interface MutexProver {

	/**
	 * Returns true if something has been proved. i.e. either if it is proved that the candidate is a mutex, or if it is proved that the candidate is a strong mutex.
	 * 
	 * @param allCandidates list of all mutexCandidates, for which we may already know whether they are (strong) mutexes or not. The proof that the given candidateMutex is a mutex may depend on these.
	 * @param candidateMutex the mutex candidate for which we want to determine whether it is a mutex or not.
	 * @param proveStrong if set to true, it will try to prove the candidate is a strong mutex, i.e. that always exactly one of its values must be true. Otherwise it will also allow 0 values to be true.
	 */
	public boolean proveMutex(List<MutexCandidate> allCandidates, MutexCandidate candidateMutex, boolean proveStrong);
	
}
