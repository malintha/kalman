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

public class Filter {
	public static void main(String args[]){
		//state vector position(lon,lat object), velocity
		//control vector time/acceleration?
		/**
		 * state vector[posX,velocity]
		 * measurement vector [pos] - mm
		 * control vector [a]
		 * position noise - mm = measurement noise
		 * velocity noise
		 * initial state estimate matrix [pos,vel]
		 * 
		 * 
		 */
		
//	Double[][] stateVector = new Double[][] {
//			{1d,0.1},
//			{0d, 1d}
//	};
//	Double[] controlInput = new Double[] {1d};
//	
//	Double[] initCovariance = new Double[] {0d};
//	
//	Double[] processNoiseCovariance = new Double[] {0d};
//	
//	Double[] initErrorCovariance = new Double[] {0d};
	
		RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
		// no control input
		RealMatrix B = new Array2DRowRealMatrix(new double[] { 1d });
		// H = [ 1 ]
		RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
		// Q = [ 0 ]
		RealMatrix Q = new Array2DRowRealMatrix(new double[] { 1d });
		// R = [ 0 ]
		RealMatrix R = new Array2DRowRealMatrix(new double[] { 0 });

		ProcessModel pm
		   = new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[] { 0 }), null);
		MeasurementModel mm = new DefaultMeasurementModel(H, R);
		KalmanFilter filter = new KalmanFilter(pm, mm);
		
		for(int i=0;i<5;i++){
			filter.predict();
		System.out.println(filter.getStateEstimation()[0]);
		}
		
	}
}
