package zero_knowledge_proofs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import zero_knowledge_proofs.CryptoData.BigIntData;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;


public class ZeroKnowledgeOrProver extends ZKPProtocol{
	private ZKPProtocol[] p;
	private BigInteger challengeMod;
	//private BigInteger[] simulatedChallenges;  //TODO:  Do I want to allow a person to do the proper Zero Knowledge Proof on more than one statement in a ZKP-OR
	//I currently think not, I will require that the simulatedChallenges array have only one null value.
	//	private int trueProof; 	//Which proof is being proven



	public ZeroKnowledgeOrProver(ZKPProtocol[] p, BigInteger challengeModulus) {
		this.p = p.clone();
		this.challengeMod = challengeModulus;
	}



	@Override
	protected ArrayList<BigInteger> internalNullChallenges(CryptoData response, BigInteger challenge, ArrayList<BigInteger> list) {
		if(response == null) {
			list.add(challenge);
			return list;
		}
		if(!response.hasNull()) return list;
		CryptoData[] z = response.getCryptoDataArray();
		CryptoData[] c = z[z.length-1].getCryptoDataArray();
		for(int i = 0; i < z.length-1; i++)
		{
			if(z[i] == null) {
				list.add(c[i].getBigInt());
			}
			else if(z[i].hasNull()){
				p[i].internalNullChallenges(z[i], c[i].getBigInt(), list);
			}
		}
		return list;

		//			CryptoData[] input = proverInput.getCryptoDataArray();
		//			int hasNullIndex = proverInput.getFirstNullIndex();
		//			if(input[hasNullIndex] == null)
		//			{
		//				return challenge;
		//			}
		//			else return p[hasNullIndex].firstNullChallenge(input[hasNullIndex], challenge);
	}
	//	@Override
	//	public BigInteger firstNullChallenge(CryptoData proverInput, BigInteger challenge) {
	//		CryptoData[] input = proverInput.getCryptoDataArray();
	//		int hasNullIndex = proverInput.getFirstNullIndex();
	//		CryptoData[] simChallenges = input[input.length-1].getCryptoDataArray();
	//		BigInteger endChallenge = simChallenges[hasNullIndex].getBigInt();
	//		if(input[hasNullIndex] == null)
	//		{
	//			return endChallenge;
	//		}
	//		else return p[hasNullIndex].firstNullChallenge(input[hasNullIndex], endChallenge);
	//		
	//	}

	//input format:  [[(inputs for p1)],[(inputs for p2)],...[(inputs for pn)],[simulatedChallenges]
	//
	@Override
	public CryptoData initialComm(CryptoData input, CryptoData packedEnvironment) throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		if(input == null) return null;

		CryptoData[] environment = packedEnvironment.getCryptoDataArray();
		CryptoData[] i = input.getCryptoDataArray(); 
		CryptoData[] simulatedChallenges = i[i.length - 1].getCryptoDataArray();
		//System.out.println("In OR: " + i[i.length - 1]);
		CryptoData[] o = new CryptoData[p.length];
		if (simulatedChallenges.length != p.length) 
		{
			System.out.println(i[i.length - 1]);
			throw new ArraySizesDoNotMatchException("" + p.length + " != " + simulatedChallenges.length);
		}
		boolean trueProofFound = false;

