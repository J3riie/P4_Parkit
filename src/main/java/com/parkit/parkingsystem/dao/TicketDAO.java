package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

	private static final String ERROR_FETCHING_NEXT_AVAILABLE_SLOT = "Error fetching next available slot";

	private DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public DataBaseConfig getDataBaseConfig() {
		return dataBaseConfig;
	}

	public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
		this.dataBaseConfig = dataBaseConfig;
	}

	public boolean saveTicket(Ticket ticket) throws SQLException {
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);) {
			ps.setInt(1, ticket.getParkingSpot().getId());
			ps.setString(2, ticket.getVehicleRegNumber());
			ps.setDouble(3, ticket.getPrice());
			ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
			ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
			ps.setBoolean(6, ticket.isRecurringUser());
			return ps.execute();
		} catch (final Exception ex) {
			throw new SQLException(ERROR_FETCHING_NEXT_AVAILABLE_SLOT, ex);
		}
	}

	public Ticket getTicket(String vehicleRegNumber) throws SQLException {
		Ticket ticket = null;
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);) {
			// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
			ps.setString(1, vehicleRegNumber);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticket = new Ticket();
				final ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(7)),
						false);
				ticket.setParkingSpot(parkingSpot);
				ticket.setId(rs.getInt(2));
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(rs.getDouble(3));
				ticket.setInTime(rs.getTimestamp(4));
				ticket.setOutTime(rs.getTimestamp(5));
				ticket.setRecurringUser(rs.getBoolean(6));
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
			return ticket;
		} catch (final Exception ex) {
			throw new SQLException(ERROR_FETCHING_NEXT_AVAILABLE_SLOT, ex);
		}
	}

	public boolean updateTicket(Ticket ticket) throws SQLException {
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET)) {
			ps.setDouble(1, ticket.getPrice());
			ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
			ps.setInt(3, ticket.getId());
			ps.execute();
			return true;
		} catch (final Exception ex) {
			throw new SQLException("Error saving ticket info", ex);
		}
	}

	public boolean findTicketByVehicleRegNumberAndOutTimeIsNotNull(String vehicleRegNumber) throws SQLException {
		boolean ticketFound = false;
		try (Connection con = dataBaseConfig.getConnection();
				final PreparedStatement ps = con.prepareStatement(DBConstants.GET_LAST_TICKET);) {
			// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
			ps.setString(1, vehicleRegNumber);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketFound = true;
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
			return ticketFound;
		} catch (final Exception ex) {
			throw new SQLException(ERROR_FETCHING_NEXT_AVAILABLE_SLOT, ex);
		}
	}
}
