package problemset;

public class Problem1 {

	public static void main(String[] args) {
        double dEarth = 7600.0;
        double dSun = 865000.0;

        double rEarth = dEarth / 2.0;
        double rSun = dSun / 2.0;

        // V = 4/3 * pi * r^3 
        double vEarth = (4.0/3.0) * Math.PI * Math.pow(rEarth, 3);
        double vSun = (4.0/3.0) * Math.PI * Math.pow(rSun, 3);

        double ratio = vSun / vEarth;

        System.out.println("The volume of the Earth is " + vEarth + " cubic miles.");
        System.out.println("The volume of the Sun is " + vSun + " cubic miles.");
        System.out.println("The ratio of the volume of the Sun to the Earth is " + ratio);
    }
}
