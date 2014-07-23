package com.m8.kalman;

import java.util.HashMap;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.genetics.Fitness;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import com.henson.midp.Float11;

/**
 * 
 * @author malintha
 *
 */

/**
 * We are not using any control input. hence no B and U
 * 
 * s	state matrix 	[rX		]
 * 						[d(rX)	]
 * 						[rY		]
 * 						[d(rY)	]
 * 	
 * A	state transition matrix [1  dt  0  0]
 * 								[0  1   0  0]
 * 								[0  0   1 dt]
 * 								[0  0   0  1]
 * 
 * Q	process noise can be huge and can't assume to be distributed normally. so omit it.
 * 
 * dt 	period between 2 measurements. 10 seconds.
 * 
 * x	initial state. should be initialized with the initial coordinates.	[XfromLatLon]
 * 																			[dX			]
 * 																			[YfromLatLon]
 * 																			[dY			]
 * H 	transformation matrix for x to z	[1 0 0 0]
 * 											[0 0 1 0]
 * 
 * R	measurement noise	[X] //a generic measurement error 10m
 * 							[Y]
 * 
 * m_noise  measurement noise which is assumed to be normally distributed around R
 * 
 * 
 * z	Resulting measurement matrix	[X]
 * 										[Y]
 * 
 * Vx	measurement error X
 * Vy	measurement error Y
 * 
 * P0	initial error covariance	[1 1 1 1]
 * 									[1 1 1 1]
 * 									[1 1 1 1]
 * 									[1 1 1 1]
 * 
 */
public class KalmanF {
	private static final double PI_DIV_180 = 0.017453292519943295769236907684886;
	public static final double DEGREES_PER_RADIAN = 57.295779513082320876798154814105;
	public static final long a = 6378200;
	public static final long b = 6356600;
	public static final long height = 10; //ellipsoidal height
	private static HashMap gpshm = new HashMap();
	private static double rX;
	private static double rY;
	private static double rZ;
	
	public static void main(String[] args){
		doKalman(6.881243, 79.89043, 133.00, 20.000);
		
		setgpshm(6.880756, 79.89090, 139.00, 29.000, 1.4, 2.1);
		doCorrect();
		System.out.println(getEstimation()[0]+", "+getEstimation()[1]);

		
		setgpshm(6.880080, 79.89117, 179.00, 24.000, 1.1, 1.8);
		doCorrect();
		System.out.println(getEstimation()[0]+", "+getEstimation()[1]);
		
	}
	
	
	public static void setgpshm(double lat, double lon,double course,double velocity, double Vx, double Vy){
		double dX,dY;
		double[] dxy;

		rX = xFromLonLatH(lat, lon, height, a, b);
		rY = yFromLonLatH(lat, lon, height, a, b);
		rZ = zFromLatH(lat, lon, a, b);
		
		dxy = dXY(course, velocity);
		
		gpshm.put("rX", rX);
		gpshm.put("rY", rY);
		gpshm.put("rZ", rZ);
		gpshm.put("dX", dxy[0]);
		gpshm.put("dY", dxy[1]);
		gpshm.put("Vx", Vx);
		gpshm.put("Vy", Vy);
		
		//System.out.println("rX:"+rX+", dX:"+dxy[0]+", rY:"+rY+", dY:"+dxy[1]);
	}
	

    private static double xFromLonLatH(final double PHI, final double LAM, final double H, final double a, final double b) {
    	
        final double RadPHI = PHI * PI_DIV_180;
        final double RadLAM = LAM * PI_DIV_180;
        // Compute eccentricity squared and nu
        final double e2 = (Float11.pow(a, 2) - Float11.pow(b, 2)) / Float11.pow(a, 2);
        final double V = a / (Math.sqrt(1 - (e2 * (Float11.pow(Math.sin(RadPHI), 2)))));
        // Compute X
        return (V + H) * (Math.cos(RadPHI)) * (Math.cos(RadLAM));
    }
	
    private static double yFromLonLatH(final double PHI, final double LAM, final double H, final double a, final double b) {
        final double RadPHI = PHI * PI_DIV_180;
        final double RadLAM = LAM * PI_DIV_180;
        final double e2 = (Float11.pow(a, 2) - Float11.pow(b, 2)) / Float11.pow(a, 2);
        final double V = a / (Math.sqrt(1 - (e2 * (Float11.pow(Math.sin(RadPHI), 2)))));
        return (V + H) * (Math.cos(RadPHI)) * (Math.sin(RadLAM));
    }
    
    private static double zFromLatH(final double PHI, final double H, final double a, final double b) {
        final double RadPHI = PHI * PI_DIV_180;
        final double e2 = (Float11.pow(a, 2) - Float11.pow(b, 2)) / Float11.pow(a, 2);
        final double V = a / (Math.sqrt(1 - (e2 * (Float11.pow(Math.sin(RadPHI), 2)))));
        return ((V * (1 - e2)) + H) * (Math.sin(RadPHI));
    }
    
