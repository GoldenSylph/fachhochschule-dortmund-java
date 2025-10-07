public class problem1 {
    public static void main(String[] args) {
        double radius = 0.0;
        double circleArea = 100.0;
        int feet = 0;
        int inches = 0;
        radius = Math.sqrt(circleArea/Math.PI);
        feet = (int)Math.floor(radius);
        inches = (int)Math.round(12.0*(radius - feet));
        System.out.println("The radius of a circle with area " + circleArea
        + " square feet is\n " + feet + " feet " + inches + " inches ");

        // Calculate volumes of Earth and Sun
        double earthDiameter = 7600.0;
        double sunDiameter = 865000.0;

        double earthRadius = earthDiameter / 2.0;
        double sunRadius = sunDiameter / 2.0;

        // Volume of sphere = (4/3) * pi * r^3
        double earthVolume = (4.0 / 3.0) * Math.PI * Math.pow(earthRadius, 3);
        double sunVolume = (4.0 / 3.0) * Math.PI * Math.pow(sunRadius, 3);

        // Ratio of Sun volume to Earth volume
        double volumeRatio = sunVolume / earthVolume;

        System.out.println("\nThe volume of the Earth is " + earthVolume + " cubic miles,");
        System.out.println("the volume of the Sun is " + sunVolume + " cubic miles,");
        System.out.println("and the ratio of the volume of the Sun to the volume of the Earth is " + volumeRatio + ".");
    }

}