		for(int j = 0; j < o.length; j++)
		{

			BigInteger c = simulatedChallenges[j].getBigInt();
			if(c != null) 
			{
				if(i[j] == null)
					o[j] = null;
				else
					o[j] = p[j].initialCommSim(i[j], c, environment[j]);
			}
			else
			{
				if(!trueProofFound)
				{
					trueProofFound = true;
				}
				else throw new MultipleTrueProofException();

				if(i[j] == null)
					o[j] = null;
				else
					o[j] = p[j].initialComm(i[j], environment[j]);

			}
		}
		if(!trueProofFound) 
		{
			throw new NoTrueProofException();
		}
		return new CryptoDataArray(o);
	}

	//input format:  [[(inputs for p1)],[(inputs for p2)],...,[inputs for pn],[simulatedChallenges]]  
	//Exactly, one challenge should be 0.  If no challenges are 0, it will replace the last challenge to accommodate the challenge argument.
	//If there is more than 1 missing challenge, it will throw a MultipleTrueProofException.
	//If the simulated challenges array is smaller than the number of protocols, then it will throw an ArraySizesDoNotMatchException.
	@Override
	public CryptoData initialCommSim(CryptoData input, BigInteger challenge, CryptoData packedEnvironment) throws MultipleTrueProofException, ArraySizesDoNotMatchException, NoTrueProofException {
		if(input == null) return null;
		CryptoData[] environment = packedEnvironment.getCryptoDataArray();
		CryptoData[] in = input.getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length];
		CryptoData[] simulatedChallenges = in[p.length].getCryptoDataArray();
		try {
			if(simulatedChallenges.length != p.length) throw new ArraySizesDoNotMatchException("" + p.length + " != " + simulatedChallenges.length);
		}catch(NullPointerException e)
		{
			System.out.println(input);
			throw new NullPointerException(e.getMessage());
		}
		boolean trueProofFound = false;
		BigInteger trueChallenge = challenge;
		int trueProof2 = -1;

		for(int j = 0; j < p.length-1; j++)
		{
			BigInteger c = simulatedChallenges[j].getBigInt();
			if(c == null)
			{
				if(trueProofFound)
				{
					throw new MultipleTrueProofException();
				} else {
					trueProof2 = j;
					trueProofFound = true;
				}
			}
			else
			{
				if(in[j] == null) 
					out[j] = null;
				else
					out[j] = p[j].initialCommSim(in[j], c, environment[j]);
				trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			}
		}
		if(trueProofFound)
		{
			BigInteger c = simulatedChallenges[p.length-1].getBigInt();
			if(in[p.length-1] == null)
				out[p.length-1] = null;
			else
				out[p.length - 1] = p[p.length - 1].initialCommSim(in[p.length-1], c, environment[p.length-1]);
			trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			simulatedChallenges[trueProof2] = new BigIntData(trueChallenge);
			if(in[trueProof2] == null)
				out[trueProof2] = null;
			else
				out[trueProof2] = p[trueProof2].initialCommSim(in[trueProof2], trueChallenge, environment[trueProof2]);
		}
		else {
			throw new NoTrueProofException();
		}
		return new CryptoDataArray(out);
	}

	@Override
	public CryptoData calcResponse(CryptoData input, BigInteger challenge, CryptoData packedEnvironment) throws NoTrueProofException, MultipleTrueProofException {
		if(input == null) return null;
		CryptoData[] in = input.getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length + 1];
		CryptoData[] environment = packedEnvironment.getCryptoDataArray();
		CryptoData[] simulatedChallenges = in[in.length - 1].getCryptoDataArray().clone();
		BigInteger trueChallenge = challenge;
		int trueProof = -1;

		for(int i = 0; i < p.length; i++)
		{
			BigInteger c = simulatedChallenges[i].getBigInt();
			if(c == null)
			{
				if(trueProof != -1) throw new MultipleTrueProofException();
				trueProof = i;
			}
			else
			{
				if(in[i] == null)
					out[i] = null;
				else
					out[i] = p[i].simulatorGetResponse(in[i]);

				trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			}
		}
		if(trueProof == -1) throw new NoTrueProofException();
		simulatedChallenges[trueProof] = new BigIntData(trueChallenge);
		if(in[trueProof] == null) 
			out[trueProof] = null;
		else
			out[trueProof] = p[trueProof].calcResponse(in[trueProof], trueChallenge, environment[trueProof]);

		out[p.length] = new CryptoDataArray(simulatedChallenges);

		CryptoData toReturn = new CryptoDataArray(out);

		//System.out.printf("P:\tinput = %s\nP:\tz = %s\nP:\tc = %s\n", input, toReturn, challenge);

		return toReturn;
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData input) {
		if(input == null) return null;
		CryptoData[] in = input.getCryptoDataArray();
		CryptoData[] simulatedChallenges = in[in.length-1].getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length + 1];
		for(int i = 0; i < in.length-1; i++)
		{
			if(in[i] == null) 
				out[i] = null;
			else
				out[i] = p[i].simulatorGetResponse(in[i]);
		}
		out[p.length] = new CryptoDataArray(simulatedChallenges);
		CryptoData toReturn = new CryptoDataArray(out);

		return toReturn;
	}
	@Override
	public boolean verifyResponse(CryptoData input, CryptoData a_unopened, CryptoData z_unopened, BigInteger challenge, CryptoData environments) {
		CryptoData[] in = input.getCryptoDataArray();
		CryptoData[] a = a_unopened.getCryptoDataArray();
		CryptoData[] z = z_unopened.getCryptoDataArray();
		CryptoData[] e = environments.getCryptoDataArray();
		CryptoData[] challenges = z[p.length].getCryptoDataArray();
		BigInteger summedChallenge = challenge;
		//		System.out.println("V:\tin = " + input);
		//		System.out.println("V:\ta  = " + a_unopened);
		//		System.out.println("V:\tz  = " + z_unopened);
		//		System.out.println("V:\tc  = " + challenge);
		boolean toReturn = true;
		boolean flag;
		for(int i = 0; i < p.length; i++)
		{ 
			BigInteger c = challenges[i].getBigInt();
			summedChallenge = summedChallenge.subtract(c).mod(challengeMod);
			flag = p[i].verifyResponse(in[i], a[i], z[i], c, e[i]);
			if(!flag) 
			{
				System.out.println(Thread.currentThread() + " OR failed on proof " + i);
				toReturn = false;
			}
//			/**/else System.out.println("OR good on " + i);
		}
		if(!summedChallenge.equals(BigInteger.ZERO))
		{	
			System.out.println("Bad Challenge in OR");
			System.out.println(challengeMod);
			System.out.println(Arrays.toString(challenges));
			

			System.out.println(challenge);
			toReturn =  false;
		}

		return toReturn;
	}
	@Override
	public String toString()
	{
		String toReturn = "OR(";
		for(int i = 0; i < p.length; i++)
		{
			if(i != 0) toReturn += ", ";
			toReturn += p[i].toString();
		}
		return toReturn + ")";
	}



	@Override
	public CryptoData initialComm(CryptoData publicInput, CryptoData secrets, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		if(publicInput == null || secrets == null) return null;

		CryptoData[] e = environment.getCryptoDataArray();
		CryptoData[] i = publicInput.getCryptoDataArray(); 
		CryptoData[] s = secrets.getCryptoDataArray(); 
		CryptoData[] simulatedChallenges = s[s.length - 1].getCryptoDataArray();
		//System.out.println("In OR: " + i[i.length - 1]);
		CryptoData[] o = new CryptoData[p.length];
		if (simulatedChallenges.length != p.length) 
		{
			System.out.println(i[i.length - 1]);
			throw new ArraySizesDoNotMatchException("" + p.length + " != " + simulatedChallenges.length);
		}
		if (s.length - 1 != p.length) 
		{
			System.out.println(i[i.length - 1]);
			throw new ArraySizesDoNotMatchException("" + s.length + " - 1 != " + p.length);
		}
		boolean trueProofFound = false;

		for(int j = 0; j < o.length; j++)
		{
			BigInteger c = simulatedChallenges[j].getBigInt();
			if(c != null) 
			{
				if(i[j] == null)
					o[j] = null;
				else
					o[j] = p[j].initialCommSim(i[j], s[j], c, e[j]);
			}
			else
			{
				if(!trueProofFound)
				{
					trueProofFound = true;
				}
				else throw new MultipleTrueProofException();

				if(i[j] == null)
					o[j] = null;
				else
					o[j] = p[j].initialComm(i[j], s[j], e[j]);

			}
		}
		if(!trueProofFound) 
		{
			throw new NoTrueProofException();
		}
		return new CryptoDataArray(o);
	}



	@Override
	public CryptoData initialCommSim(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment)
					throws MultipleTrueProofException, ArraySizesDoNotMatchException, NoTrueProofException {
		if(publicInput == null || secrets == null) return null;
		CryptoData[] e = environment.getCryptoDataArray();
		CryptoData[] in = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length];
		CryptoData[] simulatedChallenges = in[p.length].getCryptoDataArray();
		if(simulatedChallenges.length != p.length) throw new ArraySizesDoNotMatchException("" + p.length + " != " + simulatedChallenges.length);
		boolean trueProofFound = false;
		BigInteger trueChallenge = challenge;
		int trueProof = -1;

		for(int j = 0; j < p.length-1; j++)
		{
			BigInteger c = simulatedChallenges[j].getBigInt();
			if(c == null)
			{
				if(trueProofFound)
				{
					throw new MultipleTrueProofException();
				} else {
					trueProof = j;
					trueProofFound = true;
				}
			}
			else
			{
				if(in[j] == null) 
					out[j] = null;
				else
					out[j] = p[j].initialCommSim(in[j], s[j], c, e[j]);
				trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			}
		}
		if(trueProofFound)
		{
			BigInteger c = simulatedChallenges[p.length-1].getBigInt();
			if(in[p.length-1] == null)
				out[p.length-1] = null;
			else
				out[p.length - 1] = p[p.length - 1].initialCommSim(in[p.length-1], s[p.length-1], c, e[p.length-1]);
			trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			simulatedChallenges[trueProof] = new BigIntData(trueChallenge);
			if(in[trueProof] == null)
				out[trueProof] = null;
			else
				out[trueProof] = p[trueProof].initialCommSim(in[trueProof], s[trueProof], trueChallenge, e[trueProof]);
		}
		else {
			throw new NoTrueProofException();
		}
		return new CryptoDataArray(out);
	}



	@Override
	public CryptoData calcResponse(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment) throws NoTrueProofException, MultipleTrueProofException {
		if(publicInput == null || secrets == null) return null;
		CryptoData[] in = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length + 1];
		CryptoData[] e = environment.getCryptoDataArray();
		CryptoData[] simulatedChallenges = s[s.length - 1].getCryptoDataArray().clone();
		BigInteger trueChallenge = challenge;
		int trueProof = -1;

		for(int i = 0; i < p.length; i++)
		{
			BigInteger c = simulatedChallenges[i].getBigInt();
			if(c == null)
			{
				if(trueProof != -1) throw new MultipleTrueProofException();
				trueProof = i;
			}
			else
			{
				if(in[i] == null)
					out[i] = null;
				else
					out[i] = p[i].simulatorGetResponse(in[i], s[i]);

				trueChallenge = trueChallenge.subtract(c).mod(challengeMod);
			}
		}
		if(trueProof == -1) throw new NoTrueProofException();
		simulatedChallenges[trueProof] = new BigIntData(trueChallenge);
		if(in[trueProof] == null) 
			out[trueProof] = null;
		else
			out[trueProof] = p[trueProof].calcResponse(in[trueProof], s[trueProof], trueChallenge, e[trueProof]);

		out[p.length] = new CryptoDataArray(simulatedChallenges);

		CryptoData toReturn = new CryptoDataArray(out);

		//System.out.printf("P:\tinput = %s\nP:\tz = %s\nP:\tc = %s\n", input, toReturn, challenge);

		return toReturn;
	}



	@Override
	public CryptoData simulatorGetResponse(CryptoData publicInput, CryptoData secrets) {
		if(secrets == null) return null;
		CryptoData[] sIn = secrets.getCryptoDataArray();
		CryptoData[] pIn = publicInput.getCryptoDataArray();
		CryptoData[] simulatedChallenges = sIn[sIn.length-1].getCryptoDataArray();
		CryptoData[] out = new CryptoData[p.length + 1];
		for(int i = 0; i < pIn.length; i++)
		{
			if(sIn[i] == null) 
				out[i] = null;
			else
				out[i] = p[i].simulatorGetResponse(pIn[i], sIn[i]);
		}
		out[p.length] = new CryptoDataArray(simulatedChallenges);
		CryptoData toReturn = new CryptoDataArray(out);

		return toReturn;
	}
}
