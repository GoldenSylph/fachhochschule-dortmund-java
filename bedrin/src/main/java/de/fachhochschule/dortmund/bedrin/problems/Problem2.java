package de.fachhochschule.dortmund.bedrin.problems;

import de.fachhochschule.dortmund.bedrin.problems.interfaces.IProblem;

public class Problem2 implements IProblem  {

	@Override
	public void demonstrateSolution() {
		System.out.println("START OF PROBLEM2 SOLUTION");
		oldSolution();
		System.out.println("--- NEW SOLUTION ---");
		newSolution();
		System.out.println("END OF PROBLEM2 SOLUTION");
	}
	
	private void oldSolution() {
		int nValues = 50;
		boolean isPrime = true;
		for (int i = 2; i < nValues; i++) {
			isPrime = true;
			for (int j = 2; j < i; j++) {
				if (i % j == 0) {
					isPrime = false;
					break;
				}
			}
			if (isPrime) {
				System.out.println(i);
			}
		}
	}
	
	private void newSolution() {
		int nValues = 50;
		main:
		for (int i = 2; i < nValues; i++) {
			if (i == 2) {
				System.out.println(i);
				continue;
			}
			int upperLimit = (int) Math.ceil(Math.sqrt(i));
			for (int j = 2; j <= upperLimit; j++) {
				if (i % j == 0) {					
					continue main;
				}
			}
			System.out.println(i);
		}
	}

}
