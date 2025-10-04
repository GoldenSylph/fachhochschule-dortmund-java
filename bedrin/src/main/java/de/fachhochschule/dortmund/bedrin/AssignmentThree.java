package de.fachhochschule.dortmund.bedrin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import de.fachhochschule.dortmund.bedrin.facility.AGV;
import de.fachhochschule.dortmund.bedrin.facility.IndustrialProcess;
import de.fachhochschule.dortmund.bedrin.facility.interfaces.ICPU;
import de.fachhochschule.dortmund.bedrin.facility.utils.AGVPrograms;
import de.fachhochschule.dortmund.bedrin.inheritance.Resource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_operations.TransportOperation;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.SoftwareResource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.HardwareResource;

public class AssignmentThree {
	private static String getIdAndBatteryLoadOf(ICPU<InputStream, OutputStream> cpu) {
		cpu.executeProgram(AGVPrograms.getRequestIdAndBatteryLoadProgram());
		byte[] requestInfoProgramRawOutput = ((ByteArrayOutputStream) cpu.getOutput()).toByteArray();
		ByteBuffer requestInfoProgramOutputBuffer = ByteBuffer.allocate(requestInfoProgramRawOutput.length);
		requestInfoProgramOutputBuffer.put(requestInfoProgramRawOutput);
		requestInfoProgramOutputBuffer = requestInfoProgramOutputBuffer.flip();

		String agvId = Integer.valueOf(requestInfoProgramOutputBuffer.get(0)).toString();
		double agvBatteryLoad = requestInfoProgramOutputBuffer.slice(1, 8).asDoubleBuffer().get();
		return "ID: " + agvId + ", battery load - " + agvBatteryLoad + "%";
	}

	public static void main(String[] args) {
		ICPU<InputStream, OutputStream> agv1 = new AGV("0");
		SoftwareResource agv1InitProgram = new SoftwareResource(agv1, AGVPrograms.getInitializeProgram1(true));
		ICPU<InputStream, OutputStream> agv2 = new AGV("1");
		SoftwareResource agv2InitProgram = new SoftwareResource(agv2, AGVPrograms.getInitializeProgram2(true));

		ICPU<InputStream, OutputStream> agv3 = new AGV("2");
		SoftwareResource agv3InitProgram = new SoftwareResource(agv3, AGVPrograms.getInitializeProgram3(true));
		ICPU<InputStream, OutputStream> agv4 = new AGV("3");
		SoftwareResource agv4InitProgram = new SoftwareResource(agv4, AGVPrograms.getInitializeProgram4(true));

		TransportOperation shipSoftwareOp = new TransportOperation("op0", "Zero operation");
		shipSoftwareOp.addResource(agv1InitProgram);
		shipSoftwareOp.addResource(agv2InitProgram);
		shipSoftwareOp.addResource(agv3InitProgram);
		shipSoftwareOp.addResource(agv4InitProgram);

		TransportOperation op1 = new TransportOperation("op1", "First operation");
		op1.addResource((HardwareResource) agv1);
		op1.addResource((HardwareResource) agv2);

		TransportOperation op2 = new TransportOperation("op2", "Second operation");
		op2.addResource((HardwareResource) agv3);
		op2.addResource((HardwareResource) agv4);

		IndustrialProcess initProcess = new IndustrialProcess("initProc", List.of(shipSoftwareOp));
		initProcess.processResources().stream().forEach(future -> {
			try {
				SoftwareResource soft = (SoftwareResource) future.get();
				System.out.println("Resource (Software for Hardware ID: "
						+ getIdAndBatteryLoadOf(soft.getHardwareResource()) + ") has been initialized.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		IndustrialProcess process = new IndustrialProcess("proc1", Arrays.asList(op1, op2));

		process.processResources().stream().forEach(future -> {
			try {
				Resource resource = future.get();
				if (resource != null) {
					@SuppressWarnings("unchecked")
					String cpuInfo = getIdAndBatteryLoadOf((ICPU<InputStream, OutputStream>) resource);
					System.out.println("Resource (" + cpuInfo + "): has been waited for and processed.");

				} else {
					System.err.println("Resource processing failed or returned null.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
