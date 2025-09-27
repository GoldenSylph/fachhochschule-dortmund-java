package de.fachhochschule.dortmund.bedrin.facility.interfaces;

public interface IResource<I, O> {
	public O getData();
	public void setData(I newData);
}
