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

/**
 * 
 * @author malintha
 * 
 *         s state matrix [r] [dr]
 * 
 *         A state transition matrix [1 dt] [0 1]
 * 
 *         dt time period
 * 
 *         x initial state, initialize with initial data [r] [dr]
 * 
 *         H transformation matrix from x to z [1 0]
 * 
 *         R measurement noise, calculated from reference node [V]
 * 
 *         z Resulting measurement matrix [r] [dr]
 * 
 * 
 */

public class BlueKalman {

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
	private static double varianceR;

	static final double dt = 1;
	static double initdR = 0;
	static double dR = 0;
	static double lastReceived = 0;

	public static void main(String[] args) {

	}

	/**
	 * Before doCorrect(), set the noise for the value
	 * 
	 * @param vR
	 *            noise value generated using the output of reference node
	 */
	public static void setNoise(double vR) {
		varianceR = vR;
	}
/**
 * 
 * @param nowR	new distance value
 * @return instantaneous speed
 */
	public static double getdR(double nowR) {
		return (nowR-lastReceived)/dt;
	}

	/**
	 * initialize the kalman filter with first value.
	 * 
	 * @param initR
	 *            initial distance from the sensor
	 */
	public static void doKalman(double initR) {

		lastReceived = initR;
		
		A = new Array2DRowRealMatrix(new double[][] { { 1d, dt }, { 0d, 1d, } });
		B = null;
		Q = new Array2DRowRealMatrix(new double[][] { { 1d, 0d }, { 0d, 1d } });
		H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });
		x = new ArrayRealVector(new double[] { initR, getdR(initR) });
		P0 = new Array2DRowRealMatrix(new double[][] { { 1d, 0d }, { 0d, 1d } });
		pm = new DefaultProcessModel(A, B, Q, x, P0);
		mm = new DefaultMeasurementModel(H, R);
		filter = new KalmanFilter(pm, mm);

	}

	/**
	 * run this every time a distance receives
	 * 
	 * @param nowR received distance
	 * @param spdt_1 speeed at t-1 time. used in the measurement model.
	 */
	public static void doCorrect(double nowR) {
		filter.predict();
		
		x.setEntry(0, nowR);
		x.setEntry(1, getdR(nowR));
		R = new Array2DRowRealMatrix(new double[] { varianceR });
		m_noise.setEntry(0, varianceR);
		RealVector z = H.operate(x).add(m_noise);

		filter.correct(z);
	}

	public static double[] getStates() {
		return filter.getStateEstimation();
	}
}
