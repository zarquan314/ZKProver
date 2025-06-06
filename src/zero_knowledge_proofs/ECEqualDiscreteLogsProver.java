package zero_knowledge_proofs;

import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import curve_wrapper.ECCurveWrapper;
import curve_wrapper.ECPointWrapper;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;

public class ECEqualDiscreteLogsProver extends ZKPProtocol {

	//pub = [y_g, y_h]
	//sec = [rp, x]
	//env = [g, h]
	@Override
	public CryptoData initialComm(CryptoData input, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		ECPointWrapper[] data = new ECPointWrapper[2];
		CryptoData[] e = environment.getCryptoDataArray();
		CryptoData[] i = input.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		BigInteger r = i[2].getBigInt();
		data[0] = g.multiply(r);
		data[1] = h.multiply(r);
		

		CryptoData toReturn = new CryptoDataArray(data);
		return toReturn;
	}

	//input = [y_g, y_h, z]
	@Override
	public CryptoData initialCommSim(CryptoData input, BigInteger challenge, CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException {
		ECPointWrapper[] data = new ECPointWrapper[2];
		CryptoData[] i = input.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();		//(y, z) 
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		ECPointWrapper y_g = i[0].getECPointData(c);
		ECPointWrapper y_h = i[1].getECPointData(c);
		BigInteger z = i[2].getBigInt();
		//a = g^z * y^(-c)
		//System.out.printf("c = %s\ninputs = %s\n", challenge.toString(16), input);
		data[0] = g.multiply(z).add(y_g.multiply(challenge.negate()));
		data[1] = h.multiply(z).add(y_h.multiply(challenge.negate()));
		
		CryptoData toReturn = new CryptoDataArray(data);
		return toReturn;
	}

	@Override
	public CryptoData calcResponse(CryptoData input, BigInteger challenge, CryptoData environment)
			throws NoTrueProofException, MultipleTrueProofException {
		BigInteger[] array = new BigInteger[1];
		CryptoData[] i = input.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();

		BigInteger x = i[3].getBigInt();
		BigInteger r = i[2].getBigInt();
		array[0] = (r.add(x.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		//System.out.printf("P:\t%s ?= %s\n", ((i[1].modPow(challenge, e[1]).multiply(e[0].modPow(i[2], e[1]))).mod(e[1])), e[0].modPow(array[0], e[1]));
		
		//System.out.printf("P:\tg = %s\nP:\th = %s\nP:\tp = %s\nP:\tr = %s\nP:\tx = %s\nP:\ty = %s\nP:\tz = %s\nP:\tc = %s\n",e[0],e[1],e[1],i[2],i[0],i[1], array[0], challenge);
		CryptoData toReturn = new CryptoDataArray(array);
		return toReturn;
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData input) {
		CryptoData[] in = input.getCryptoDataArray();
		BigInteger[] out = new BigInteger[1];
		out[0] = in[2].getBigInt();
		return new CryptoDataArray(out);
	}

	@Override
	public boolean verifyResponse(CryptoData input, CryptoData a, CryptoData z, BigInteger challenge,
			CryptoData environment) {
		CryptoData[] e = environment.getCryptoDataArray();
		CryptoData[] resp = z.getCryptoDataArray();
		CryptoData[] i = input.getCryptoDataArray();
		CryptoData[] a_pack = a.getCryptoDataArray();
		
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h;
		try{
			h = e[1].getECPointData(c);
		} catch (Exception ex) {
			System.out.println("qewytdijcvnzxmvhfd");
			System.out.println(environment);
			ex.printStackTrace();
			return false;
		}
		
		ECPointWrapper y_g = i[0].getECPointData(c);
		ECPointWrapper y_h = i[1].getECPointData(c);
		BigInteger zNumber = resp[0].getBigInt();
		ECPointWrapper a_1 = a_pack[0].getECPointData(c);
		ECPointWrapper a_2 = a_pack[1].getECPointData(c);
		boolean verify = (((y_g.multiply(challenge).add(a_1))).equals(g.multiply(zNumber)) && ((y_h.multiply(challenge).add(a_2))).equals(h.multiply(zNumber)));
		if(!verify) {
			System.out.printf("Error: %s != %s OR %s != %s\n", ((y_g.multiply(challenge).add(a_1))).normalize(), g.multiply(zNumber).normalize(), ((y_h.multiply(challenge).add(a_2))).normalize(), h.multiply(zNumber).normalize());
			System.out.flush();
		}
		return verify;

	}

	@Override
	public CryptoData initialComm(CryptoData publicInput, CryptoData secrets, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		ECPointWrapper[] data = new ECPointWrapper[2];
		CryptoData[] e = environment.getCryptoDataArray();
//		CryptoData[] pI = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		BigInteger r = s[0].getBigInt();
		data[0] = g.multiply(r);
		data[1] = h.multiply(r);
		

		CryptoData toReturn = new CryptoDataArray(data);
		return toReturn;
	}

	@Override
	public CryptoData initialCommSim(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException, NoTrueProofException {
		ECPointWrapper[] data = new ECPointWrapper[2];
		CryptoData[] i = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();		//(y, z) 
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		ECPointWrapper y_g = i[0].getECPointData(c);
		ECPointWrapper y_h = i[1].getECPointData(c);
		BigInteger z = s[0].getBigInt();
		//a = g^z * y^(-c)
		//System.out.printf("c = %s\ninputs = %s\n", challenge.toString(16), input);
		data[0] = g.multiply(z).add(y_g.multiply(challenge.negate()));
		data[1] = h.multiply(z).add(y_h.multiply(challenge.negate()));
		
		CryptoData toReturn = new CryptoDataArray(data);
		return toReturn;
	}

	@Override
	public CryptoData calcResponse(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment) throws NoTrueProofException, MultipleTrueProofException {
		BigInteger[] array = new BigInteger[1];
//		CryptoData[] i = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();
		BigInteger x = s[1].getBigInt();
		BigInteger r = s[0].getBigInt();
		array[0] = (r.add(x.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		//System.out.printf("P:\t%s ?= %s\n", ((i[1].modPow(challenge, e[1]).multiply(e[0].modPow(i[2], e[1]))).mod(e[1])), e[0].modPow(array[0], e[1]));
		
		//System.out.printf("P:\tg = %s\nP:\th = %s\nP:\tp = %s\nP:\tr = %s\nP:\tx = %s\nP:\ty = %s\nP:\tz = %s\nP:\tc = %s\n",e[0],e[1],e[1],i[2],i[0],i[1], array[0], challenge);
		CryptoData toReturn = new CryptoDataArray(array);
		return toReturn;
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData publicInput, CryptoData secrets) {
		CryptoData[] in = secrets.getCryptoDataArray();
		BigInteger[] out = new BigInteger[1];
		out[0] = in[0].getBigInt();
		return new CryptoDataArray(out);
	}
}
