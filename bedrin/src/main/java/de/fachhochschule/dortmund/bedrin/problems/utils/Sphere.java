package de.fachhochschule.dortmund.bedrin.problems.utils;

public class Sphere {
	protected String name;
	protected USLength diameter;
	protected USLength radius;
	protected double volumeInCubicMiles;
	protected double circleCutAreaInSquareMeters;
	
	public Sphere(String initialName, USLength initialDiameter) {
		this.name = initialName;
		this.diameter = initialDiameter;
		this.radius = new USLength(initialDiameter.getMeters() / 2.0);
		double radiusMeters = getRadius().getMeters(); // Utilizing meters to avoid precision losses
		// We will not utilize Lagrangian in Math.pow(...) to approximate small powers
		this.circleCutAreaInSquareMeters = Math.PI * radiusMeters * radiusMeters;
		double radiusMiles = radiusMeters / USLength.MILES_MULTIPLIER;
		this.volumeInCubicMiles = ((4.0 * Math.PI * radiusMiles * radiusMiles * radiusMiles) / 3.0);
	}
	
	public Sphere(String initialName, double initialCircleCutAreaInSquareMeters) {
		this.name = initialName;
		this.circleCutAreaInSquareMeters = initialCircleCutAreaInSquareMeters;
		this.radius = new USLength(Math.sqrt(getCircleCutArea() / Math.PI));
		this.diameter = new USLength(getRadius().getMeters() * 2.0);
		double radiusMeters = getRadius().getMeters(); // Utilizing meters to avoid precision losses
		double radiusMiles = radiusMeters / USLength.MILES_MULTIPLIER;
		this.volumeInCubicMiles = ((4.0 * Math.PI * radiusMiles * radiusMiles * radiusMiles) / 3.0);
	}
	
	public String getName() {
		return name;
	}

	public USLength getDiameter() {
		return diameter;
	}

	public USLength getRadius() {
		return radius;
	}

	public double getVolume() {
		return volumeInCubicMiles;
	}

	public double getCircleCutArea() {
		return circleCutAreaInSquareMeters;
	}

	public void printInfo() {
		String formattedName = getName().toUpperCase();
		System.out.println("--- Info about \"" + formattedName + "\" start ---");
		System.out.print("The radius of a circle cut with area ");
		System.out.print(getCircleCutArea());
		System.out.print(" square meters is ");
		USLength radius = getRadius();
		System.out.println(radius.getMiles() + " miles " + radius.getFeet() + " feet " + radius.getInches() + " inches.");
		System.out.println("The volume in cubic miles is " + getVolume());
		System.out.println("--- Info about \"" + formattedName  + "\" end ---");
	}
}
