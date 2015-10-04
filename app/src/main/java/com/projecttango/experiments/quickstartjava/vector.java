package com.projecttango.experiments.quickstartjava;

/**
 * Created by Kiyoon on 2015-10-04.
 */
public class vector {
    private double x;
    private double y;
    private double z;
    public vector(){
        x=0;
        y=0;
        z=0;
    }

    public vector(double x1, double y1, double z1){
        x = x1;
        y = y1;
        z = z1;
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }

    public void setX(double x2){
        this.x = x2;
    }

    public void setY(double y2){
        this.y = y2;
    }

    public void setZ(double z2){
        this.z = z2;
    }
}