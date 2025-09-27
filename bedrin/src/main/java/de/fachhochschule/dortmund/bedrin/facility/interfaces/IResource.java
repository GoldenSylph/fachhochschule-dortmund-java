package de.fachhochschule.dortmund.bedrin.facility.interfaces;

public interface IResource<T> {
	public T getData();
	public void setData(T newData);
}
