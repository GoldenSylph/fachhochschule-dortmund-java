package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public final class AGVPrograms {
	public static final int START_AND_STOP_BYTECODES_SIZE = 2;

	/**
	 * Start, enqueue 3 waypoints (0,0)->(1,0)->(1,1), take id=1, release id=1, end.
	 */
	public static ByteArrayInputStream startMove3Take1Release1() {
		int waypoints = 3;
		int size = START_AND_STOP_BYTECODES_SIZE + waypoints * (1 + 2 * Integer.BYTES) + 2 /* take */ + 2 /* release */;
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.put((byte) 0x00); // start
		// waypoints: (0,0)->(1,0)->(1,1)
		buf.put((byte) 0x14).putInt(0).putInt(0);
		buf.put((byte) 0x14).putInt(1).putInt(0);
		buf.put((byte) 0x14).putInt(1).putInt(1);
		// take id=1, release id=1
		buf.put((byte) 0x20).put((byte) 0x01);
		buf.put((byte) 0x21).put((byte) 0x01);
		buf.put((byte) 0xFF); // end
		return new ByteArrayInputStream(buf.array());
	}

	/**
	 * Start, enqueue 2 waypoints (10,5)->(12,7), take id=2, end.
	 */
	public static ByteArrayInputStream startMove2Take2() {
		int waypoints = 2;
		int size = START_AND_STOP_BYTECODES_SIZE + waypoints * (1 + 2 * Integer.BYTES) + 2 /* take */;
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.put((byte) 0x00);
		// waypoints: (10,5)->(12,7)
		buf.put((byte) 0x14).putInt(10).putInt(5);
		buf.put((byte) 0x14).putInt(12).putInt(7);
		buf.put((byte) 0x20).put((byte) 0x02); // take id=2
		buf.put((byte) 0xFF);
		return new ByteArrayInputStream(buf.array());
	}

	/**
	 * Start, enqueue 4 waypoints (1,1)->(2,1)->(2,2)->(3,2), release id=3, end.
	 */
	public static ByteArrayInputStream startMove4Release3() {
		int waypoints = 4;
		int size = START_AND_STOP_BYTECODES_SIZE + waypoints * (1 + 2 * Integer.BYTES) + 2 /* release */;
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.put((byte) 0x00);
		// waypoints: (1,1)->(2,1)->(2,2)->(3,2)
		buf.put((byte) 0x14).putInt(1).putInt(1);
		buf.put((byte) 0x14).putInt(2).putInt(1);
		buf.put((byte) 0x14).putInt(2).putInt(2);
		buf.put((byte) 0x14).putInt(3).putInt(2);
		buf.put((byte) 0x21).put((byte) 0x03); // release id=3
		buf.put((byte) 0xFF);
		return new ByteArrayInputStream(buf.array());
	}

	/**
	 * Start, enqueue 2 waypoints (-5,100)->(0,0), take id=4, release id=4, end.
	 */
	public static ByteArrayInputStream startMove2EdgeTake4Release4() {
		int waypoints = 2;
		int size = START_AND_STOP_BYTECODES_SIZE + waypoints * (1 + 2 * Integer.BYTES) + 2 /* take */ + 2 /* release */;
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.put((byte) 0x00);
		// waypoints: (-5,100)->(0,0)
		buf.put((byte) 0x14).putInt(-5).putInt(100);
		buf.put((byte) 0x14).putInt(0).putInt(0);
		buf.put((byte) 0x20).put((byte) 0x04); // take id=4
		buf.put((byte) 0x21).put((byte) 0x04); // release id=4
		buf.put((byte) 0xFF);
		return new ByteArrayInputStream(buf.array());
	}

	/**
	 * Minimal start and stop.
	 */
	public static ByteArrayInputStream startAndStop() {
		ByteBuffer buf = ByteBuffer.allocate(START_AND_STOP_BYTECODES_SIZE);
		buf.put((byte) 0x00);
		buf.put((byte) 0xFF);
		return new ByteArrayInputStream(buf.array());
	}
}