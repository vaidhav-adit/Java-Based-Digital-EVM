package Users;

import System.EVM;
import System.DatabaseManager;
import exceptions.SamePartyCandidateException;
import exceptions.CandidateNotFoundException;
import exceptions.AdminFailException;
import java.sql.SQLException;

public class Admin extends ElectionStaff implements ElectionRole, DataManager {
    String adminPassword;

    public Admin(String name, String password) {
        super(name);
        this.adminPassword = password;
    }

    public void addCandidate(EVM evm, String candidateName, String candidateSymbol, String candidateParty) {
        try {
            for (Candidate c : evm.getCandidateList()) {
                if (c.getCandidateParty().equalsIgnoreCase(candidateParty)) {
                    throw new SamePartyCandidateException("There is already another candidate contesting from this constituency. Only 1 candidate from each party is allowed per faction!");
                }
            }
            Candidate newCandidate = new Candidate(candidateName, candidateSymbol, candidateParty);
            evm.getCandidateList().add(newCandidate);
            DatabaseManager dbManager = new DatabaseManager();
            int candidateId = dbManager.addCandidate(candidateName, candidateSymbol, candidateParty, EVM.constituency);
            newCandidate.setCandidateId(candidateId);
            System.out.println("Candidate " + candidateName + " Representing: " + candidateParty + " has been added successfully.\n");
        } catch (SamePartyCandidateException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void removeCandidate(EVM evm, String candidateName, String candidateParty) {
        try {
            java.util.Iterator<Candidate> iterator = evm.getCandidateList().iterator();
            int candidateId = -1;
            while (iterator.hasNext()) {
                Candidate c = iterator.next();
                if (c.getCandidateName().equalsIgnoreCase(candidateName) && c.getCandidateParty().equalsIgnoreCase(candidateParty)) {
                    candidateId = c.getCandidateId();
                    iterator.remove();
                    System.out.println("Candidate " + c.getCandidateName() + " Representing " + c.getCandidateParty() + " has been removed successfully.\n");
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.removeCandidate(candidateId);
                    return;
                }
            }
            throw new CandidateNotFoundException("The specified candidate does not exist in the list. Please check spelling again.");
        } catch (CandidateNotFoundException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewResults(EVM evm) {
        try {
            System.out.println("\n--- Election Results ---");
            DatabaseManager dbManager = new DatabaseManager();
            java.util.List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            if (candidates.isEmpty()) {
                throw new CandidateNotFoundException("No candidates registered.");
            }
            for (Candidate c : candidates) {
                System.out.println(c.getCandidateName() + " (" + c.getCandidateParty() + ") (" + c.getCandidateSymbol() + "): " + c.getVoteCount() + " total votes");
            }
        } catch (CandidateNotFoundException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void accessSystem(EVM evm, java.util.Scanner scan) {
        try {
            System.out.println("Admin can configure system, manage candidates, and view results.\n");
            System.out.print("Enter admin password: ");
            String inputPassword = scan.nextLine();
            DatabaseManager dbManager = new DatabaseManager();
            if (!dbManager.verifyAdmin(getUserName(), inputPassword)) {
                throw new AdminFailException("Invalid password. Alerting Electoral Commission about unauthorized login attempt!\n");
            }
            System.out.println("Admin has been approved, Welcome " + getUserName() + ".");
            while (true) {
                System.out.println("\n1. Add Candidate | 2. Remove Candidate | 3. View Results | 4. Exit Admin Mode");
                int choice = scan.nextInt();
                scan.nextLine();
                if (choice == 1) {
                    System.out.print("Enter candidate name: ");
                    String name = scan.nextLine();
                    System.out.print("Enter candidate symbol: ");
                    String symbol = scan.nextLine();
                    System.out.print("Enter candidate party: ");
                    String party = scan.nextLine();
                    addCandidate(evm, name, symbol, party);
                } else if (choice == 2) {
                    System.out.print("Enter candidate name to remove: ");
                    String name = scan.nextLine();
                    System.out.print("Enter candidate party: ");
                    String party = scan.nextLine();
                    removeCandidate(evm, name, party);
                } else if (choice == 3) {
                    viewResults(evm);
                } else if (choice == 4) {
                    break;
                }
            }
        } catch (AdminFailException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void manageData(EVM evm) {
        // Implementation of data management for admin
    }
} 