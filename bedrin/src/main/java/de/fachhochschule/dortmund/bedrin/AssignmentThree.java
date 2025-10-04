package de.fachhochschule.dortmund.bedrin;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import de.fachhochschule.dortmund.bedrin.facility.AGV;
import de.fachhochschule.dortmund.bedrin.facility.IndustrialProcess;
import de.fachhochschule.dortmund.bedrin.facility.interfaces.ICPU;
import de.fachhochschule.dortmund.bedrin.facility.utils.AGVPrograms;
import de.fachhochschule.dortmund.bedrin.inheritance.new_operations.TransportOperation;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.HardwareResource;

public class AssignmentThree {
	public static void main(String[] args) {
		ICPU<InputStream, OutputStream> agv1 = new AGV("0");
		agv1.cacheProgram(AGVPrograms.getInitializeProgram1(true));
		ICPU<InputStream, OutputStream> agv2 = new AGV("1");
		agv2.cacheProgram(AGVPrograms.getInitializeProgram2(true));
		
		ICPU<InputStream, OutputStream> agv3 = new AGV("2");
		agv3.cacheProgram(AGVPrograms.getInitializeProgram3(true));
		ICPU<InputStream, OutputStream> agv4 = new AGV("3");
		agv4.cacheProgram(AGVPrograms.getInitializeProgram4(true));
		
		TransportOperation op1 = new TransportOperation("op1", "First operation");
		op1.addHardwareResource((HardwareResource) agv1);
		op1.addHardwareResource((HardwareResource) agv2);
		
		TransportOperation op2 = new TransportOperation("op2", "Second operation");
		op2.addHardwareResource((HardwareResource) agv3);
		op2.addHardwareResource((HardwareResource) agv4);
		
		IndustrialProcess process = new IndustrialProcess("proc1", Arrays.asList(op1, op2));
		process.processResources();
	}
}
