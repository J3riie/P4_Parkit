package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.service.TechnicalException;

public class ParkingSpotDAO {
	private DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public DataBaseConfig getDataBaseConfig() {
		return dataBaseConfig;
	}

	public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
		this.dataBaseConfig = dataBaseConfig;
	}

	public int getNextAvailableSlot(ParkingType parkingType) throws TechnicalException {
		int result = -1;
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)) {
			ps.setString(1, parkingType.toString());
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
			return result;
		} catch (final Exception ex) {
			throw new TechnicalException("Error fetching next available slot", ex);
		}
	}

	public boolean updateParking(ParkingSpot parkingSpot) throws SQLException {
		// update the availability for that parking spot
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)) {
			ps.setBoolean(1, parkingSpot.isAvailable());
			ps.setInt(2, parkingSpot.getId());
			final int updateRowCount = ps.executeUpdate();
			dataBaseConfig.closePreparedStatement(ps);
			return (updateRowCount == 1);
		} catch (final Exception ex) {
			throw new SQLException("Error updating parking info", ex);
		}
	}

}
