package ddejonge.ggp.asp.mutex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

import ddejonge.ggp.asp.ASPProver;
import ddejonge.ggp.asp.GDL2ASPConverter;
import ddejonge.ggp.prover.mutex.MutexCandidate;
import ddejonge.ggp.prover.mutex.MutexDetector;
import ddejonge.ggp.prover.mutex.MutexDetector_OLD;
import ddejonge.ggp.prover.mutex.MutexProver;
import ddejonge.ggp.tools.Stopwatch;
import ddejonge.ggp.tools.logic.LogicUtils;

public class ASPMutexProver implements MutexProver{

	
	static Stopwatch sw1 = new Stopwatch();
	static Stopwatch sw2 = new Stopwatch();
	
	ASPProver aspProver; 
	
	//CONSTRUCTOR
	public ASPMutexProver(List<Gdl> gameDescription, List<GdlSentence> groundedBasePropositions, List<GdlSentence> groundedDoesPropositions){
		this.aspProver = new ASPProver(gameDescription, groundedBasePropositions, groundedDoesPropositions);
	}
	
	/**
	 * Returns true if something has been proved. i.e. either if it is proved that the candidate is a mutex, or if it is proved that the candidate is a strong mutex.
	 * 
	 * @param allCandidates list of all mutexCandidates, for which we may already know whether they are (strong) mutexes or not. The proof that the given candidateMutex is a mutex may depend on these.
	 * @param candidateMutex the mutex candidate for which we want to determine whether it is a mutex or not.
	 * @param proveStrong if set to true, it will try to prove the candidate is a strong mutex, i.e. that always exactly one of its values must be true. Otherwise it will also allow 0 values to be true.
	 */
	@Override
	public boolean proveMutex(List<MutexCandidate> allCandidates, MutexCandidate candidateMutex, boolean proveStrong){
		
		sw1.start();
		
		//A set of ASP strings to define the domains of the variables in the Mutexes.
		// e.g. if the mutex contains a variable ?Y0 which may take on one of the following values:  X, O, b
		// then this set will contain the strings  "domY0(X)", "domY0(O)", and "domY0(b)"
		Set<String> domainStrings = new HashSet<String>();
		
		//1. Create the ASP rules that are necessary to exclude mutexes already found. Also, for any var that occurs in these rules, create a domain string and add it to this set.
		List<String> knownMutexRules = getKnownMutexRules(allCandidates, candidateMutex); 
		
		
		//2. Get the domains for the variables that appear in the known mutex rules.
		for(MutexCandidate otherMutex : allCandidates){
			
			if(otherMutex == candidateMutex || otherMutex.isMutex == null || !otherMutex.isMutex){
				continue;
			}
			
			GdlSentence mutexSentence = otherMutex.getRepresentant();
			
			//Get the variables that appear in the current mutex sentence.
			/*List<GdlVariable> mutexSentenceInputVars = new ArrayList<GdlVariable>();
			List<GdlVariable> mutexSentenceOutputVars = new ArrayList<GdlVariable>();
			MutexDetector.fillVariableLists(mutexSentence, mutexSentenceInputVars, mutexSentenceOutputVars);
			*/
			
			//Get the domain strings for these variables, so that they can be added to the ASP file.
			fillDomainStrings(mutexSentence, otherMutex.getInputVars(), domainStrings);
			fillDomainStrings(mutexSentence, otherMutex.getOutputVars(), domainStrings);
			
		}
		
		
		//Get the input and output vars of the candidate mutex.
		/*List<GdlVariable> inputVars = new ArrayList<GdlVariable>();
		List<GdlVariable> outputVars = new ArrayList<GdlVariable>();
		MutexDetector.fillVariableLists(candidateMutex.representant, inputVars, outputVars);*/
		
		//3. Create the ASP rules to define the domains of the variables.
		List<GdlVariable> allVars = new ArrayList<GdlVariable>();
		/*allVars.addAll(inputVars);
		allVars.addAll(outputVars);*/
		
		allVars.addAll(candidateMutex.getInputVars());
		allVars.addAll(candidateMutex.getOutputVars());
		fillDomainStrings(candidateMutex.getRepresentant(), allVars, domainStrings);
		
		//3. Create the strings that define the constraints.
		
		//e.g.  String s1 = "hyp(?X0, ?X1) :- 0 { true(cell(?X0, ?X1, ?Y2)) : domY2(?Y2) } 1, domX0(?X0), domX1(?X1).";
		
		String s1 = "hyp(#A) :- 0 { #B : #C } 1, #D.";
		
		if(candidateMutex.getInputVars().isEmpty()){
			s1 = "hyp :- 0 { #B : #C } 1.";
		}
		
		if(proveStrong){
			s1.replace("0", "1");
		}
		
		
		String a = GDL2ASPConverter.getSequenceString(candidateMutex.getInputVars());
		String c = GDL2ASPConverter.getSequenceString(ASPProver.getDomainStrings(candidateMutex.getOutputVars()));
		String d = GDL2ASPConverter.getSequenceString(ASPProver.getDomainStrings(candidateMutex.getInputVars()));
				
		s1 = s1.replace("#A", a);
		s1 = s1.replace("#B", GDL2ASPConverter.toAspString(candidateMutex.getRepresentant()));
		s1 = s1.replace("#C", c);
		s1 = s1.replace("#D", d);
		
		
		
		// #for each ?X0 and ?X1, h(?X0, ?X1) must be true:
		// String s2 = ":- not hyp(?X0, ?X1), domX0(?X0), domX1(?X1).";
		String s2 = ":- not hyp(#A), #D.";
		
		if(candidateMutex.getInputVars().isEmpty()){
			s2 = ":- not hyp.";
		}
		
		s2 = s2.replace("#A", a);
		s2 = s2.replace("#D", d);
		
		
		
		//copy the previous rule, but this time with 'true' replaced by 'next', and 'h0' replaced by 't'.
		// We are looking for a model in which this new rule is false while the previous rule is true.
		String s3 = s1.replace("true", "next");
		s3 = s3.replace("hyp", "t");
		
		// t must be false:
		//":- not t(?X0, ?X1), domX0(?X0), domX1(?X1).";
		String s4 = s2.replace("not hyp", "t");
		
		
		
		ArrayList<String> inductionStep = new ArrayList<String>();
		inductionStep.addAll(knownMutexRules);
		inductionStep.addAll(domainStrings);
		inductionStep.add(s1);
		inductionStep.add(s2);
		inductionStep.add(s3);
		inductionStep.add(s4);
		
		sw1.stop();
		
		System.out.println("ASPMutexProver2.proveMutex() Calling prover to prove " + candidateMutex.getRepresentant() + " " + proveStrong);
		sw2.start();
		Boolean result = aspProver.prove(inductionStep, false);
		sw2.stop();
		
		//if clingo failed, then return false, because indeed nothing has been proved.
		if(result == null){
			return false;
		}
		
		if(result != null && result){
			candidateMutex.isMutex = true;
			if(proveStrong){
				candidateMutex.isStrongMutex = true;
			}
		}
		//NOTE: if the result is negative, this is not yet a proof that the candidate is not a mutex.
		
		return result;
	}
	
