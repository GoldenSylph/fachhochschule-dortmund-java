package de.fachhochschule.dortmund.bedrin.facility.interfaces;

public interface ICPU<I, O> {
	public O getOutput();
	public void executeProgram(I program);
	public void cacheProgram(I program);
}
