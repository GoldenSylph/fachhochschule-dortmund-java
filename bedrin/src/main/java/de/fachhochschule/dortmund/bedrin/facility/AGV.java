package de.fachhochschule.dortmund.bedrin.facility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.sql.Time;
import java.util.ArrayDeque;
import java.util.Deque;

import de.fachhochschule.dortmund.bedrin.facility.interfaces.ICPU;
import de.fachhochschule.dortmund.bedrin.inheritance.Resource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.HardwareResource;

public class AGV extends HardwareResource implements ICPU<InputStream, OutputStream> {
	public static final RuntimeException SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED = new RuntimeException("Syntax Error: always start program with 0x00 byte code");
	public static final RuntimeException SYNTAX_EXCEPTION_PROGRAM_ALREADY_RUNNING = new RuntimeException("Syntax Error: program already running, cannot start it again");
	public static final RuntimeException SYNTAX_EXCEPTION_ALREADY_PRINTING_OUTPUT_BUFFER = new RuntimeException("Syntax Error: already printing output buffer, cannot start it again");
	public static final RuntimeException SYNTAX_EXCEPTION_NOT_PRINTING_OUTPUT_BUFFER = new RuntimeException("Syntax Error: not printing output buffer, cannot stop it");
	
	private String id;
	private double batteryLoad;
	private double batteryConsumptionPerMinute;
	private Time lastCharged;
	private float maxSpeedPerMinute;
	private float actSpeedPerMinute;
	private boolean printingOutputBuffer;
	private boolean programRunning;
	private OutputStream outputBuffer;
	private InputStream cachedProgram;
	
	private final int[] position = new int[2];

	public AGV(String newId) {
		super(1d, "Automated Guided Vehicle");
		this.id = newId;
		this.outputBuffer = new ByteArrayOutputStream();
	}

