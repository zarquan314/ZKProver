package zero_knowledge_proofs;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import curve_wrapper.ECCurveWrapper;
import curve_wrapper.ECPointWrapper;
import zero_knowledge_proofs.CryptoData.BigIntData;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;
import zero_knowledge_proofs.CryptoData.ECPointData;

public class ECPOKPedersenProver extends ZKPProtocol{

	//environment format:	[(c, g), h]
	//input format:  [y, r_1, r_2, m, r]
	//g^m*h^r
	@Override
	public CryptoData initialComm(CryptoData input, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		ECPointWrapper g = ue[0].getECPointData(c);
		ECPointWrapper h = ue[1].getECPointData(c);
		
		CryptoData[] ui = input.getCryptoDataArray();
		BigInteger r_1 = ui[1].getBigInt();
		BigInteger r_2 = ui[2].getBigInt();
		
		CryptoData a = new CryptoDataArray(new CryptoData[] {new ECPointData(g.multiply(r_1).add(h.multiply(r_2)))});
		
		return a;
	}

	//input format:  [y, z_1, z_2]
	@Override
	public CryptoData initialCommSim(CryptoData input, BigInteger challenge, CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException {

		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		ECPointWrapper g = ue[0].getECPointData(c);
		ECPointWrapper h = ue[1].getECPointData(c);
		
		CryptoData[] ui = input.getCryptoDataArray();
		ECPointWrapper y = ui[0].getECPointData(c);
		BigInteger z_1 = ui[1].getBigInt();
		BigInteger z_2 = ui[2].getBigInt();
		
		return new CryptoDataArray(new CryptoData[] {new ECPointData(g.multiply(z_1).add(h.multiply(z_2).add(y.multiply(challenge.negate()))))});
	}

	@Override
	public CryptoData calcResponse(CryptoData input, BigInteger challenge, CryptoData environment)
			throws NoTrueProofException, MultipleTrueProofException {
		
		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		
		CryptoData[] ui = input.getCryptoDataArray();
		BigInteger r_1 = ui[1].getBigInt();
		BigInteger r_2 = ui[2].getBigInt();
		BigInteger m = ui[3].getBigInt();
		BigInteger r = ui[4].getBigInt();
		
		

		return new CryptoDataArray(new CryptoData[] {new BigIntData(r_1.add(m.multiply(challenge)).mod(c.getOrder())),new BigIntData(r_2.add(r.multiply(challenge)).mod(c.getOrder()))});
	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData input) {

		CryptoData[] ui = input.getCryptoDataArray();
		
		return new CryptoDataArray(new CryptoData[] {ui[1], ui[2]});
	}
	
	@Override
	public boolean verifyResponse(CryptoData input, CryptoData a, CryptoData z, BigInteger challenge, CryptoData environment) {
		// TODO Auto-generated method stub

		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		ECPointWrapper g = ue[0].getECPointData(c);
		ECPointWrapper h = ue[1].getECPointData(c);
		
		CryptoData[] ui = input.getCryptoDataArray();
		ECPointWrapper y = ui[0].getECPointData(c);
		
		CryptoData[] ua = a.getCryptoDataArray();
		ECPointWrapper init = ua[0].getECPointData(c);

		CryptoData[] uz = z.getCryptoDataArray();
		BigInteger z_1 = uz[0].getBigInt();
		BigInteger z_2 = uz[1].getBigInt();
		//check:  g^z_1*h^z_2 == y^c*a
		return (g.multiply(z_1).add(h.multiply(z_2)).equals(y.multiply(challenge).add(init)));
	}

	@Override
	public CryptoData initialComm(CryptoData publicInput, CryptoData secrets, CryptoData environment)
			throws MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException {
		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		ECPointWrapper g = ue[0].getECPointData(c);
		ECPointWrapper h = ue[1].getECPointData(c);
		
		CryptoData[] ui = secrets.getCryptoDataArray();
		BigInteger r_1 = ui[0].getBigInt();
		BigInteger r_2 = ui[1].getBigInt();
		
		CryptoData a = new CryptoDataArray(new CryptoData[] {new ECPointData(g.multiply(r_1).add(h.multiply(r_2)))});
		
		return a;
	}

	@Override
	public CryptoData initialCommSim(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment)
			throws MultipleTrueProofException, ArraySizesDoNotMatchException, NoTrueProofException {
		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		ECPointWrapper g = ue[0].getECPointData(c);
		ECPointWrapper h = ue[1].getECPointData(c);
		
		CryptoData[] s = secrets.getCryptoDataArray();
		CryptoData[] ui = publicInput.getCryptoDataArray();
		ECPointWrapper y = ui[0].getECPointData(c);
		BigInteger z_1 = s[0].getBigInt();
		BigInteger z_2 = s[1].getBigInt();
		
		return new CryptoDataArray(new CryptoData[] {new ECPointData(g.multiply(z_1).add(h.multiply(z_2).add(y.multiply(challenge.negate()))))});

	}

	@Override
	public CryptoData calcResponse(CryptoData publicInput, CryptoData secrets, BigInteger challenge,
			CryptoData environment) throws NoTrueProofException, MultipleTrueProofException {
		
		CryptoData[] ue = environment.getCryptoDataArray();
		ECCurveWrapper c = ue[0].getECCurveData();
		
		CryptoData[] s = secrets.getCryptoDataArray();
		
		BigInteger r_1 = s[0].getBigInt();
		BigInteger r_2 = s[1].getBigInt();
		BigInteger m = s[3].getBigInt();
		BigInteger r = s[4].getBigInt();
		
		

		return new CryptoDataArray(new CryptoData[] {new BigIntData(r_1.add(m.multiply(challenge)).mod(c.getOrder())),new BigIntData(r_2.add(r.multiply(challenge)).mod(c.getOrder()))});

	}

	@Override
	public CryptoData simulatorGetResponse(CryptoData publicInput, CryptoData secrets) {
		CryptoData[] ui = secrets.getCryptoDataArray();
		
		return new CryptoDataArray(new CryptoData[] {ui[0], ui[1]});
	}

}
