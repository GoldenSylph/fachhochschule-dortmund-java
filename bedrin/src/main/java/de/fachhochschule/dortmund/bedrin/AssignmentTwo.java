package de.fachhochschule.dortmund.bedrin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import de.fachhochschule.dortmund.bedrin.facility.AGV;
import de.fachhochschule.dortmund.bedrin.facility.IndustrialProcess;
import de.fachhochschule.dortmund.bedrin.facility.OperationWithAppendableResources;

public class AssignmentTwo {

	// I got a bit bored and implemented AGVs as if they were real black box hardware with it's own set of commands
	// and inputs and outputs. So I made as if an AGV had its own CPU based on basic Stack Machine. 
	// I may look like I've utilized some AI, but I've not. 
	// I'm just have an experience over 3 years of studying Java Enterprise and 
	// Android Development at my bachelor and 2 years of working with C#.
	public static void main(String[] args) {
		final int START_AND_STOP_BYTECODES_SIZE = 2;
		final int AGV1_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 6 + 2 * Double.BYTES + Long.BYTES + 2 * Float.BYTES + 2 * Integer.BYTES;
		ByteBuffer bufferedInputForAgv1 = ByteBuffer.allocate(AGV1_PROGRAM_SIZE_BYTES);

		// start of the program for AGV 1
		bufferedInputForAgv1.put((byte) 0x00);
		
		// firstly - battery load initialization
		bufferedInputForAgv1.put((byte) 0x03);
		bufferedInputForAgv1.putDouble(100d);
		
		// secondly - battery consumption initialization
		bufferedInputForAgv1.put((byte) 0x05);
		bufferedInputForAgv1.putDouble(5d);
		
		// thirdly - charging time initialization
		bufferedInputForAgv1.put((byte) 0x07);
		bufferedInputForAgv1.putLong(new java.util.Date().getTime());
		
		// fourthly - max speed initialization
		bufferedInputForAgv1.put((byte) 0x09);
		bufferedInputForAgv1.putFloat(10f);

		// fifthly - act speed initialization
		bufferedInputForAgv1.put((byte) 0x11);
		bufferedInputForAgv1.putFloat(1f);
		
		// sixthly - position initialization
		bufferedInputForAgv1.put((byte) 0x13);
		bufferedInputForAgv1.putInt(8);
		bufferedInputForAgv1.putInt(255);
		
		// end of the program for AGV 1
		bufferedInputForAgv1.put((byte) 0xFF);

		final int AGV2_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 6 + 2 * Double.BYTES + Long.BYTES + 2 * Float.BYTES + 2 * Integer.BYTES;
		ByteBuffer bufferedInputForAgv2 = ByteBuffer.allocate(AGV2_PROGRAM_SIZE_BYTES);

		// start of the program for AGV 2
		bufferedInputForAgv2.put((byte) 0x00);
		
		// firstly - battery load initialization
		bufferedInputForAgv2.put((byte) 0x03);
		bufferedInputForAgv2.putDouble(70d);
		
		// secondly - battery consumption initialization
		bufferedInputForAgv2.put((byte) 0x05);
		bufferedInputForAgv2.putDouble(10d);
		
		// thirdly - charging time initialization
		bufferedInputForAgv2.put((byte) 0x07);
		bufferedInputForAgv2.putLong(new java.util.Date().getTime());
		
		// fourthly - max speed initialization
		bufferedInputForAgv2.put((byte) 0x09);
		bufferedInputForAgv2.putFloat(20f);

		// fifthly - act speed initialization
		bufferedInputForAgv2.put((byte) 0x11);
		bufferedInputForAgv2.putFloat(10f);
		
		// sixthly - position initialization
		bufferedInputForAgv2.put((byte) 0x13);
		bufferedInputForAgv2.putInt(230);
		bufferedInputForAgv2.putInt(3);
		
		// end of the program for AGV 2
		bufferedInputForAgv2.put((byte) 0xFF);

		AGV agv1 = new AGV("0");
		AGV agv2 = new AGV("1");
		
		AGV agv3 = new AGV("2");
		AGV agv4 = new AGV("3");
		
		OperationWithAppendableResources op1 = new OperationWithAppendableResources("op1", "First operation");
		ByteArrayInputStream agv1Input = new ByteArrayInputStream(bufferedInputForAgv1.array());
		op1.appendNewResourceAndSetData(agv1, agv1Input);
		ByteArrayInputStream agv2Input = new ByteArrayInputStream(bufferedInputForAgv2.array());
		op1.appendNewResourceAndSetData(agv2, agv2Input);

		// copying AGVs programs of 1 and 2 into 3 and 4
		OperationWithAppendableResources op2 = new OperationWithAppendableResources("op1", "First operation");
		ByteArrayInputStream agv3Input = new ByteArrayInputStream(bufferedInputForAgv1.array());
		op2.appendNewResourceAndSetData(agv3, agv3Input);
		ByteArrayInputStream agv4Input = new ByteArrayInputStream(bufferedInputForAgv2.array());
		op2.appendNewResourceAndSetData(agv4, agv4Input);

		IndustrialProcess ip1 = new IndustrialProcess("ip1", Arrays.asList(op1, op2));
		
		System.out.println("IP1 - process duration for operation: " + ip1.processDuration(0));
		System.out.println("IP1 - process duration for operation: " + ip1.processDuration(1));
		
		// let's try and request id and battery load from a certain AGV
		final int REQUEST_ID_AND_BATTERY_LOAD_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 2;
		ByteBuffer requestInfoProgramInput = ByteBuffer.allocate(REQUEST_ID_AND_BATTERY_LOAD_PROGRAM_SIZE_BYTES);
		requestInfoProgramInput.put((byte) 0x00); // start program
		requestInfoProgramInput.put((byte) 0x01); // request id byte code
		requestInfoProgramInput.put((byte) 0x02); // request battery load byte code
		requestInfoProgramInput.put((byte) 0xFF); // end program
		ip1.processResources(0, 0).setData(new ByteArrayInputStream(requestInfoProgramInput.array()));
		
		byte[] requestInfoProgramRawOutput = ((ByteArrayOutputStream) ip1.processResources(0, 0).getData()).toByteArray();
		ByteBuffer requestInfoProgramOutputBuffer = ByteBuffer.allocate(requestInfoProgramRawOutput.length);
		requestInfoProgramOutputBuffer.put(requestInfoProgramRawOutput);
		requestInfoProgramOutputBuffer = requestInfoProgramOutputBuffer.flip();
		
		String agv1Id = Integer.valueOf(requestInfoProgramOutputBuffer.get(0)).toString();
		double agv1BatteryLoad = requestInfoProgramOutputBuffer.slice(1,  8).asDoubleBuffer().get();
		System.out.println("AGV (ID: " + agv1Id + "): battery load - " + agv1BatteryLoad + "%");
	}

}
	