    private static double latFromXYZ(final double X, final double Y, final double Z, final double a, final double b) {

        final double RootXYSqr = Math.sqrt(Float11.pow(X, 2) + Float11.pow(Y, 2));
        final double e2 = (Float11.pow(a, 2) - Float11.pow(b, 2)) / Float11.pow(a, 2);
        final double PHI1 = Float11.atan2(Z, (RootXYSqr * (1 - e2)));
        final double PHI = iterateLatFromXYZ(a, e2, PHI1, Z, RootXYSqr);
        return PHI * DEGREES_PER_RADIAN;
    }
    private static double iterateLatFromXYZ(final double a, final double e2, double PHI1, final double Z, final double RootXYSqr) {

        double V = a / (Math.sqrt(1 - (e2 * Float11.pow(Math.sin(PHI1), 2))));
        double PHI2 = Float11.atan2((Z + (e2 * V * (Math.sin(PHI1)))), RootXYSqr);

        while (Math.abs(PHI1 - PHI2) > 0.000000000001) {
            PHI1 = PHI2;
            V = a / (Math.sqrt(1 - (e2 * Float11.pow(Math.sin(PHI1), 2))));
            PHI2 = Float11.atan2((Z + (e2 * V * (Math.sin(PHI1)))), RootXYSqr);
        }
        return PHI2;
    }

private static double lonFromXYZ(final double X, final double Y) {
    return Float11.atan2(Y, X) * DEGREES_PER_RADIAN;
}

public static double[] dXY(double course,double velocity){
	double dX = velocity*Math.sin(course);
	double dY = velocity*Math.cos(course);
	return new double[] {dX,dY};
}


private static RealVector x;
private static RealMatrix B;
private static RealMatrix Q;
private static RealMatrix P0;
private static RealMatrix R;
private static RealMatrix H;
private static RealMatrix A;
private static RealVector m_noise;
private static ProcessModel pm;
private static MeasurementModel mm;
private static KalmanFilter filter;


public static void doKalman(double lat, double lon, double course, double velocity){
	System.out.println("i : "+lat+" , "+lon);
	double initX = xFromLonLatH(lat, lon, height, a, b);
	double initY = yFromLonLatH(lat, lon, height, a, b);
	double[] initVelocity = dXY(course, velocity);

	
	double dt = 10d;	
	A = new Array2DRowRealMatrix(new double[][] { 	{ 1d, dt, 0d, 0d }, 
													{ 0d, 1d, 0d, 0d }, 
													{ 0d, 0d, 1d, dt },
													{ 0d, 0d, 0d, 1d } 
												});
	//initialize with initial coordinates
	x = new ArrayRealVector(new double[] { initX, initVelocity[0], initY, initVelocity[1] });
	//System.out.println("init : "+x);
	B = null;
	
	Q = new Array2DRowRealMatrix(new double[][]{	{1d, 0d, 0d, 0d},
													{0d, 1d, 0d, 0d},
													{0d, 0d, 1d, 0d},
													{0d, 0d, 0d, 1d}
												});
	

	R = new Array2DRowRealMatrix(new double[][] { 	{ 3d, 0d, 0d, 0d },
													{ 0d, 1d, 0d, 0d },
													{ 0d, 0d, 3d, 0d },
													{ 0d, 0d, 0d, 1d }
													});
	
	H = new Array2DRowRealMatrix(new double[][] { 	{ 1d, 0d, 0d, 0d },
													{ 0d, 1d, 0d, 0d },
													{ 0d, 0d, 1d, 0d },
													{ 0d, 0d, 0d, 1d }
													});
	
	P0 = new Array2DRowRealMatrix(new double[][] {{1d, 0d, 0d, 0d},
													{0d, 1d, 0d, 0d},
													{0d, 0d, 1d, 0d},
													{0d, 0d, 0d, 1d}});
	m_noise = new ArrayRealVector(4);
	
	pm = new DefaultProcessModel(A, B, Q, x, P0);
	mm = new DefaultMeasurementModel(H, R);
	filter = new KalmanFilter(pm, mm);
}

	public static void doCorrect(){
		//every changing matrix should be set in here

		//xt = A*x(t-1)
		//this should help to predict the next state

		//x = A.operate(x);
		//System.out.println("a : "+filter.getStateEstimationVector());
		//now predict
		filter.predict();
		//System.out.println("p : "+filter.getStateEstimationVector());

		double[] m_noise_array = {(Double) gpshm.get("Vx"),(Double) gpshm.get("Vy")};
		
		m_noise.setEntry(0, m_noise_array[0]);
		m_noise.setEntry(1, Math.pow(m_noise_array[0], 0.5));
		m_noise.setEntry(2, m_noise_array[1]);
		m_noise.setEntry(3, Math.pow(m_noise_array[0], 0.5));
		
		//z = H*x + m_noise
		//RealVector z = H.operate(x).add(m_noise);
		RealVector z = new ArrayRealVector(4);
		//get next estimates and add with error
		z.setEntry(0, (Double) gpshm.get("rX"));
		z.setEntry(1, (Double) gpshm.get("dX"));
		z.setEntry(2, (Double) gpshm.get("rY"));
		z.setEntry(3, (Double) gpshm.get("dY"));
		//System.out.println("z : "+z);
		z = z.add(m_noise);
		//System.out.println("z' : "+z);
		//now correct
		filter.correct(z);
		
		//System.out.println("c : "+filter.getStateEstimationVector());
		}
	
	public static RealVector getEstimateVector(){
		return filter.getStateEstimationVector();
	}
	
	public static double[] getEstimation(){
		double tempRx, tempRy, tempRz;
		double[] tempEstimation = filter.getStateEstimation();
		tempRx = tempEstimation[0];
		tempRy = tempEstimation[2];
		tempRz = (Double) gpshm.get("rZ");
		
		double lat = latFromXYZ(tempRx, tempRy, tempRz, a, b);
		double lon = lonFromXYZ(tempRx, tempRy);


		return new double[] {lat, lon};
	}

}
