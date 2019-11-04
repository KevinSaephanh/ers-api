package dao;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.Reimbursement;
import utils.ConnectionUtil;
// Use pagination when getting ALL reimbursements (because the dataset may be too large)
// SELECT * FROM ers_reimbursements LIMIT 20 OFFSET (page_num taken from parameter * LIMIT SIZE)
// When user selects next on some table/list, a query will be sent to the server to query another
// paginated data set (i.e. the next 20 reimbursement tickets)
public class ReimbursementDao {
	public List<Reimbursement> getUserReimbs(int id) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements WHERE reimb_author = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			
			// Extract all user reimbursements and add to list
			List<Reimbursement> reimbs = new ArrayList<>();
			while (rs.next()) {
				Reimbursement reimb = extractReimb(rs);
				reimbs.add(reimb);
			}
			return reimbs;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public List<Reimbursement> getPendingReimbs() {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements WHERE reimb_status_id = 1";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			
			// Extract all reimbursements and add to list
			List<Reimbursement> reimbs = new ArrayList<>();
			while (rs.next()) {
				Reimbursement reimb = extractReimb(rs);
				reimbs.add(reimb);
			}
			return reimbs;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public List<Reimbursement> getApprovedReimbs() {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements WHERE reimb_status_id = 2";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			
			// Extract all reimbursements and add to list
			List<Reimbursement> reimbs = new ArrayList<>();
			while (rs.next()) {
				Reimbursement reimb = extractReimb(rs);
				reimbs.add(reimb);
			}
			return reimbs;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public List<Reimbursement> getDeniedReimbs() {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements WHERE reimb_status_id = 3";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			
			// Extract all reimbursements and add to list
			List<Reimbursement> reimbs = new ArrayList<>();
			while (rs.next()) {
				Reimbursement reimb = extractReimb(rs);
				reimbs.add(reimb);
			}
			return reimbs;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public List<Reimbursement> getAllReimbs() {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			// Extract all reimbursements and add to list
			List<Reimbursement> reimbs = new ArrayList<>();
			while (rs.next()) {
				Reimbursement reimbursement = extractReimb(rs);
				reimbs.add(reimbursement);
			}
			return reimbs;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Reimbursement getReimb(int id) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT * FROM ers_reimbursements WHERE reimb_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			
			// Check if reimbursement was found
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Reimbursement reimb = extractReimb(rs);
				return reimb;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public int createReimb(Reimbursement reimb) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "INSERT INTO ers_reimbursements"
					+ "(reimb_amount, reimb_submitted, reimb_description, reimb_receipt, reimb_author, reimb_status_id, reimb_type_id)"
					+ " VALUES(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setBigDecimal(1, reimb.getAmount());
			ps.setObject(2, LocalDateTime.now());
			ps.setString(3, reimb.getDescription());
			ps.setObject(4, reimb.getReceipt());
			ps.setInt(5, reimb.getAuthorId()); 
			ps.setInt(6, 1);
			ps.setInt(7, reimb.getReimbTypeId());

			int createCount = ps.executeUpdate();
			return createCount;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public Reimbursement updateReimb(Reimbursement reimb, int id) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "UPDATE ers_reimbursements "
					+ "SET reimb_resolved = ?, reimb_resolver = ?, reimb_status_id = ? "
					+ "WHERE reimb_id = ? RETURNING reimb_resolved, reimb_resolver, reimb_status_id";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setObject(1, LocalDateTime.now());
			ps.setInt(2, reimb.getResolverId());
			ps.setInt(3, reimb.getReimbStatusId());
			ps.setInt(4, id);
			
			// Check if reimbursement was updated
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				LocalDateTime resolved = rs.getTimestamp("reimb_resolved").toLocalDateTime();
				int resolverId = rs.getInt("reimb_resolver");
				int statusId = rs.getInt("reimb_status_id");
				
				reimb.setResolved(resolved);
				reimb.setResolverId(resolverId);
				reimb.setReimbStatusId(statusId);
				return reimb;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public int deleteReimb(int id) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "DELETE FROM ers_reimbursements WHERE reimb_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			int deleteCount = ps.executeUpdate();
			return deleteCount;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	public Reimbursement extractReimb(ResultSet rs) throws SQLException {
		int id = rs.getInt("reimb_id");
		BigDecimal amount = rs.getBigDecimal("reimb_amount");
		LocalDateTime submitted = rs.getTimestamp("reimb_submitted").toLocalDateTime();
		Timestamp resolved = rs.getTimestamp("reimb_resolved");
		String description = rs.getString("reimb_description");
		Blob receipt = (Blob) rs.getObject("reimb_receipt");
		int authorId = rs.getInt("reimb_author");
		int resolverId = rs.getInt("reimb_resolver");
		int statusId = rs.getInt("reimb_status_id");
		int reimbTypeId = rs.getInt("reimb_type_id");
		
		// If reimbursement was already resolved (not null), convert it to LocalDateTime
		LocalDateTime resolvedLDT = null;
		if (resolved != null)
			resolvedLDT = resolved.toLocalDateTime();

		Reimbursement reimb = new Reimbursement(id, amount, submitted, resolvedLDT, description, receipt, authorId,
				resolverId, reimbTypeId, statusId);
		return reimb;
	}
}
