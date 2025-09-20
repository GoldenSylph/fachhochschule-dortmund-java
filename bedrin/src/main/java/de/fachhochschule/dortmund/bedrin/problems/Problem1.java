package de.fachhochschule.dortmund.bedrin.problems;

import de.fachhochschule.dortmund.bedrin.problems.interfaces.IProblem;
import de.fachhochschule.dortmund.bedrin.problems.utils.Sphere;
import de.fachhochschule.dortmund.bedrin.problems.utils.USLength;

public class Problem1 implements IProblem {
	
	@Override
	public void demonstrateSolution() {
		System.out.println("START OF PROBLEM1 SOLUTION");
		Sphere sun = new Sphere("Sun", new USLength(865000, 0, 0));	
		Sphere earth = new Sphere("Earth", new USLength(7600, 0, 0));
		sun.printInfo();
		earth.printInfo();
		double ratio = sun.getVolume() / earth.getVolume();
		System.out.println("A ratio of Sun's volume to Earth's is " + ratio);
		System.out.println("END OF PROBLEM1 SOLUTION");
	}
}
