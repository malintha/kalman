package org.geo.cep.extension;

import org.apache.commons.math3.filter.*;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.HashMap;

/**
 * Created by malintha on 8/2/14.
 */
public class MathKalman {

    private RealVector x;
    private RealVector u;
    private RealMatrix B;
    private RealMatrix Q;
    private RealMatrix P0;
    private RealMatrix R;
    private RealMatrix H;
    private RealMatrix A;
    private ProcessModel pm;
    private MeasurementModel mm;
    private KalmanFilter filter;
    private MatrixSetter ms;
    private HashMap<String,Double> datahm = new HashMap<String, Double>();

    public static void main(String[] args) {
        MathKalman mk = new MathKalman();
        mk.initializeMatrices(0,30,0,51.96);

        mk.doKalmanPredict();
        mk.addCurrentMeasurement(29.59, 29.89, 46.5, 41.56); //41.96
        mk.doKalmanCorrect();
        System.out.println(mk.getEstimation()[0]+" , "+mk.getEstimation()[2]);

        System.out.println();

        mk.doKalmanPredict();
        mk.addCurrentMeasurement(58, 29.56, 84, 30.56); //31.96
        mk.doKalmanCorrect();
        System.out.println(mk.getEstimation()[0]+" , "+mk.getEstimation()[2]);
    }

    public MathKalman() {
        ms = new MatrixSetter();
        ms.readFile();
        System.out.println("###initialized###\n"+ms.getdt()+"\n"+ms.getX()+"\n"+
                ms.getA()+"\n"+ ms.getB()+"\n"+ms.getQ()+"\n"+ms.getR()+"\n"+ms.getH()+"\n"+
                ms.getP0()+"\n"+ms.getU());
    }

    public void initializeMatrices(double Xval, double dx, double Yval, double dy){
        double dt = ms.getdt();
        A = ms.getA();
        B = ms.getB();
        Q = ms.getQ();
        R = ms.getR();
        H = ms.getH();
        P0 = ms.getP0();
        x = ms.getX();
        u = ms.getU();

        pm = new DefaultProcessModel(A, B, Q, x, P0);
        mm = new DefaultMeasurementModel(H, R);
        filter = new KalmanFilter(pm, mm);
        setInitState(Xval, dx,Yval, dy);
    }

    public void setInitState(double Xval, double dx,double Yval, double dy){
        System.out.println("##initstate "+Xval+" "+Yval+" "+dx+" "+dy);
        x.setEntry(0, Xval);
        x.setEntry(1,dx);
        x.setEntry(2,Yval);
        x.setEntry(3,dy);
    }

    public void addCurrentMeasurement(double Xval,double dx,double Yval, double dy){
        datahm.put("rX", Xval);
        datahm.put("rY", Yval);
        datahm.put("dX", dx);
        datahm.put("dY", dy);
    }

    public void doKalmanPredict(){

        filter.predict(u);
        System.out.println("###predictedState" + filter.getStateEstimationVector());
    }

    public void doKalmanCorrect(){


        RealVector z = new ArrayRealVector(4);
        //get next estimates and add with error
        System.out.println(datahm);
        z.setEntry(0, datahm.get("rX"));
        z.setEntry(1, datahm.get("dX"));
        z.setEntry(2, datahm.get("rY"));
        z.setEntry(3, datahm.get("dY"));
        //now correct
        filter.correct(z);
    }

    public double[] getEstimation(){
        double[] tempEstimation = filter.getStateEstimation();
        return tempEstimation;
    }
}
