package de.fachhochschule.dortmund.bedrin;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import de.fachhochschule.dortmund.bedrin.facility.AGV;
import de.fachhochschule.dortmund.bedrin.facility.IndustrialProcess;
import de.fachhochschule.dortmund.bedrin.facility.utils.AGVPrograms;
import de.fachhochschule.dortmund.bedrin.inheritance.new_operations.TransportOperation;

public class AssignmentTwo {

	// I got a bit bored and implemented AGVs as if they were real black box
	// hardware with it's own set of commands
	// and inputs and outputs. So I made as if an AGV had its own CPU based on basic
	// Stack Machine.
	// It may look like I've utilized some AI, but I've not.
	// I just have an experience over 3 years of studying and playing with Java Enterprise and
	// Android Development at my bachelor program at Kazan Federal University, 2
	// years of working with C# and 3 years with TypeScript and Solidity (Hardhat + Foundry + Solidity).
	public static void main(String[] args) {
		AGV agv1 = new AGV("0");
		agv1.executeProgram(AGVPrograms.getInitializeProgram1(false));
		AGV agv2 = new AGV("1");
		agv2.executeProgram(AGVPrograms.getInitializeProgram2(false));

		AGV agv3 = new AGV("2");
		agv3.executeProgram(AGVPrograms.getInitializeProgram3(false));
		
		AGV agv4 = new AGV("3");
		agv4.executeProgram(AGVPrograms.getInitializeProgram4(false));
		
		TransportOperation op1 = new TransportOperation("op1", "First operation");
		op1.addResource(agv1);
		op1.addResource(agv2);

		TransportOperation op2 = new TransportOperation("op2", "Second operation");
		op2.addResource(agv3);
		op2.addResource(agv4);

		// let's do some groupings
		IndustrialProcess ip1 = new IndustrialProcess("ip1", Arrays.asList(op1, op2));

		System.out.println("IP1 - process duration for operation: " + ip1.processDuration() + " seconds");

		// let's try and request id and battery load from a certain AGV
		agv1.executeProgram(AGVPrograms.getRequestIdAndBatteryLoadProgram());

		byte[] requestInfoProgramRawOutput = ((ByteArrayOutputStream) agv1.getOutput())
				.toByteArray();
		ByteBuffer requestInfoProgramOutputBuffer = ByteBuffer.allocate(requestInfoProgramRawOutput.length);
		requestInfoProgramOutputBuffer.put(requestInfoProgramRawOutput);
		requestInfoProgramOutputBuffer = requestInfoProgramOutputBuffer.flip();

		String agv1Id = Integer.valueOf(requestInfoProgramOutputBuffer.get(0)).toString();
		double agv1BatteryLoad = requestInfoProgramOutputBuffer.slice(1, 8).asDoubleBuffer().get();
		System.out.println("AGV (ID: " + agv1Id + "): battery load - " + agv1BatteryLoad + "%");
		
		agv2.executeProgram(AGVPrograms.getRequestIdAndBatteryLoadProgram());
		
		byte[] requestInfoProgramRawOutput2 = ((ByteArrayOutputStream) agv2.getOutput())
				.toByteArray();
		ByteBuffer requestInfoProgramOutputBuffer2 = ByteBuffer.allocate(requestInfoProgramRawOutput2.length);
		requestInfoProgramOutputBuffer2.put(requestInfoProgramRawOutput2);
		requestInfoProgramOutputBuffer2 = requestInfoProgramOutputBuffer2.flip();

		String agv2Id = Integer.valueOf(requestInfoProgramOutputBuffer2.get(0)).toString();
		double agv2BatteryLoad = requestInfoProgramOutputBuffer2.slice(1, 8).asDoubleBuffer().get();
		System.out.println("AGV (ID: " + agv2Id + "): battery load - " + agv2BatteryLoad + "%");
		
		// Every other field could be obtained through a similar matter. With it's own requestings byte code program.
	}

}