	@Override
	public OutputStream getOutput() {
		ByteArrayOutputStream distinctOutput = new ByteArrayOutputStream();
		try {
			distinctOutput.write(((ByteArrayOutputStream) this.outputBuffer).toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				this.outputBuffer.flush();
				this.outputBuffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return distinctOutput;
	}

	/*
	 * Just a simple Stack Machine:
	 * 
	 * --- Memory markup:
	 * double - 8 bytes
	 * float - 4 bytes
	 * String (ID) - 1 byte (0 - 255)
	 * Time - 8 bytes (UNIX timestamp)
	 * int[2] (position) - 4 bytes * 2 axis
	 * 
	 * --- Special byte codes
	 * 0x00 - Start program or skip tick
	 * 0xFF - End program
	 * 0x01 - get resource ID
	 * 0x97 - start printing output buffer
	 * 0x98 - stop printing output buffer
	 * --- Getters byte codes (even ones)
	 * 0x02 - get battery load, no args
	 * 0x04 - get battery consumption per minute, no args
	 * 0x06 - get time last charged, no args
	 * 0x08 - get max speed per minute, no args
	 * 0x10 - get act speed per minute, no args
	 * 0x12 - get position pair, no args
	 * 
	 * --- Setters byte codes (odd ones)
	 * 0x03 - set battery load, arguments: 8 bytes
	 * 0x05 - set battery consumption per minute, arguments: 8 bytes
	 * 0x07 - set time last charged, arguments: 8 bytes
	 * 0x09 - set max speed per minute, arguments: 4 bytes
	 * 0x11 - set act speed per minute, arguments: 4 bytes
	 * 0x13 - set position pair, arguments: 4 + 4 bytes
	 * 
	 */
	
	@Override
	public void executeProgram(InputStream newData) {
		// just wanted to make a simple stack machine
		Deque<Integer> memory = new ArrayDeque<>();

		// initialize memory of the stack machine
		try (newData) {
			int nextByte = newData.read();
			while (nextByte != -1) {
				memory.addLast(nextByte);
				nextByte = newData.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// the main cycle of the Stack Machine
		main:
		do {
			int byteCode = memory.pollFirst();
			switch (byteCode) {
				case 0x00:
					if (this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_ALREADY_RUNNING;
					}
					this.programRunning = true;
					continue main;
				case 0xFF:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					this.programRunning = false;
					break main;
				case 0x01:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						this.outputBuffer.write(Byte.parseByte(id));
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): ID requested, sent " + id);
					break;
				case 0x97:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					if (this.printingOutputBuffer) {
						throw SYNTAX_EXCEPTION_ALREADY_PRINTING_OUTPUT_BUFFER;
					}
					this.printingOutputBuffer = true;
					System.out.println("AGV (ID: " + id + "): started printing output buffer");
					continue main;
				case 0x98:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					if (!this.printingOutputBuffer) {
						throw SYNTAX_EXCEPTION_NOT_PRINTING_OUTPUT_BUFFER;
					}
					this.printingOutputBuffer = false;
					System.out.println("AGV (ID: " + id + "): stopped printing output buffer");
					continue main;
				case 0x02:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewBatteryLoad = ByteBuffer.allocate(Double.BYTES);
						bufferedViewBatteryLoad.putDouble(getBatteryLoad());
						this.outputBuffer.write(bufferedViewBatteryLoad.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): battery load requested, sent " + getBatteryLoad() + "%");
					break;
				case 0x04:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewBatteryConsumptionPerMinute = ByteBuffer.allocate(Double.BYTES);
						bufferedViewBatteryConsumptionPerMinute.putDouble(getBatteryConsumptionPerMinute());
						this.outputBuffer.write(bufferedViewBatteryConsumptionPerMinute.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): battery consumption per minute requested, sent " + getBatteryConsumptionPerMinute() + "%");
					break;
				case 0x06:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewLastTimeCharged = ByteBuffer.allocate(Long.BYTES);
						bufferedViewLastTimeCharged.putLong(getLastCharged().getTime());
						this.outputBuffer.write(bufferedViewLastTimeCharged.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): time last charged requested, sent " + getLastCharged());
					break;
				case 0x08:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewMaxSpeedPerMinute = ByteBuffer.allocate(Double.BYTES);
						bufferedViewMaxSpeedPerMinute.putDouble(getMaxSpeedPerMinute());
						this.outputBuffer.write(bufferedViewMaxSpeedPerMinute.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): max speed per minute requested, sent " + getMaxSpeedPerMinute() + " m/min");
					break;
				case 0x10:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewActSpeedPerMinute = ByteBuffer.allocate(Double.BYTES);
						bufferedViewActSpeedPerMinute.putDouble(getActSpeedPerMinute());
						this.outputBuffer.write(bufferedViewActSpeedPerMinute.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): actual speed per minute requested, sent " + getActSpeedPerMinute() + " m/min");
					break;
				case 0x12:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					try {
						ByteBuffer bufferedViewPosition = ByteBuffer.allocate(2);
						bufferedViewPosition.asIntBuffer().put(getPosition());
						this.outputBuffer.write(bufferedViewPosition.array());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): position requested, sent [" + getPosition()[0] + ", " + getPosition()[1] + "]");
					break;
				case 0x03:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newBatteryLoadValue = popNBytesFromMemory(memory, Double.BYTES);
					ByteBuffer bufferedViewNewBatteryLoadValue = ByteBuffer.allocate(Double.BYTES);
					this.batteryLoad = bufferedViewNewBatteryLoadValue
							.put(newBatteryLoadValue)
							.flip()
							.asDoubleBuffer()
							.get();
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): battery load set to " + this.batteryLoad + "%");
					break;
				case 0x05:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newBatteryConsumptionPerMinute = popNBytesFromMemory(memory, Double.BYTES);
					ByteBuffer bufferedViewNewBatteryConsumptionPerMinute = ByteBuffer.allocate(Double.BYTES);
					this.batteryConsumptionPerMinute = bufferedViewNewBatteryConsumptionPerMinute
							.put(newBatteryConsumptionPerMinute)
							.flip()
							.asDoubleBuffer()
							.get();
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): battery consumption per minute set to " + this.batteryConsumptionPerMinute + "%");
					break;
				case 0x07:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newTimeLastCharged = popNBytesFromMemory(memory, Long.BYTES);
					ByteBuffer bufferedViewNewTimeLastCharged = ByteBuffer.allocate(Long.BYTES);
					this.lastCharged = new Time(bufferedViewNewTimeLastCharged
							.put(newTimeLastCharged)
							.flip()
							.asLongBuffer()
							.get()
						);
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): time last charged set to " + this.lastCharged);
					break;
				case 0x09:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newMaxSpeedPerMinute = popNBytesFromMemory(memory, Float.BYTES);
					ByteBuffer bufferedViewNewMaxSpeedPerMinute = ByteBuffer.allocate(Float.BYTES);
					this.maxSpeedPerMinute = bufferedViewNewMaxSpeedPerMinute
							.put(newMaxSpeedPerMinute)
							.flip()
							.asFloatBuffer()
							.get();
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): max speed per minute set to " + this.maxSpeedPerMinute + " m/min");
					break;
				case 0x11:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newActSpeedPerMinute = popNBytesFromMemory(memory, Float.BYTES);
					ByteBuffer bufferedViewNewActSpeedPerMinute = ByteBuffer.allocate(Float.BYTES);
					this.actSpeedPerMinute = bufferedViewNewActSpeedPerMinute
							.put(newActSpeedPerMinute)
							.flip()
							.asFloatBuffer()
							.get();
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): actual speed per minute set to " + this.actSpeedPerMinute + " m/min");
					break;
				case 0x13:
					if (!this.programRunning) {
						throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
					}
					byte[] newPosition = popNBytesFromMemory(memory, Integer.BYTES * 2);
					ByteBuffer bufferedViewNewPosition = ByteBuffer.allocate(Integer.BYTES * 2);
					IntBuffer intBufferedViewNewPosition = bufferedViewNewPosition.put(newPosition).flip().asIntBuffer();
					this.position[0] = intBufferedViewNewPosition.get();
					this.position[1] = intBufferedViewNewPosition.get();
					if (this.printingOutputBuffer) System.out.println("AGV (ID: " + id + "): position set to [" + this.position[0] + ", " + this.position[1] + "]");
					break;
				default:
					throw new RuntimeException("Syntax Eror: unknown byte code " + byteCode);
			}
		} while (memory.size() > 0);
		
		// just so you're wondering, it's still not an AI) I could record a live-coding video if I must of course)
	}
	
	private static byte[] popNBytesFromMemory(Deque<Integer> memory, int bytesCount) {
		byte[] result = new byte[bytesCount];
		for (int i = 0; i < bytesCount; i++) {
			result[i] = memory.pollFirst().byteValue();
		}
		return result;
	}

	@Override
	public Resource call() {
		executeProgram(this.cachedProgram);
		return this;
	}

	@Override
	public void cacheProgram(InputStream program) {
		this.cachedProgram = program;		
	}
	
	protected String getId() {
		return id;
	}

	protected double getBatteryLoad() {
		return batteryLoad;
	}

	protected double getBatteryConsumptionPerMinute() {
		return batteryConsumptionPerMinute;
	}

	protected Time getLastCharged() {
		return lastCharged;
	}

	protected float getMaxSpeedPerMinute() {
		return maxSpeedPerMinute;
	}

	protected float getActSpeedPerMinute() {
		return actSpeedPerMinute;
	}

	protected int[] getPosition() {
		return position;
	}
}
