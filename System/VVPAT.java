package System;

import Users.Candidate;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class VVPAT {
    public void generateSlip(Candidate candidate) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vvpat_slip.txt"))) {
            writer.write("Voter Verifiable Paper Audit Trail\n");
            writer.write("--------------------------------\n");
            writer.write("Candidate Name: " + candidate.getCandidateName() + "\n");
            writer.write("Party: " + candidate.getCandidateParty() + "\n");
            writer.write("Symbol: " + candidate.getCandidateSymbol() + "\n");
            writer.write("Timestamp: " + LocalDateTime.now() + "\n");
            writer.write("--------------------------------\n");
            writer.write("This is your VVPAT slip. Please verify your vote.\n");
        } catch (IOException e) {
            System.out.println("Error generating VVPAT slip: " + e.getMessage());
        }
    }

    public void showSlip(Candidate candidate) {
        System.out.println("\n--- VVPAT Slip ---");
        System.out.println("Candidate Name: " + candidate.getCandidateName());
        System.out.println("Party: " + candidate.getCandidateParty());
        System.out.println("Symbol: " + candidate.getCandidateSymbol());
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("-------------------");
        System.out.println("Please verify your vote.");
    }

    public void printSlip(Candidate candidate) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("printed_slips.txt", true))) {
            writer.write("Voter Verifiable Paper Audit Trail\n");
            writer.write("--------------------------------\n");
            writer.write("Candidate Name: " + candidate.getCandidateName() + "\n");
            writer.write("Party: " + candidate.getCandidateParty() + "\n");
            writer.write("Symbol: " + candidate.getCandidateSymbol() + "\n");
            writer.write("Timestamp: " + LocalDateTime.now() + "\n");
            writer.write("--------------------------------\n\n");
        } catch (IOException e) {
            System.out.println("Error printing VVPAT slip: " + e.getMessage());
        }
    }
} 