package org.geo.cep.extension;

import org.apache.commons.math3.filter.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import com.henson.midp.Float11;

import java.util.HashMap;

/**
 * Created by malintha on 7/24/14.
 */

public class KalmanFIlter {
    private static final double PI_DIV_180 = 0.017453292519943295769236907684886;
    public static final double DEGREES_PER_RADIAN = 57.295779513082320876798154814105;
    public static final long a = 6378200;
    public static final long b = 6356600;
    public static final long height = 10; //ellipsoidal height
    private static HashMap gpshm = new HashMap();
    private static double rX;
    private static double rY;
    private static double rZ;
    private final static String FILE_PATH = "/home/malintha/Desktop/Kalman_project/Workspace/Kalman_testing/src/com/m8/kalman/kalmanConfig.csv";
    private static int Matcounter = 0;
    private static int Linecounter = 0;

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

        System.out.print("#####setgpshm");
    }

    /**
     * Kalman filter starts from here.
     *
     * @param lat       latitude
     * @param lon       longitude
     * @param course    course
     * @param velocity  velocity
     */
    public static void doKalman(double lat, double lon, double course, double velocity){

        System.out.println("i : "+lat+" , "+lon);
        double initX = xFromLonLatH(lat, lon, height, a, b);
        double initY = yFromLonLatH(lat, lon, height, a, b);
        double[] initVelocity = dXY(course, velocity);

        //set in config
        double dt = 10d;

        //set in config
        A = new Array2DRowRealMatrix(new double[][] { 	{ 1d, dt, 0d, 0d },
                                                        { 0d, 1d, 0d, 0d },
                                                        { 0d, 0d, 1d, dt },
                                                        { 0d, 0d, 0d, 1d }
                                                });
        //initialize with initial coordinates
        x = new ArrayRealVector(new double[] { initX, initVelocity[0], initY, initVelocity[1] });
        //System.out.println("init : "+x);

        //set in config
        B = null;

        //set in config
        Q = new Array2DRowRealMatrix(new double[][]{	{1d, 0d, 0d, 0d},
                                                        {0d, 1d, 0d, 0d},
                                                        {0d, 0d, 1d, 0d},
                                                        {0d, 0d, 0d, 1d}
                                                });
//	set in config
        R = new Array2DRowRealMatrix(new double[][] { 	{ 3d, 0d, 0d, 0d },
                                                        { 0d, 1d, 0d, 0d },
                                                        { 0d, 0d, 3d, 0d },
                                                        { 0d, 0d, 0d, 1d }
                                                });
//	set in config
        H = new Array2DRowRealMatrix(new double[][] { 	{ 1d, 0d, 0d, 0d },
                                                        { 0d, 1d, 0d, 0d },
                                                        { 0d, 0d, 1d, 0d },
                                                        { 0d, 0d, 0d, 1d }
                                                });
//	set in config
        P0 = new Array2DRowRealMatrix(new double[][] {  { 1d, 0d, 0d, 0d },
                                                        { 0d, 1d, 0d, 0d },
                                                        { 0d, 0d, 1d, 0d },
                                                        { 0d, 0d, 0d, 1d }
                                                });

//	set in config
        m_noise = new ArrayRealVector(4);

        pm = new DefaultProcessModel(A, B, Q, x, P0);
        mm = new DefaultMeasurementModel(H, R);
        filter = new KalmanFilter(pm, mm);
    }

    public static RealVector getEstimateVector(){
        return filter.getStateEstimationVector();
    }

    public static void doCorrect(){
        //every changing matrix should be set in here

        //Prediction equation xt = A*x(t-1)

        //now predict
        filter.predict();

        double[] m_noise_array = {(Double) gpshm.get("Vx"),(Double) gpshm.get("Vy")};

        m_noise.setEntry(0, m_noise_array[0]);
        m_noise.setEntry(1, Math.pow(m_noise_array[0], 0.5));
        m_noise.setEntry(2, m_noise_array[1]);
        m_noise.setEntry(3, Math.pow(m_noise_array[0], 0.5));

        //z = H*x + m_noise

        RealVector z = new ArrayRealVector(4);
        //get next estimates and add with error
        z.setEntry(0, (Double) gpshm.get("rX"));
        z.setEntry(1, (Double) gpshm.get("dX"));
        z.setEntry(2, (Double) gpshm.get("rY"));
        z.setEntry(3, (Double) gpshm.get("dY"));
        //System.out.println("z : "+z);
        z = z.add(m_noise);

        //now correct
        filter.correct(z);
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
