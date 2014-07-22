package com.m8.kalman;

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

public class Filter {
	public static void main(String args[]){
		// discrete time interval
		double dt = 1d;
		// position measurement noise (meter)
		double measurementNoise = 10d;
		// acceleration noise (meter/sec^2)
		double accelNoise = 0;
	
		// A = [ 1 dt ]
		//     [ 0  1 ]
		RealMatrix A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });
		// B = [ dt^2/2 ]
		//	   [ dt     ]
		RealMatrix B = new Array2DRowRealMatrix(new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });
		// H = [ 1 0 ]
		RealMatrix 	H = new Array2DRowRealMatrix(new double[][] { 	{ 1d, 0d, 0d, 0d }, 
																	{ 0d, 0d, 1d, 0d }
			});
		// x = [ 0; 0 ]
		RealVector x = new ArrayRealVector(new double[] { 0, 0, 0, 0 });
	
		RealMatrix tmp = new Array2DRowRealMatrix(new double[][] {
		    { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
		    { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });
		// Q = 		[ dt^4/4 dt^3/2 ]
//		     	[ dt^3/2 dt^2   ]
		RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));
		// P0 = 	[ 1 1 ]
//		      	[ 1 1 ]
		RealMatrix P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });
		// R = [ measurementNoise^2 ]
		RealMatrix R = new Array2DRowRealMatrix(new double[] { Math.pow(measurementNoise, 2) });
	
		// constant control input, increase velocity by 0.1 m/s per cycle
		//RealVector u = new ArrayRealVector(new double[] { 0.1d });
	
		ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
		MeasurementModel mm = new DefaultMeasurementModel(H, R);
		KalmanFilter filter = new KalmanFilter(pm, mm);
	
		RandomGenerator rand = new JDKRandomGenerator();
	
		RealVector tmpPNoise = new ArrayRealVector(new double[] { Math.pow(dt, 2d) / 2d, dt });
		RealVector mNoise = new ArrayRealVector(2);
	
		// iterate 10 steps
		for (int i = 0; i < 10; i++) {
			// filter.predict(u);
		    //System.out.println("Predicted value at timestamp "+i+" : "+x);
		    // simulate the process
		    RealVector pNoise = tmpPNoise.mapMultiply(rand.nextGaussian());
	
		    // x = A * x + B * u + pNoise
		    x = A.operate(x).add(pNoise);
	
		    // simulate the measurement
		    mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
		    mNoise.setEntry(1, measurementNoise * rand.nextGaussian());
		    // z = H * x + m_noise
		    RealVector z = H.operate(x).add(mNoise);
		    
		    filter.correct(z);
		    System.out.println("Corrected value at timestamp "+i+" : "+x);
		    double position = filter.getStateEstimation()[0];
		    double velocity = filter.getStateEstimation()[1];
		}
	}
}
