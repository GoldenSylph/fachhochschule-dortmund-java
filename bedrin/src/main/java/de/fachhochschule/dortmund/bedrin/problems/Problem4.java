package de.fachhochschule.dortmund.bedrin.problems;

import java.util.stream.Stream;

import de.fachhochschule.dortmund.bedrin.problems.interfaces.IProblem;

public class Problem4 implements IProblem {

	@Override
	public void demonstrateSolution() {
		System.out.println("START OF PROBLEM4 SOLUTION");
		Stream.of(Problem3.TEXT.split(" "))
				.map(e -> e.replaceAll("\\W", " "))
				.map(String::toLowerCase)
				.map(String::trim)
				.sorted()
				.forEach(System.out::println);
		System.out.println("END OF PROBLEM3 SOLUTION");
	}

}
