package zero_knowledge_proofs;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import curve_wrapper.ECCurveWrapper;
import curve_wrapper.ECPointWrapper;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;

public class ECProofOfPrechosenExponentProver extends ZKPProtocol {

	//Input = [orig Base (m), new message (m^x), commitment (g^x*h^t), r_1, r_2, x, t]
	@Override
	public CryptoData initialComm(CryptoData input, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		
		CryptoData[] i = input.getCryptoDataArray();
		ECPointWrapper m = i[0].getECPointData(c);
		BigInteger r1 = i[3].getBigInt();
		BigInteger r2 = i[4].getBigInt();
		
		ECPointWrapper[] init = new ECPointWrapper[2];
		init[0] = m.multiply(r1);
		init[1] = g.multiply(r1).add(h.multiply(r2));
		return new CryptoDataArray(init);
	}
	
	//Input = [orig Base (m), new message (m^x), commitment (g^x*h^t), z_1, z_2]
	@Override
	public CryptoData initialCommSim(CryptoData input, BigInteger challenge, CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		
		CryptoData[] i = input.getCryptoDataArray();
		ECPointWrapper m = i[0].getECPointData(c);
		ECPointWrapper newM = i[1].getECPointData(c);
		ECPointWrapper comm = i[2].getECPointData(c);
		BigInteger z1 = i[3].getBigInt();
		BigInteger z2 = i[4].getBigInt();		
		
		ECPointWrapper[] init = new ECPointWrapper[2];
		init[0] = m.multiply(z1).add(newM.multiply(challenge.negate()));
		init[1] = g.multiply(z1).add(h.multiply(z2)).add(comm.multiply(challenge.negate()));

		return new CryptoDataArray(init);
	}

	@Override
	public CryptoData calcResponse(CryptoData input, BigInteger challenge, CryptoData environment)
			throws NoTrueProofException, MultipleTrueProofException {
		BigInteger[] array = new BigInteger[2];
		CryptoData[] i = input.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();

		BigInteger x = i[5].getBigInt();
		BigInteger t = i[6].getBigInt();
		BigInteger r1 = i[3].getBigInt();
		BigInteger r2 = i[4].getBigInt();
		array[0] = (r1.add(x.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		array[1] = (r2.add(t.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		
		
		return new CryptoDataArray(array);
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData input) {
		CryptoData[] i = input.getCryptoDataArray();
		return new CryptoDataArray(new CryptoData[] {i[3], i[4]});
	}

	//input: [origBase (m), newMessage(m^x), commitment (g^x*h^t)]
	@Override
	public boolean verifyResponse(CryptoData input, CryptoData a, CryptoData z, BigInteger challenge,
			CryptoData environment) {

		CryptoData[] e = environment.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		
		CryptoData[] i = input.getCryptoDataArray();
		ECPointWrapper m = i[0].getECPointData(c);
		ECPointWrapper newM = i[1].getECPointData(c);
		ECPointWrapper comm = i[2].getECPointData(c);
		
		CryptoData[] init = a.getCryptoDataArray();
		ECPointWrapper a1 = init[0].getECPointData(c);
		ECPointWrapper a2 = init[1].getECPointData(c);
		
		CryptoData[] resp = z.getCryptoDataArray();
		BigInteger z1 = resp[0].getBigInt();
		BigInteger z2 = resp[1].getBigInt();
		
		
		return (((newM.multiply(challenge).add(a1)).equals(m.multiply(z1))) && ((comm.multiply(challenge).add(a2)).equals(g.multiply(z1).add(h.multiply(z2)))));
	}

	@Override
	public CryptoData initialComm(CryptoData publicInput, CryptoData secrets, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		
		CryptoData[] i = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		ECPointWrapper m = i[0].getECPointData(c);
		BigInteger r1 = s[0].getBigInt();
		BigInteger r2 = s[1].getBigInt();
		
		ECPointWrapper[] init = new ECPointWrapper[2];
		init[0] = m.multiply(r1);
		init[1] = g.multiply(r1).add(h.multiply(r2));
		return new CryptoDataArray(init);
	}

	@Override
	public CryptoData initialCommSim(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException, NoTrueProofException {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurveWrapper c = e[0].getECCurveData();
		ECPointWrapper g = e[0].getECPointData(c);
		ECPointWrapper h = e[1].getECPointData(c);
		
		CryptoData[] i = publicInput.getCryptoDataArray();
		CryptoData[] s = secrets.getCryptoDataArray();
		ECPointWrapper m = i[0].getECPointData(c);
		ECPointWrapper newM = i[1].getECPointData(c);
		ECPointWrapper comm = i[2].getECPointData(c);
		BigInteger z1 = s[0].getBigInt();
		BigInteger z2 = s[1].getBigInt();		
		
		ECPointWrapper[] init = new ECPointWrapper[2];
		init[0] = m.multiply(z1).add(newM.multiply(challenge.negate()));
		init[1] = g.multiply(z1).add(h.multiply(z2)).add(comm.multiply(challenge.negate()));

		return new CryptoDataArray(init);
	}

	@Override
	public CryptoData calcResponse(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment) throws NoTrueProofException, MultipleTrueProofException {
		BigInteger[] array = new BigInteger[2];
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] e = environment.getCryptoDataArray();

		BigInteger x = s[2].getBigInt();
		BigInteger t = s[3].getBigInt();
		BigInteger r1 = s[0].getBigInt();
		BigInteger r2 = s[1].getBigInt();
		array[0] = (r1.add(x.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		array[1] = (r2.add(t.multiply(challenge))).mod(e[0].getECCurveData().getOrder());
		
		
		return new CryptoDataArray(array);
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData publicInput, CryptoData secrets) {
		CryptoData[] i = publicInput.getCryptoDataArray();
		return new CryptoDataArray(new CryptoData[] {i[0], i[1]});
	}
}
