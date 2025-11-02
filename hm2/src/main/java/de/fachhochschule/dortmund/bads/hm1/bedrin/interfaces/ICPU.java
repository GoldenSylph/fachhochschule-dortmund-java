package de.fachhochschule.dortmund.bads.hm1.bedrin.interfaces;

public interface ICPU<I, O> {
	public O getOutput();
	public void executeProgram(I program);
	public void cacheProgram(I program);
}
