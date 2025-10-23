package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public final class AGVPrograms {
	public static final int START_AND_STOP_BYTECODES_SIZE = 2;

	public static ByteArrayInputStream getInitializeProgram1(boolean debugMode) {
		final int AGV_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 8 + 2 * Double.BYTES + Long.BYTES
				+ 2 * Float.BYTES + 2 * Integer.BYTES;
		final ByteBuffer bufferedInputForAgv = ByteBuffer.allocate(AGV_PROGRAM_SIZE_BYTES);

		// start of the program for AGV
		bufferedInputForAgv.put((byte) 0x00);

		if (debugMode) {
			// starting printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x97);
		}

		// firstly - battery load initialization
		bufferedInputForAgv.put((byte) 0x03);
		bufferedInputForAgv.putDouble(100d);

		// secondly - battery consumption initialization
		bufferedInputForAgv.put((byte) 0x05);
		bufferedInputForAgv.putDouble(5d);

		// thirdly - charging time initialization
		bufferedInputForAgv.put((byte) 0x07);
		bufferedInputForAgv.putLong(new java.util.Date().getTime());

		// fourthly - max speed initialization
		bufferedInputForAgv.put((byte) 0x09);
		bufferedInputForAgv.putFloat(10f);

		// fifthly - act speed initialization
		bufferedInputForAgv.put((byte) 0x11);
		bufferedInputForAgv.putFloat(1f);

		// sixthly - position initialization
		bufferedInputForAgv.put((byte) 0x13);
		bufferedInputForAgv.putInt(8);
		bufferedInputForAgv.putInt(255);

		if (debugMode) {
			// stopping printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x98);
		}

		// end of the program for AGV
		bufferedInputForAgv.put((byte) 0xFF);
		return new ByteArrayInputStream(bufferedInputForAgv.array());
	}

	public static ByteArrayInputStream getInitializeProgram2(boolean debugMode) {
		final int AGV_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 8 + 2 * Double.BYTES + Long.BYTES
				+ 2 * Float.BYTES + 2 * Integer.BYTES;
		final ByteBuffer bufferedInputForAgv = ByteBuffer.allocate(AGV_PROGRAM_SIZE_BYTES);

		// start of the program for AGV
		bufferedInputForAgv.put((byte) 0x00);

		if (debugMode) {
			// starting printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x97);
		}
		// firstly - battery load initialization
		bufferedInputForAgv.put((byte) 0x03);
		bufferedInputForAgv.putDouble(70d);

		// secondly - battery consumption initialization
		bufferedInputForAgv.put((byte) 0x05);
		bufferedInputForAgv.putDouble(10d);

		// thirdly - charging time initialization
		bufferedInputForAgv.put((byte) 0x07);
		bufferedInputForAgv.putLong(new java.util.Date().getTime());

		// fourthly - max speed initialization
		bufferedInputForAgv.put((byte) 0x09);
		bufferedInputForAgv.putFloat(20f);

		// fifthly - act speed initialization
		bufferedInputForAgv.put((byte) 0x11);
		bufferedInputForAgv.putFloat(10f);

		// sixthly - position initialization
		bufferedInputForAgv.put((byte) 0x13);
		bufferedInputForAgv.putInt(230);
		bufferedInputForAgv.putInt(3);

		if (debugMode) {
			// stopping printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x98);
		}

		// end of the program for AGV
		bufferedInputForAgv.put((byte) 0xFF);
		return new ByteArrayInputStream(bufferedInputForAgv.array());
	}

	public static ByteArrayInputStream getInitializeProgram3(boolean debugMode) {
		final int AGV_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 8 + 2 * Double.BYTES + Long.BYTES
				+ 2 * Float.BYTES + 2 * Integer.BYTES;
		final ByteBuffer bufferedInputForAgv = ByteBuffer.allocate(AGV_PROGRAM_SIZE_BYTES);

		// start of the program for AGV
		bufferedInputForAgv.put((byte) 0x00);

		if (debugMode) {
			// starting printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x97);
		}

		// firstly - battery load initialization
		bufferedInputForAgv.put((byte) 0x03);
		bufferedInputForAgv.putDouble(10d);

		// secondly - battery consumption initialization
		bufferedInputForAgv.put((byte) 0x05);
		bufferedInputForAgv.putDouble(1.2d);

		// thirdly - charging time initialization
		bufferedInputForAgv.put((byte) 0x07);
		bufferedInputForAgv.putLong(new java.util.Date().getTime());

		// fourthly - max speed initialization
		bufferedInputForAgv.put((byte) 0x09);
		bufferedInputForAgv.putFloat(60f);

		// fifthly - act speed initialization
		bufferedInputForAgv.put((byte) 0x11);
		bufferedInputForAgv.putFloat(2f);

		// sixthly - position initialization
		bufferedInputForAgv.put((byte) 0x13);
		bufferedInputForAgv.putInt(1);
		bufferedInputForAgv.putInt(1);

		if (debugMode) {
			// stopping printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x98);
		}

		// end of the program for AGV
		bufferedInputForAgv.put((byte) 0xFF);
		return new ByteArrayInputStream(bufferedInputForAgv.array());
	}

	public static ByteArrayInputStream getInitializeProgram4(boolean debugMode) {
		final int AGV_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 8 + 2 * Double.BYTES + Long.BYTES
				+ 2 * Float.BYTES + 2 * Integer.BYTES;
		final ByteBuffer bufferedInputForAgv = ByteBuffer.allocate(AGV_PROGRAM_SIZE_BYTES);

		// start of the program for AGV
		bufferedInputForAgv.put((byte) 0x00);

		if (debugMode) {
			// starting printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x97);
		}

		// firstly - battery load initialization
		bufferedInputForAgv.put((byte) 0x03);
		bufferedInputForAgv.putDouble(99d);

		// secondly - battery consumption initialization
		bufferedInputForAgv.put((byte) 0x05);
		bufferedInputForAgv.putDouble(33d);

		// thirdly - charging time initialization
		bufferedInputForAgv.put((byte) 0x07);
		bufferedInputForAgv.putLong(new java.util.Date().getTime());

		// fourthly - max speed initialization
		bufferedInputForAgv.put((byte) 0x09);
		bufferedInputForAgv.putFloat(40f);

		// fifthly - act speed initialization
		bufferedInputForAgv.put((byte) 0x11);
		bufferedInputForAgv.putFloat(3f);

		// sixthly - position initialization
		bufferedInputForAgv.put((byte) 0x13);
		bufferedInputForAgv.putInt(-100);
		bufferedInputForAgv.putInt(100);

		if (debugMode) {
			// stopping printing output buffer on the fly
			bufferedInputForAgv.put((byte) 0x98);
		}
		// end of the program for AGV
		bufferedInputForAgv.put((byte) 0xFF);
		return new ByteArrayInputStream(bufferedInputForAgv.array());
	}

	public static ByteArrayInputStream getRequestIdAndBatteryLoadProgram() {
		final int REQUEST_ID_AND_BATTERY_LOAD_PROGRAM_SIZE_BYTES = START_AND_STOP_BYTECODES_SIZE + 2;
		final ByteBuffer requestInfoProgramInput = ByteBuffer.allocate(REQUEST_ID_AND_BATTERY_LOAD_PROGRAM_SIZE_BYTES);
		requestInfoProgramInput.put((byte) 0x00); // start program
		requestInfoProgramInput.put((byte) 0x01); // request id byte code
		requestInfoProgramInput.put((byte) 0x02); // request battery load byte code
		requestInfoProgramInput.put((byte) 0xFF); // end program
		return new ByteArrayInputStream(requestInfoProgramInput.array());
	}
}
