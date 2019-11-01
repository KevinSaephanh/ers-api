package servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Reimbursement;
import services.ReimbursementService;

/*
 * Receipt is an image to be stored
 * Users can add a receipt to a reimbursement form then send the form request to the server
 * The server will take call the service and then dao to save to the db
 * The db will send data back to the dao then back to the service ... to the client
 * The image will then be sent to the server to be added to the database and the process repeats
 * until some json (image, success, etc.) is sent to the client
 * */
public class ReimbursementController extends Controller {
	private ReimbursementService rs = new ReimbursementService();
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void process(HttpServletRequest req, HttpServletResponse resp)
			throws JsonParseException, JsonMappingException, IOException {
		String url = req.getRequestURI();
		String[] parse = url.split("/");
		String reimbStatus = "";
		int reimbId = 0;
		int userId = 0;

		// Check for additional parameters in URI after 'reimbursements'
		if (parse.length > 3 && parse[3].matches(".*\\d.*"))
			reimbId = Integer.parseInt(parse[3]);
		else if (parse.length > 4 && parse[3].matches("user") && parse[4].matches(".*\\d.*"))
			userId = Integer.parseInt(parse[4].replaceAll("[\\D]", ""));
		else if (parse.length > 3 && parse[3].matches("[a-zA-Z]+"))
			reimbStatus = parse[3].toLowerCase();

		// Call associated handler using request method
		switch (req.getMethod()) {
		case "GET":
			if (reimbStatus.equals("pending"))
				handleGetPendingReimbs(req, resp);
			else if (reimbStatus.equals("approved"))
				handleGetApprovedReimbs(req, resp);
			else if (reimbStatus.equals("denied"))
				handleGetDeniedReimbs(req, resp);
			else if (userId != 0)
				handleGetUserReimbs(req, resp, userId);
			else if (reimbId != 0)
				handleGet(req, resp, reimbId);
			else if (parse[2].equals("reimbursements"))
				handleGetAllReimbs(req, resp);
			break;
		case "POST":
			if (parse[2].equals("reimbursements"))
				handleCreate(req, resp);
			break;
		case "PUT":
			handleUpdate(req, resp, reimbId);
			break;
		case "DELETE":
			handleDelete(req, resp, reimbId);
			break;
		}
	}

	private void handleGetAllReimbs(HttpServletRequest req, HttpServletResponse resp)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Reimbursement> reimbs = rs.getAllReimbs();
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimbs);
	}

	private void handleGetPendingReimbs(HttpServletRequest req, HttpServletResponse resp)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Reimbursement> reimbs = rs.getPendingReimbs();
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimbs);
	}

	private void handleGetApprovedReimbs(HttpServletRequest req, HttpServletResponse resp)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Reimbursement> reimbs = rs.getApprovedReimbs();
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimbs);
	}

	private void handleGetDeniedReimbs(HttpServletRequest req, HttpServletResponse resp)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Reimbursement> reimbs = rs.getDeniedReimbs();
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimbs);
	}

	private void handleGetUserReimbs(HttpServletRequest req, HttpServletResponse resp, int id)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Reimbursement> reimbs = rs.getUserReimbs(id);
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimbs);
	}

	private void handleGet(HttpServletRequest req, HttpServletResponse resp, int id)
			throws JsonParseException, JsonMappingException, IOException {
		Reimbursement reimb = rs.getReimb(id);
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimb);
	}

	private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
			throws JsonParseException, JsonMappingException, IOException {
		Reimbursement reimb = om.readValue(req.getReader(), Reimbursement.class);
		rs.createReimb(reimb);
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimb);
	}

	private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, int id)
			throws JsonParseException, JsonMappingException, IOException {
		Reimbursement reimb = om.readValue(req.getReader(), Reimbursement.class);
		reimb = rs.updateReimb(reimb, id);

		// Set status and write json object
		resp.setStatus(201);
		om.writeValue(resp.getWriter(), reimb);
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp, int id)
			throws JsonGenerationException, JsonMappingException, IOException {
		int deleteCount = rs.deleteReimb(id);

		// Check if a reimbursement was deleted
		if (deleteCount > 0) {
			resp.setStatus(201);
			om.writeValue(resp.getWriter(), "Reimbursement has been successfully deleted");
		}
	}
}
