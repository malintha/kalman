package com.m8.kalman;

import java.util.HashMap;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import com.henson.midp.Float11;

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
		doKalman(6.87308728, 79.87337101, 310.1, 4.25);
		setgpshm(6.87595897, 79.86998918, 337.8, 12.093387, 11, 8);
		doCorrect();
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
		gpshm.put("dX", dxy[0]);
		gpshm.put("dY", dxy[1]);
		gpshm.put("Vx", Vx);
		gpshm.put("Vy", Vy);
		
		System.out.println("rX:"+rX+", dX:"+dxy[0]+", rY:"+rY+", dY:"+dxy[1]);
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
	
	double initX = xFromLonLatH(lat, lon, height, a, b);
	double initY = yFromLonLatH(lat, lon, height, a, b);
	double[] initVelocity = dXY(course, velocity);
	
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
	 * dt 	period between 2 measurements. lets say 2 seconds.
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
	
	double Vx;
	double Vy;
	
	double dt = 77d;	
	A = new Array2DRowRealMatrix(new double[][] { 	{ 1d, dt, 0d, 0d }, 
													{ 0d, 1d, 0d, 0d }, 
													{ 0d, 0d, 1d, dt },
													{ 0d, 0d, 0d, 1d } 
												});
	//initialize with initial coordinates
	x = new ArrayRealVector(new double[] { initX, initVelocity[0], initY, initVelocity[1] });
	System.out.println("init : "+x);
	B = null;
	Q = new Array2DRowRealMatrix(new double[][]{	{0, 0, 0, 0},
													{0, 0, 0, 0},
													{0, 0, 0, 0},
													{0, 0, 0, 0}
												});
	
	P0 = new Array2DRowRealMatrix(new double[][] {	{ 1, 1, 1, 1 }, 
													{ 1, 1, 1, 1 },
													{ 1, 1, 1, 1 },
													{ 1, 1, 1, 1 }
												});

	double[] generic_error = {10d,};
	R = new Array2DRowRealMatrix(generic_error);
	
	H = new Array2DRowRealMatrix(new double[][] { 	{ 1d, 0d, 1d, 0d } });
	m_noise = new ArrayRealVector(1);
	
	pm = new DefaultProcessModel(A, B, Q, x, P0);
	mm = new DefaultMeasurementModel(H, R);
	filter = new KalmanFilter(pm, mm);
}


//public static void main(String args[]){
//	// discrete time interval
//	double dt = 1d;
//	// position measurement noise (meter)
//	double measurementNoise = 10d;
//	// acceleration noise (meter/sec^2)
//	double accelNoise = 0;
//
//	// A = [ 1 dt ]
//	//     [ 0  1 ]
//	RealMatrix A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });
//	// B = [ dt^2/2 ]
//	//	   [ dt     ]
//	RealMatrix B = new Array2DRowRealMatrix(new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });
//	// H = [ 1 0 ]
//	RealMatrix H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });
//	// x = [ 0; 0 ]
//	RealVector x = new ArrayRealVector(new double[] { 0, 0 });
//
//	RealMatrix tmp = new Array2DRowRealMatrix(new double[][] {
//	    { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
//	    { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });
//	// Q = 		[ dt^4/4 dt^3/2 ]
////	     	[ dt^3/2 dt^2   ]
//	RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));
//	// P0 = 	[ 1 1 ]
////	      	[ 1 1 ]
//	RealMatrix P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });
//	// R = [ measurementNoise^2 ]
//	RealMatrix R = new Array2DRowRealMatrix(new double[] { Math.pow(measurementNoise, 2) });
//
//	// constant control input, increase velocity by 0.1 m/s per cycle
//	//RealVector u = new ArrayRealVector(new double[] { 0.1d });
//
//	ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
//	MeasurementModel mm = new DefaultMeasurementModel(H, R);
//	KalmanFilter filter = new KalmanFilter(pm, mm);
//
//	RandomGenerator rand = new JDKRandomGenerator();
//
//	RealVector tmpPNoise = new ArrayRealVector(new double[] { Math.pow(dt, 2d) / 2d, dt });
//	RealVector mNoise = new ArrayRealVector(1);
//
//	// iterate 10 steps
//	for (int i = 0; i < 10; i++) {
//		// filter.predict(u);
//	    //System.out.println("Predicted value at timestamp "+i+" : "+x);
//	    // simulate the process
//	    RealVector pNoise = tmpPNoise.mapMultiply(rand.nextGaussian());
//
//	    // x = A * x + B * u + pNoise
//	    x = A.operate(x).add(pNoise);
//
//	    // simulate the measurement
//	    mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
//
//	    // z = H * x + m_noise
//	    RealVector z = H.operate(x).add(mNoise);
//	    
//	    filter.correct(z);
//	    System.out.println("Corrected value at timestamp "+i+" : "+x);
//	    double position = filter.getStateEstimation()[0];
//	    double velocity = filter.getStateEstimation()[1];
//	}
//}
	
	public static void doCorrect(){
		//every changing matrix should be set in here

		//R.setColumn(0, new double[] {(Double) gpshm.get("Vx"),(Double) gpshm.get("Vy")});

		//xt = A*x(t-1)
		//this should help to predict the next state
		x = A.operate(x);
//		System.out.println("after A.X(t-1)"+x);
		//now predict
		filter.predict();
		System.out.println("p : "+x);
		
		
		//set new values
//		x.setEntry(0, (Double) gpshm.get("rX"));
//		x.setEntry(1, (Double) gpshm.get("dX"));
//		x.setEntry(2, (Double) gpshm.get("rY"));
//		x.setEntry(3, (Double) gpshm.get("dY"));
		
		//System.out.println("1 : "+x);
		double[] m_noise_array = {(Double) gpshm.get("Vx"),(Double) gpshm.get("Vy")};
		m_noise.setEntry(0, m_noise_array[0]);
		//m_noise.setEntry(1, m_noise_array[1]);
		
		//z = H*x + m_noise
		RealVector z = H.operate(x).add(m_noise);
		System.out.println(z+"\n"+x);
		//now correct
		
		//filter.correct(z);
		//System.out.println("c : "+x);
	}
	
}


