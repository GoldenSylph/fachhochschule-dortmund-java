package de.fachhochschule.dortmund.bedrin.problems.utils;

/// A class for representing the US length measure.
public class USLength {
	public static final double MILES_MULTIPLIER = 1609.344;
	public static final double FEET_MULTIPLIER = 0.3048;
	public static final double INCHES_MULTIPLIER = 0.0254;
	
	protected double meters;
	protected int miles;
	protected int feet;
	protected int inches;
	
	public USLength(int initialMiles, int initialFeet, int initialInches) {
		setMiles(initialMiles);
		setFeet(initialFeet);
		setInches(initialInches);
		updateMeters();
	}
	
	public USLength(double initialMeters) {
		setMeters(initialMeters);
	}
	
	public double getMeters() {
		return this.meters;
	}
	
	public int getMiles() {
		return miles;
	}

	public void setMiles(int miles) {
		this.miles = miles;
		updateMeters();
	}
	
	public int getFeet() {
		return feet;
	}

	public void setFeet(int feet) {
		this.feet = feet;
		updateMeters();
	}

	public int getInches() {
		return inches;
	}

	public void setInches(int inches) {
		this.inches = inches;
		updateMeters();
	}
	
	protected void updateMeters() {
		this.meters = getMiles() * MILES_MULTIPLIER + getFeet() * FEET_MULTIPLIER + getInches() * INCHES_MULTIPLIER;
	}
	
	protected void setMeters(double newMeters) {
		this.meters = newMeters;
	    this.miles = (int) (getMeters() / MILES_MULTIPLIER);
	    double remainingAfterMiles = getMeters() - getMiles() * MILES_MULTIPLIER;
	    this.feet = (int) (remainingAfterMiles / FEET_MULTIPLIER);
	    double remainingAfterFeet = remainingAfterMiles - getFeet() * FEET_MULTIPLIER;
	    this.inches = (int) Math.floor(remainingAfterFeet / INCHES_MULTIPLIER);
	}
}
