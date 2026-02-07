package System;

import Users.Candidate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EVM {
    private ArrayList<Candidate> candidateList = new ArrayList<>();
    private static final String master_admin_password = "VUadmin@123";
    public static String constituency;
    private List<Ballot> voteBuffer = new ArrayList<>();
    private int voterCounter = 1;

    public void displayCandidates() {
        System.out.println("\n--- List of Candidates ---");
        for (int i = 0; i < candidateList.size(); i++) {
            Candidate c = candidateList.get(i);
            System.out.println((i + 1) + ". " + c.getCandidateName() + " (" + c.getCandidateParty() + ") (" + c.getCandidateSymbol() + ")");
        }
    }

    public boolean castVote(int buttonPressed) {
        if (buttonPressed < 1 || buttonPressed > candidateList.size()) {
            return false;
        }
        Candidate selectedCandidate = candidateList.get(buttonPressed - 1);
        selectedCandidate.addVote();
        String anonymizedID = "V" + voterCounter++;
        Ballot ballot = new Ballot(anonymizedID, selectedCandidate.getCandidateName());
        voteBuffer.add(ballot);
        return true;
    }

    public static boolean verifyAdmin(String inputPassword) {
        return inputPassword.equals(master_admin_password);
    }

    public ArrayList<Candidate> getCandidateList() {
        return candidateList;
    }

    public void shineRedLight() {
        System.out.println("Red light is shining");
    }

    public void logVote(String voterID, String candidateID) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vote_log.txt", true))) {
            writer.write(voterID + "," + candidateID + "," + LocalDateTime.now() + "\n");
        } catch (IOException e) {
            System.out.println("Error writing to vote log: " + e.getMessage());
        }
    }

    public void writeVotesToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("votes.csv"))) {
            writer.write("VoterID,Candidate,Timestamp\n");
            for (Ballot ballot : voteBuffer) {
                writer.write(ballot.anonymizedID + "," + ballot.candidateID + "," + ballot.timestamp + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private class Ballot {
        String anonymizedID;
        String candidateID;
        LocalDateTime timestamp;

        Ballot(String voterID, String candidateID) {
            this.anonymizedID = voterID;
            this.candidateID = candidateID;
            this.timestamp = LocalDateTime.now();
        }
    }
} 