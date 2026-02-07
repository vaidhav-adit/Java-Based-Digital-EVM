package System;

import Users.Voter;
import Users.ElectionRole;
import Users.DataManager;
import exceptions.InvalidAgeException;
import exceptions.RemoveVoterException;
import java.util.HashMap;
import java.sql.SQLException;

public class ElectionCommission implements ElectionRole, DataManager {
    private HashMap<String, Voter> voterList = new HashMap<>();

    public void registerVoter(String name, int voterAge, String aadhaarNumber, String address) {
        try {
            if (voterAge < 18) {
                throw new InvalidAgeException("Voter must be at least 18 years old to register!");
            }
            Voter newVoter = new Voter(name, voterAge, aadhaarNumber, address);
            DatabaseManager dbManager = new DatabaseManager();
            int voterDbId = dbManager.addVoter(newVoter.getVoterID(), name, voterAge, aadhaarNumber, address, EVM.constituency);
            newVoter.setVoterDbId(voterDbId);
            voterList.put(aadhaarNumber, newVoter);
            System.out.println("Voter " + name + " has been registered successfully!");
        } catch (InvalidAgeException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void removeVoter(String name, String aadhaarNumber, int reason) {
        try {
            Voter voter = voterList.get(aadhaarNumber);
            if (voter == null) {
                throw new RemoveVoterException("Voter not found in the system!");
            }
            if (!voter.getUserName().equals(name)) {
                throw new RemoveVoterException("Name and Aadhaar number do not match!");
            }
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.removeVoter(voter.getVoterDbId());
            voterList.remove(aadhaarNumber);
            System.out.println("Voter " + name + " has been removed from the system.");
        } catch (RemoveVoterException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewAllVoters() {
        try {
            System.out.println("\n--- Registered Voters ---");
            DatabaseManager dbManager = new DatabaseManager();
            java.util.List<Voter> voters = dbManager.getVoters(EVM.constituency);
            if (voters.isEmpty()) {
                System.out.println("No voters registered yet.");
                return;
            }
            for (Voter v : voters) {
                System.out.println("Name: " + v.getUserName());
                System.out.println("Voter ID: " + v.getVoterID());
                System.out.println("Age: " + v.getVoterAge());
                System.out.println("Aadhaar: " + v.getAadhaarNumber());
                System.out.println("Address: " + v.getAddress());
                System.out.println("Voted: " + (v.getVoteStatus() ? "Yes" : "No"));
                System.out.println("-------------------");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public HashMap<String, Voter> getVoterList() {
        return voterList;
    }

    @Override
    public void accessSystem(EVM evm, java.util.Scanner scan) {
        System.out.println("Election Commission can manage voters and oversee the election process.");
        while (true) {
            System.out.println("\n1. Register Voter | 2. Remove Voter | 3. View All Voters | 4. Exit");
            int choice = scan.nextInt();
            scan.nextLine();
            if (choice == 1) {
                System.out.print("Enter voter name: ");
                String name = scan.nextLine();
                System.out.print("Enter voter age: ");
                int age = scan.nextInt();
                scan.nextLine();
                System.out.print("Enter Aadhaar number: ");
                String aadhaar = scan.nextLine();
                System.out.print("Enter address: ");
                String address = scan.nextLine();
                registerVoter(name, age, aadhaar, address);
            } else if (choice == 2) {
                System.out.print("Enter voter name: ");
                String name = scan.nextLine();
                System.out.print("Enter Aadhaar number: ");
                String aadhaar = scan.nextLine();
                System.out.print("Enter reason (1-3): ");
                int reason = scan.nextInt();
                scan.nextLine();
                removeVoter(name, aadhaar, reason);
            } else if (choice == 3) {
                viewAllVoters();
            } else if (choice == 4) {
                break;
            }
        }
    }

    @Override
    public void manageData(EVM evm) {
        // Implementation of data management for election commission
    }
} 