	/**
	 * Creates ASP rules that define the domains of the variables that appear in the mutex representations.
	 * @param sentence
	 * @param allVars
	 * @param domainStrings
	 */
	public void fillDomainStrings(GdlSentence sentence, List<GdlVariable> allVars, Set<String> domainStrings){
		
		for(GdlVariable var : allVars){
			
			List<GdlTerm> domainTerms = LogicUtils.getDomain(sentence, var, aspProver.getGroundedBasePropositions()); //e.g. {1,2,3,4,5,6,7,8}
			
			for(GdlTerm domainTerm : domainTerms){
				
				if(domainTerm == null){
					throw new RuntimeException("MutexDetector.fillDomainStrings() Error! sentence: " + sentence + " var: " + var);
				}
				
				domainStrings.add(ASPProver.getDomainString(var, domainTerm) + ".");//e.g. domX0(2)
			}
		}
		
	}
	
	
	/**
	 * Returns a list of ASP rules to ensure that the mutexes that have already been proved are taken into account.
	 * @param allMutexCandidates
	 * @param candidateMutex
	 * @return
	 */
	private static ArrayList<String> getKnownMutexRules(List<MutexCandidate> allMutexCandidates, MutexCandidate candidateMutex){

		ArrayList<String> knownMutexRules = new ArrayList<String>();
		
		if(allMutexCandidates == null){
			return knownMutexRules;
		}
		
		//for each already known mutex, add the restriction that none of them can be violated.
		for(MutexCandidate otherMutex : allMutexCandidates){
			
			//Skip those candidates for which we know that they are not a mutex, or for which we don't know it yet.
			if(otherMutex == candidateMutex || otherMutex.isMutex == null || !otherMutex.isMutex){
				continue;
			}
			
			/*List<GdlVariable> inputVars = new ArrayList<GdlVariable>();
			List<GdlVariable> outputVars = new ArrayList<GdlVariable>();
			MutexDetector.fillVariableLists(otherMutex.representant, inputVars, outputVars);
			*/
			// e.g.  :- 0 { true(cell(?X0, ?X1, ?Y2)) : domY2(?Y2) } 1, domX0(?X0), domX1(?X1).";
			
			String s1;
			if(otherMutex.getInputVars().isEmpty()){
				s1 = ":- { #B : #C } > 1.";
			}else{
				s1 = ":- { #B : #C } > 1, #D."; 
			}
			
			//If we know that the candidate is a strong mutex, then we can make the restriction even stricter.
			if(otherMutex.isStrongMutex != null && otherMutex.isStrongMutex){
				s1.replace(">", "!=");
			}
			
			String c = GDL2ASPConverter.getSequenceString(ASPProver.getDomainStrings(otherMutex.getOutputVars()));
			String d = GDL2ASPConverter.getSequenceString(ASPProver.getDomainStrings(otherMutex.getInputVars()));
			
			s1 = s1.replace("#B", GDL2ASPConverter.toAspString(otherMutex.getRepresentant()));
			s1 = s1.replace("#C", c);
			s1 = s1.replace("#D", d);
			
			knownMutexRules.add(s1);
		}
		
		return knownMutexRules;
	}


	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
