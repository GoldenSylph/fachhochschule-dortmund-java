package de.fachhochschule.dortmund.bedrin;

import de.fachhochschule.dortmund.bedrin.problems.Problem1;
import de.fachhochschule.dortmund.bedrin.problems.Problem2;
import de.fachhochschule.dortmund.bedrin.problems.Problem3;
import de.fachhochschule.dortmund.bedrin.problems.Problem4;
import de.fachhochschule.dortmund.bedrin.problems.interfaces.IProblem;

public class Homeworks {
	
	public static void main(String[] args) {
		IProblem firstExercise = new Problem1();
		IProblem secondExercise = new Problem2();
		IProblem thirdExercise = new Problem3();
		IProblem fourthExercise = new Problem4();

		firstExercise.demonstrateSolution();
		secondExercise.demonstrateSolution();
	}
}
