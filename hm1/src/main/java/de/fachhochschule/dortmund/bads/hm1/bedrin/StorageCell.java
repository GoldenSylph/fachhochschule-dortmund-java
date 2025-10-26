package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.concurrent.Callable;

public class StorageCell implements Callable<StorageCell> {
	private Storage parentStorage;
	// POJO of a storage cell
	// has 3 dimensions: length, width, height
	// belongs to 1 storage
	// can be empty or filled
	// have a type (refrigerated, bulk storage, ambient)
	
	@Override
	public StorageCell call() throws Exception {
		return null;
	}
	
}
