package zero_knowledge_proofs;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Base64;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import curve_wrapper.ECCurveWrapper;
import curve_wrapper.ECPointWrapper;
import zero_knowledge_proofs.CryptoData.CryptoData;

public class ECPedersenCommitment2 implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4464353885259184169L;
	protected byte[] data;
	
	
	public ECPedersenCommitment2(BigInteger message, BigInteger keys, CryptoData environment)
	{
		//g^m h^r 
		ECCurveWrapper c = environment.getCryptoDataArray()[0].getECCurveData();
		ECPointWrapper g = environment.getCryptoDataArray()[0].getECPointData(c);
		ECPointWrapper h = environment.getCryptoDataArray()[1].getECPointData(c);
		ECPointWrapper comm = g.multiply(message).add(h.multiply(keys));
		data = comm.getEncoded(true);
	}
	private ECPedersenCommitment2(ECPointWrapper comm)
	{
		data = comm.getEncoded(true);
	}

	public ECPointWrapper getCommitment(CryptoData environment) {
		ECCurveWrapper c = environment.getCryptoDataArray()[0].getECCurveData();
		return c.decodePoint(data);
	}

	public boolean verifyCommitment(BigInteger message, BigInteger keys, CryptoData environment) {
		ECCurveWrapper c = environment.getCryptoDataArray()[0].getECCurveData();
		ECPointWrapper g = environment.getCryptoDataArray()[0].getECPointData(c);
		ECPointWrapper h = environment.getCryptoDataArray()[1].getECPointData(c);
		ECPointWrapper comm = g.multiply(message).add(h.multiply(keys));
		return getCommitment(environment).equals(comm);
	}

	public ECPedersenCommitment2 multiplyCommitment(ECPedersenCommitment2 cmt, CryptoData environment) {
		
		return new ECPedersenCommitment2(cmt.getCommitment(environment).add(getCommitment(environment)));
	}
	public ECPedersenCommitment2 multiplyShiftedCommitment(ECPedersenCommitment2 cmt, int lShift, CryptoData environment) {
		
		return new ECPedersenCommitment2((cmt.getCommitment(environment).multiply(BigInteger.ONE.shiftLeft(lShift))).add(getCommitment(environment)));
	}
	
	public String toString64()
	{
		return String.format("(%s)", Base64.getEncoder().encodeToString(data));
	}

	public String toString()
	{
		return String.format("(%s)", new BigInteger(data));
	}
	
}
