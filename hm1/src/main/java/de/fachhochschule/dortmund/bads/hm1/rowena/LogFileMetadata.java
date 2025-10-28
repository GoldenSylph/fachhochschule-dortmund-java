package de.fachhochschule.dortmund.bads.hm1.rowena;

import java.time.LocalDate;

//Metadata for log files.
public class LogFileMetadata {
	private final String filePath;
	private final String equipmentType;
	private final String equipmentId;
	private final LocalDate date;
	private final long createdAt;
	private long fileSize;

	public LogFileMetadata(String filePath, String equipmentType, String equipmentId, LocalDate date) {
		this.filePath = filePath;
		this.equipmentType = equipmentType;
		this.equipmentId = equipmentId;
		this.date = date;
		this.createdAt = System.currentTimeMillis();
		this.fileSize = 0;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getEquipmentType() {
		return equipmentType;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public LocalDate getDate() {
		return date;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public String toString() {
		return "LogFileMetadata{" +
				"filePath='" + filePath + '\'' +
				", equipmentType='" + equipmentType + '\'' +
				", equipmentId='" + equipmentId + '\'' +
				", date=" + date +
				", createdAt=" + createdAt +
				", fileSize=" + fileSize +
				'}';
	}
}
