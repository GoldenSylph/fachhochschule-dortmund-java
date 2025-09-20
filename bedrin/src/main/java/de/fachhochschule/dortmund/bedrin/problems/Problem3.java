package de.fachhochschule.dortmund.bedrin.problems;

import de.fachhochschule.dortmund.bedrin.problems.interfaces.IProblem;

public class Problem3 implements IProblem {
	public static final String TEXT = "To be or not to be, that is the question;" + "Whether `tis nobler in the mind to suffer"
			+ " the slings and arrows of outrageous fortune," + " or to take arms against a sea of troubles,"
			+ " and by opposing end them?";
	
	@Override
	public void demonstrateSolution() {
		System.out.println("START OF PROBLEM3 SOLUTION");
		int spaces = TEXT.chars().filter(e -> { return ((char) e + "").matches("\s"); }).toArray().length;
		int vowels = TEXT.chars().filter(e -> { return ((char) e + "").matches("[aeiyuo]"); }).toArray().length;
		int letters = TEXT.trim().length();
		System.out.println(
				"The text contained vowels: " + vowels + "\nconsonants: " + (letters - vowels) + "\nspaces: " + spaces);
		System.out.println("END OF PROBLEM3 SOLUTION");
	}

}
