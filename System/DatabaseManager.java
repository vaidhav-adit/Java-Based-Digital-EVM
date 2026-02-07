package System;

import Users.Voter;
import Users.Candidate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ElectionSystem";
    private static final String USER = "root";
    private static final String PASS = "bLACKPANTHER123#";

    public static String customHash(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append((int) c);
        }
        return result.toString();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    public void initializeAdmin(String userName, String password, String staffId) throws SQLException {
        String sql = "INSERT INTO Admins (user_name, admin_password, staff_id) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE user_name=user_name";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, customHash(password));
            pstmt.setString(3, staffId);
            pstmt.executeUpdate();
        }
    }

    public boolean verifyAdmin(String userName, String password) throws SQLException {
        String sql = "SELECT * FROM Admins WHERE user_name = ? AND admin_password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, customHash(password));
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int addVoter(String voterId, String name, int age, String aadhaarNumber, String address, String constituencyName) throws SQLException {
        String sql = "INSERT INTO Voters (voter_id, user_name, voter_age, aadhaar_number, address, constituency_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, voterId);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, aadhaarNumber);
            pstmt.setString(5, address);
            pstmt.setInt(6, getConstituencyId(constituencyName));
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void updateVoterStatus(int voterDbId, boolean hasVoted) throws SQLException {
        String sql = "UPDATE Voters SET has_voted = ? WHERE voter_db_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, hasVoted);
            pstmt.setInt(2, voterDbId);
            pstmt.executeUpdate();
        }
    }

    public Voter getVoter(String aadhaarNumber) throws SQLException {
        String sql = "SELECT * FROM Voters WHERE aadhaar_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, aadhaarNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Voter voter = new Voter(rs.getString("user_name"), rs.getInt("voter_age"), rs.getString("aadhaar_number"), rs.getString("address"));
                    voter.setVoterDbId(rs.getInt("voter_db_id"));
                    return voter;
                }
            }
        }
        return null;
    }

    public void removeVoter(int voterDbId) throws SQLException {
        String sql = "DELETE FROM Voters WHERE voter_db_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, voterDbId);
            pstmt.executeUpdate();
        }
    }

    public List<Voter> getVoters(String constituencyName) throws SQLException {
        List<Voter> voters = new ArrayList<>();
        String sql = "SELECT * FROM Voters WHERE constituency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, getConstituencyId(constituencyName));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Voter voter = new Voter(rs.getString("user_name"), rs.getInt("voter_age"), rs.getString("aadhaar_number"), rs.getString("address"));
                    voter.setVoterDbId(rs.getInt("voter_db_id"));
                    voters.add(voter);
                }
            }
        }
        return voters;
    }

    public int addCandidate(String name, String symbol, String party, String constituencyName) throws SQLException {
        String sql = "INSERT INTO Candidates (candidate_name, candidate_symbol, candidate_party, constituency_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, symbol);
            pstmt.setString(3, party);
            pstmt.setInt(4, getConstituencyId(constituencyName));
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void removeCandidate(int candidateId) throws SQLException {
        String sql = "DELETE FROM Candidates WHERE candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.executeUpdate();
        }
    }

    public List<Candidate> getCandidates(String constituencyName) throws SQLException {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM Candidates WHERE constituency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, getConstituencyId(constituencyName));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Candidate candidate = new Candidate(
                        rs.getString("candidate_name"),
                        rs.getString("candidate_symbol"),
                        rs.getString("candidate_party"),
                        rs.getInt("candidate_id")
                    );
                    candidate.setVoteCount(rs.getInt("vote_count"));
                    candidates.add(candidate);
                }
            }
        }
        return candidates;
    }

    public void updateCandidateVoteCount(int candidateId) throws SQLException {
        String sql = "UPDATE Candidates SET vote_count = vote_count + 1 WHERE candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.executeUpdate();
        }
    }

    public void addVote(int candidateId, String constituencyName) throws SQLException {
        String sql = "INSERT INTO Votes (candidate_id, constituency_id, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, getConstituencyId(constituencyName));
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            updateCandidateVoteCount(candidateId);
        }
    }

    public List<VoteRecord> getVotes(String constituencyName) throws SQLException {
        List<VoteRecord> votes = new ArrayList<>();
        String sql = "SELECT v.vote_id, v.candidate_id, c.candidate_name, c.candidate_party, v.timestamp " +
                    "FROM Votes v JOIN Candidates c ON v.candidate_id = c.candidate_id " +
                    "WHERE v.constituency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, getConstituencyId(constituencyName));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    votes.add(new VoteRecord(
                        rs.getInt("vote_id"),
                        rs.getInt("candidate_id"),
                        rs.getString("candidate_name"),
                        rs.getString("candidate_party"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    ));
                }
            }
        }
        return votes;
    }

    public int addConstituency(String name) throws SQLException {
        String sql = "INSERT INTO Constituencies (constituency_name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return getConstituencyId(name);
    }

    public int getConstituencyId(String constituencyName) throws SQLException {
        String sql = "SELECT constituency_id FROM Constituencies WHERE constituency_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, constituencyName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("constituency_id");
                }
            }
        }
        return -1;
    }

    public static class VoteRecord {
        int voteId;
        int candidateId;
        String candidateName;
        String candidateParty;
        LocalDateTime timestamp;

        VoteRecord(int voteId, int candidateId, String candidateName, String candidateParty, LocalDateTime timestamp) {
            this.voteId = voteId;
            this.candidateId = candidateId;
            this.candidateName = candidateName;
            this.candidateParty = candidateParty;
            this.timestamp = timestamp;
        }
    }
} 