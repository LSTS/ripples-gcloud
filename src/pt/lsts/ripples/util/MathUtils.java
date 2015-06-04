package pt.lsts.ripples.util;

public class MathUtils {

    public static final double TWO_PI_RADS = Math.PI * 2.0;

    private MathUtils() {
    }

    /**
     * @param val
     * @param decimalHouses
     * @return
     */
    public static double round(double val, int decimalHouses) {
        double base = Math.pow(10d, decimalHouses);
        double result = Math.round(val * base) / base;
        return result;
    }

    /**
     * @param val
     * @param decimalHouses
     * @return
     */
    public static float round(float val, int decimalHouses) {
        float base = (float) Math.pow(10f, decimalHouses);
        float result = Math.round(val * base) / base;
        return result;
    }

    /** 
     * @param angle
     * @return the angle between 0 and 2pi
     */
    public static double nomalizeAngleRads2Pi(double angle) {
        double ret = angle;
        ret = ret % TWO_PI_RADS;
        if (ret < 0.0)
            ret += TWO_PI_RADS;
        return ret;
    }

    /**
     * @param angle
     * @return the angle between -pi and pi
     */
    public static double nomalizeAngleRadsPi(double angle) {
        double ret = angle;
        while (ret > Math.PI)
            ret -= TWO_PI_RADS;
        while (ret < -Math.PI)
            ret += TWO_PI_RADS;
        return ret;
    }
    
    /** 
     * @param angle
     * @return the angle between 0 and 360
     */
    public static double nomalizeAngleDegrees360(double angle) {
        double ret = angle;
        ret = ret % 360.0;
        if(ret < 0.0)
            ret+= 360.0;
        return ret;
    }

    /**
     * @param angle
     * @return the angle between -180 and 180
     */
    public static double nomalizeAngleDegrees180(double angle) {
        double ret = angle;
        while (ret > 180)
            ret -= 360;
        while (ret < -180)
            ret += 360;
        return ret;
    }
}
