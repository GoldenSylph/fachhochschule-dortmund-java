package de.fachhochschule.dortmund.bedrin.facility.interfaces;

public interface IResource<I, O> {
	public I getData();
	public void setData(O newData);
}
