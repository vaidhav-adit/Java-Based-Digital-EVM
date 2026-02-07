import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Exception classes
class SamePartyCandidateException extends Exception
{
    public SamePartyCandidateException(String message)
    {
        super(message);
    }
}

class AdminFailException extends Exception
{
    public AdminFailException(String message)
    {
        super(message);
    }
}

class CandidateNotFoundException extends Exception
{
    public CandidateNotFoundException(String message)
    {
        super(message);
    }
}

class InvalidVoteException extends Exception
{
    public InvalidVoteException(String message)
    {
        super(message);
    }
}

class InvalidAgeException extends Exception
{
    public InvalidAgeException(String message)
    {
        super(message);
    }
}

class RemoveVoterException extends Exception
{
    public RemoveVoterException(String message)
    {
        super(message);
    }
}

class VerifyVoterException extends Exception
{
    public VerifyVoterException(String message)
    {
        super(message);
    }
}

// Interface: ElectionRole
interface ElectionRole
{
    void accessSystem(EVM evm, Scanner scan);
}

// Interface: DataManager
interface DataManager
{
    void manageData(EVM evm);
}

// Abstract User class
abstract class User
{
    private String userName;

    public User(String userName)
    {
        this.userName = userName;
    }

    abstract void accessSystem(EVM evm, Scanner scan);

    public String getUserName()
    {
        return userName;
    }
}

// ElectionStaff class for multilevel inheritance
abstract class ElectionStaff extends User
{
    private String staffID;
    private static int sIDcounter = 100;

    public ElectionStaff(String name)
    {
        super(name);
        this.staffID = "STAFF" + sIDcounter++;
    }

    public String getStaffID()
    {
        return staffID;
    }
}

// Admin subclass
class Admin extends ElectionStaff implements ElectionRole, DataManager
{
    String adminPassword;

    Admin(String name, String password)
    {
        super(name);
        this.adminPassword = password;
    }

    void addCandidate(EVM evm, String candidateName, String candidateSymbol, String candidateParty)
    {
        try
        {
            for (Candidate c : evm.getCandidateList())
            {
                if (c.getCandidateParty().equalsIgnoreCase(candidateParty))
                {
                    throw new SamePartyCandidateException("There is already another candidate contesting from this constituency. Only 1 candidate from each party is allowed per faction!");
                }
            }
            Candidate newCandidate = new Candidate(candidateName, candidateSymbol, candidateParty);
            evm.getCandidateList().add(newCandidate);
            DatabaseManager dbManager = new DatabaseManager();
            int candidateId = dbManager.addCandidate(candidateName, candidateSymbol, candidateParty, EVM.constituency);
            newCandidate.setCandidateId(candidateId);
            System.out.println("Candidate " + candidateName + " Representing: " + candidateParty + " has been added successfully.\n");
        }
        catch (SamePartyCandidateException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    void removeCandidate(EVM evm, String candidateName, String candidateParty)
    {
        try
        {
            Iterator<Candidate> iterator = evm.getCandidateList().iterator();
            int candidateId = -1;
            while (iterator.hasNext())
            {
                Candidate c = iterator.next();
                if (c.getCandidateName().equalsIgnoreCase(candidateName) && c.getCandidateParty().equalsIgnoreCase(candidateParty))
                {
                    candidateId = c.getCandidateId();
                    iterator.remove();
                    System.out.println("Candidate " + c.getCandidateName() + " Representing " + c.getCandidateParty() + " has been removed successfully.\n");
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.removeCandidate(candidateId);
                    return;
                }
            }
            throw new CandidateNotFoundException("The specified candidate does not exist in the list. Please check spelling again.");
        }
        catch (CandidateNotFoundException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewResults(EVM evm)
    {
        try
        {
            System.out.println("\n--- Election Results ---");
            DatabaseManager dbManager = new DatabaseManager();
            List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            if (candidates.isEmpty())
            {
                throw new CandidateNotFoundException("No candidates registered.");
            }
            for (Candidate c : candidates)
            {
                System.out.println(c.getCandidateName() + " (" + c.getCandidateParty() + ") (" + c.getCandidateSymbol() + "): " + c.getVoteCount() + " total votes");
            }
        }
        catch (CandidateNotFoundException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void accessSystem(EVM evm, Scanner scan)
    {
        try
        {
            System.out.println("Admin can configure system, manage candidates, and view results.\n");
            System.out.print("Enter admin password: ");
            String inputPassword = scan.nextLine();
            DatabaseManager dbManager = new DatabaseManager();
            if (!dbManager.verifyAdmin(getUserName(), inputPassword))
            {
                throw new AdminFailException("Invalid password. Alerting Electoral Commission about unauthorized login attempt!\n");
            }
            System.out.println("Admin has been approved, Welcome " + getUserName() + ".");
            while (true)
            {
                System.out.println("\n1. Add Candidate | 2. Remove Candidate | 3. View Results | 4. Exit Admin Mode");
                int choice = scan.nextInt();
                scan.nextLine();
                if (choice == 1)
                {
                    System.out.print("Enter candidate name: ");
                    String name = scan.nextLine();
                    System.out.print("Enter candidate symbol: ");
                    String symbol = scan.nextLine();
                    System.out.print("Enter candidate party: ");
                    String party = scan.nextLine();
                    addCandidate(evm, name, symbol, party);
                }
                else if (choice == 2)
                {
                    System.out.print("Enter candidate name to remove: ");
                    String name = scan.nextLine();
                    System.out.print("Enter candidate party: ");
                    String party = scan.nextLine();
                    removeCandidate(evm, name, party);
                }
                else if (choice == 3)
                {
                    viewResults(evm);
                }
                else if (choice == 4)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid choice. Please select 1, 2, 3, or 4.");
                }
            }
        }
        catch (AdminFailException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void manageData(EVM evm)
    {
        System.out.println("Admin managing election data.");
        addCandidate(evm, "New Candidate", "New Symbol", "New Party");
    }
}

// Voter subclass
class Voter extends User implements ElectionRole
{
    private String voterID;
    private static int vIDcounter = 100;
    private boolean hasVoted;
    private int voterAge;
    private String aadhaarNumber;
    private String address;
    private int voterDbId;

    Voter(String name, int voterAge, String aadhaarNumber, String address)
    {
        super(name);
        this.voterAge = voterAge;
        this.voterID = "VOTER" + vIDcounter++;
        this.hasVoted = false;
        this.aadhaarNumber = aadhaarNumber;
        this.address = address;
        this.voterDbId = -1;
    }

    Voter(String name, String aadhaarNumber)
    {
        this(name, 18, aadhaarNumber, "Unknown");
    }

    void castVote(EVM evm, VVPAT vvpat, Scanner scan)
    {
        try
        {
            if (hasVoted)
            {
                throw new InvalidVoteException("You have already voted. Duplicate voting is not allowed.\n");
            }
            if (evm.getCandidateList().isEmpty())
            {
                throw new InvalidVoteException("No candidates available to vote for.\n");
            }
            evm.displayCandidates();
            System.out.println("\nPlease enter the number associated with the candidate you'd like to vote for:");
            int choice = scan.nextInt();
            scan.nextLine();
            if (choice < 1 || choice > evm.getCandidateList().size())
            {
                throw new InvalidVoteException("Invalid button press. Vote cancelled.\n");
            }
            Candidate selected = evm.getCandidateList().get(choice - 1);
            evm.castVote(choice);
            evm.shineRedLight();
            vvpat.generateSlip(selected);
            vvpat.showSlip(selected);
            vvpat.printSlip(selected);
            hasVoted = true;
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.addVote(voterDbId, selected.getCandidateId(), EVM.constituency);
            dbManager.updateVoterStatus(voterDbId, true);
        }
        catch (InvalidVoteException e)
        {
            System.out.println("Exception caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    void castVote(EVM evm, VVPAT vvpat, int candidateIndex)
    {
        try
        {
            if (hasVoted)
            {
                throw new InvalidVoteException("You have already voted. Duplicate voting is not allowed.\n");
            }
            if (evm.getCandidateList().isEmpty())
            {
                throw new InvalidVoteException("No candidates available to vote for.\n");
            }
            if (candidateIndex < 1 || candidateIndex > evm.getCandidateList().size())
            {
                throw new InvalidVoteException("Invalid candidate index. Vote cancelled.\n");
            }
            Candidate selected = evm.getCandidateList().get(candidateIndex - 1);
            evm.castVote(candidateIndex);
            evm.shineRedLight();
            vvpat.generateSlip(selected);
            vvpat.showSlip(selected);
            vvpat.printSlip(selected);
            hasVoted = true;
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.addVote(voterDbId, selected.getCandidateId(), EVM.constituency);
            dbManager.updateVoterStatus(voterDbId, true);
        }
        catch (InvalidVoteException e)
        {
            System.out.println("Exception caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void accessSystem(EVM evm, Scanner scan)
    {
        System.out.println("Voter can vote for desired candidate.");
    }

    public boolean getVoteStatus()
    {
        return hasVoted;
    }

    public String getVoterID()
    {
        return voterID;
    }

    public int getVoterAge()
    {
        return voterAge;
    }

    public String getAadhaarNumber()
    {
        return aadhaarNumber;
    }

    public String getAddress()
    {
        return address;
    }

    public int getVoterDbId()
    {
        return voterDbId;
    }

    public void setVoterDbId(int voterDbId)
    {
        this.voterDbId = voterDbId;
    }
}

// ElectionOfficer class
class ElectionOfficer extends ElectionStaff implements ElectionRole
{
    private String officerName;

    public ElectionOfficer(String officerName)
    {
        super(officerName);
        this.officerName = officerName;
    }

    public String getOfficerName()
    {
        return officerName;
    }

    boolean verifyVoter(Voter voter)
    {
        try
        {
            if (voter.getVoterAge() < 18)
            {
                throw new VerifyVoterException("Verification failed: Voter is underage.\n");
            }
            DatabaseManager dbManager = new DatabaseManager();
            Voter dbVoter = dbManager.getVoter(voter.getVoterID());
            if (dbVoter == null)
            {
                throw new VerifyVoterException("Voter not found in the system.\n");
            }
            String aadhaar = voter.getAadhaarNumber();
            if (aadhaar.length() != 12)
            {
                throw new VerifyVoterException("Invalid Aadhaar number. Voter has been rejected.\n");
            }
            String address = voter.getAddress().toLowerCase();
            if (!address.contains(EVM.constituency.toLowerCase()))
            {
                throw new VerifyVoterException("Voter does not belong to this constituency. Voter has been rejected.\n");
            }
            System.out.println("Voter verified successfully by Officer " + getOfficerName() + ". Can proceed for voting.\n");
            return true;
        }
        catch (VerifyVoterException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
            return false;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void accessSystem(EVM evm, Scanner scan)
    {
        System.out.println("Election Officer access system.\n");
    }
}

// Candidate class
class Candidate
{
    private String candidateName;
    private String candidateSymbol;
    private String candidateParty;
    private int countVotes;
    private int candidateId;

    Candidate(String name, String symbol, String candidateParty)
    {
        this.candidateName = name;
        this.candidateSymbol = symbol;
        this.candidateParty = candidateParty;
        this.countVotes = 0;
        this.candidateId = -1;
    }

    Candidate(String name, String symbol, String candidateParty, int candidateId)
    {
        this(name, symbol, candidateParty);
        this.candidateId = candidateId;
    }

    public String getCandidateName()
    {
        return candidateName;
    }

    public String getCandidateSymbol()
    {
        return candidateSymbol;
    }

    public String getCandidateParty()
    {
        return candidateParty;
    }

    public int getVoteCount()
    {
        return countVotes;
    }

    public int getCandidateId()
    {
        return candidateId;
    }

    public void addVote()
    {
        countVotes++;
    }

    public void setVoteCount(int voteCount)
    {
        this.countVotes = voteCount;
    }

    public void setCandidateId(int candidateId)
    {
        this.candidateId = candidateId;
    }
}

// EVM class
class EVM
{
    private ArrayList<Candidate> candidateList = new ArrayList<>();
    private static final String master_admin_password = "VUadmin@123";
    public static String constituency;
    private List<Ballot> voteBuffer = new ArrayList<>();
    private int voterCounter = 1;

    public void displayCandidates()
    {
        Iterator<Candidate> iterator = candidateList.iterator();
        int index = 1;
        if (!iterator.hasNext())
        {
            System.out.println("No candidates registered.");
        }
        while (iterator.hasNext())
        {
            Candidate c = iterator.next();
            System.out.println(index++ + ". Candidate: " + c.getCandidateName() + ", Party: " + c.getCandidateParty() + ", Symbol: " + c.getCandidateSymbol());
        }
    }

    public boolean castVote(int buttonPressed)
    {
        try
        {
            if (buttonPressed < 1 || buttonPressed > candidateList.size())
            {
                throw new InvalidVoteException("Invalid button press.");
            }
            Candidate selected = candidateList.get(buttonPressed - 1);
            logVote("Voter" + voterCounter++, selected.getCandidateName());
            selected.addVote();
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.updateCandidateVoteCount(selected.getCandidateId());
            return true;
        }
        catch (InvalidVoteException e)
        {
            System.out.println("Invalid Vote Exception: " + e.getMessage());
            return false;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyAdmin(String inputPassword)
    {
        return master_admin_password.equals(inputPassword);
    }

    public ArrayList<Candidate> getCandidateList()
    {
        return candidateList;
    }

    public void shineRedLight()
    {
        System.out.println("🚨🚨🚨🚨Red light ON. Beep!🚨🚨🚨🚨\n");
    }

    public void logVote(String voterID, String candidateID)
    {
        voteBuffer.add(new Ballot(voterID, candidateID));
    }

    public void writeVotesToCSV()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("votes.csv", true));
            writer.write("AnonymizedID,CandidateID,CandidateName,CandidateParty,Timestamp\n");
            for (Ballot ballot : voteBuffer)
            {
                Candidate candidate = null;
                for (Candidate c : candidateList)
                {
                    if (c.getCandidateName().equals(ballot.candidateID))
                    {
                        candidate = c;
                        break;
                    }
                }
                if (candidate == null)
                {
                    throw new IOException("Candidate not found for ballot.");
                }
                writer.write(ballot.anonymizedID + "," + candidate.getCandidateId() + "," + candidate.getCandidateName() + "," + candidate.getCandidateParty() + "," + ballot.timestamp + "\n");
            }
            DatabaseManager dbManager = new DatabaseManager();
            List<DatabaseManager.VoteRecord> dbVotes = dbManager.getVotes(constituency);
            for (DatabaseManager.VoteRecord vote : dbVotes)
            {
                writer.write("VoterX," + vote.candidateId + "," + vote.candidateName + "," + vote.candidateParty + "," + vote.timestamp + "\n");
            }
            writer.close();
            System.out.println("Votes appended to votes.csv\n");
        }
        catch (IOException e)
        {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    private class Ballot
    {
        String anonymizedID;
        String candidateID;
        LocalDateTime timestamp;

        Ballot(String voterID, String candidateID)
        {
            this.anonymizedID = "VoterX";
            this.candidateID = candidateID;
            this.timestamp = LocalDateTime.now();
        }
    }
}

// VVPAT class
class VVPAT
{
    public void generateSlip(Candidate candidate)
    {
        try
        {
            LocalDateTime dtime = LocalDateTime.now();
            System.out.println("Generating Slip of Confirmation.....");
            Thread.sleep(5000);
            if (candidate == null)
            {
                throw new InterruptedException("Candidate is null.");
            }
            System.out.println("Candidate Name: " + candidate.getCandidateName());
            System.out.println("Candidate Party: " + candidate.getCandidateParty());
            System.out.println("Candidate Party Symbol: " + candidate.getCandidateSymbol());
            System.out.println("Generated on: " + dtime);
        }
        catch (InterruptedException e)
        {
            System.out.println("Error during slip generation delay: " + e.getMessage());
        }
    }

    public void showSlip(Candidate candidate)
    {
        LocalDateTime dtime = LocalDateTime.now();
        System.out.println("Candidate: " + candidate.getCandidateName() + ", Party: " + candidate.getCandidateParty() + ", Timestamp: " + dtime);
    }

    public void printSlip(Candidate candidate)
    {
        try
        {
            LocalDateTime dtime = LocalDateTime.now();
            System.out.println("Printing the slip...");
            Thread.sleep(5000);
            if (candidate == null)
            {
                throw new InterruptedException("Candidate is null.");
            }
            System.out.println("Candidate: " + candidate.getCandidateName() + ", Party: " + candidate.getCandidateParty() + ", Timestamp: " + dtime);
            System.out.println("Dropping slip into box...");
            System.out.println("Vote placed in secure box.\n");
        }
        catch (InterruptedException e)
        {
            System.out.println("Error during slip printing delay: " + e.getMessage());
        }
    }
}

// ElectionCommission class
class ElectionCommission implements ElectionRole, DataManager
{
    private HashMap<String, Voter> voterList = new HashMap<>();

    public void registerVoter(String name, int voterAge, String aadhaarNumber, String address)
    {
        try
        {
            if (voterAge < 18)
            {
                throw new InvalidAgeException("The individual is too young to vote. Only people 18 and above are allowed to vote.\n");
            }
            Voter newVoter = new Voter(name, voterAge, aadhaarNumber, address);
            voterList.put(newVoter.getVoterID(), newVoter);
            DatabaseManager dbManager = new DatabaseManager();
            int voterDbId = dbManager.addVoter(newVoter.getVoterID(), name, voterAge, aadhaarNumber, address, EVM.constituency);
            if (voterDbId != -1)
            {
                newVoter.setVoterDbId(voterDbId);
                System.out.println("Voter " + newVoter.getUserName() + ", Age: " + newVoter.getVoterAge() + ", ID: " + newVoter.getVoterID() + " has been registered!\n");
            }
            else
            {
                voterList.remove(newVoter.getVoterID());
                throw new SQLException("Failed to register voter in database.");
            }
        }
        catch (InvalidAgeException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void removeVoter(String name, String aadhaarNumber, int reason)
    {
        try
        {
            if (reason == 1)
            {
                int matchCount = 0;
                for (Voter v : voterList.values())
                {
                    if (v.getUserName().equalsIgnoreCase(name) && v.getAadhaarNumber().equals(aadhaarNumber))
                    {
                        matchCount++;
                    }
                }
                if (matchCount <= 1)
                {
                    throw new RemoveVoterException("Only one entry found. No duplicates exist for voter");
                }
                int removedCount = 0;
                Iterator<Voter> iterator = voterList.values().iterator();
                while (iterator.hasNext() && removedCount < matchCount - 1)
                {
                    Voter v = iterator.next();
                    if (v.getUserName().equalsIgnoreCase(name) && v.getAadhaarNumber().equals(aadhaarNumber))
                    {
                        String voterId = v.getVoterID();
                        voterList.remove(voterId);
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.removeVoter(v.getVoterDbId());
                        removedCount++;
                    }
                }
                System.out.println("Duplicate entries found. " + removedCount + " duplicate(s) removed for voter: " + name + ", Aadhaar: " + aadhaarNumber);
            }
            else if (reason > 1 && reason < 7)
            {
                boolean removed = false;
                Iterator<Voter> iterator = voterList.values().iterator();
                while (iterator.hasNext())
                {
                    Voter v = iterator.next();
                    if (v.getUserName().equalsIgnoreCase(name) && v.getAadhaarNumber().equals(aadhaarNumber))
                    {
                        String voterId = v.getVoterID();
                        voterList.remove(voterId);
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.removeVoter(v.getVoterDbId());
                        System.out.println("IMMEDIATE ACTION REQUIRED: Voter " + name + " (Aadhaar: " + aadhaarNumber + ") has been removed due to reason No." + reason + ".");
                        removed = true;
                        break;
                    }
                }
                if (!removed)
                {
                    throw new RemoveVoterException("No voter found with the given name and Aadhaar number.\n");
                }
            }
            else
            {
                throw new RemoveVoterException("Invalid reason selected. Please select a number between 1 and 6.\n");
            }
        }
        catch (RemoveVoterException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewAllVoters()
    {
        try
        {
            System.out.println("\n--- Registered Voters ---");
            DatabaseManager dbManager = new DatabaseManager();
            List<Voter> voters = dbManager.getVoters(EVM.constituency);
            if (voters.isEmpty())
            {
                throw new SQLException("No voters registered.");
            }
            for (Voter v : voters)
            {
                System.out.println("ID: " + v.getVoterID() + ", Name: " + v.getUserName() + ", Age: " + v.getVoterAge() + ", Aadhaar: " + v.getAadhaarNumber() + ", Address: " + v.getAddress());
            }
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public HashMap<String, Voter> getVoterList()
    {
        return voterList;
    }

    @Override
public void accessSystem(EVM evm, Scanner scan)
{
    System.out.println("Election Commission can manage voter registration and removal.\n");
    while (true)
    {
        System.out.println("\n1. Register Voter | 2. Remove Voter | 3. View All Voters | 4. Exit Election Commission Mode");
        int choice = scan.nextInt();
        scan.nextLine();
        if (choice == 1)
        {
            System.out.print("Full registration (Y/N)? ");
            String fullReg = scan.nextLine();
            if (fullReg.equalsIgnoreCase("N"))
            {
                System.out.print("Enter voter name: ");
                String name = scan.nextLine();
                System.out.print("Enter Aadhaar number: ");
                String aadhaar = scan.nextLine();
                registerVoter(name, 18, aadhaar, "Unknown");
            }
            else
            {
                System.out.print("Enter voter name: ");
                String name = scan.nextLine();
                System.out.print("Enter voter age: ");
                int age = scan.nextInt();
                scan.nextLine();
                System.out.print("Enter Aadhaar number (12 digits): ");
                String aadhaar = scan.nextLine();
                System.out.print("Enter address: ");
                String address = scan.nextLine();
                registerVoter(name, age, aadhaar, address);
            }
        }
        else if (choice == 2)
        {
            System.out.print("Enter voter name to remove: ");
            String name = scan.nextLine();
            System.out.print("Enter voter Aadhaar (12 digits): ");
            String aadhaarNumber = scan.nextLine();
            System.out.println("Select reason for removal:");
            System.out.println("1. Duplicate entry");
            System.out.println("2. Voter's Demise");
            System.out.println("3. Change of Address");
            System.out.println("4. Disqualification");
            System.out.println("5. Non-existent");
            System.out.println("6. NRI Citizenship");
            int reason = scan.nextInt();
            scan.nextLine(); // consume newline
            removeVoter(name, aadhaarNumber, reason);
        }
        else if (choice == 3)
        {
            viewAllVoters();
        }
        else if (choice == 4)
        {
            break;
        }
        else
        {
            System.out.println("Invalid choice. Please select 1, 2, 3, or 4.");
        }
    }
}
@Override
    public void manageData(EVM evm) {
        System.out.println("Election Commission managing voter data.");
        // Add your implementation here, e.g., registerVoter calls
    }
}

// DatabaseManager class
class DatabaseManager
{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ElectionSystem";
    private static final String USER = "root";
    private static final String PASS = "bLACKPANTHER123#";

    public static String customHash(String input)
    {
        int sum = 0;
        for (char c : input.toCharArray())
        {
            sum += (int) c;
        }
        return String.valueOf(sum * 31);
    }

    public static Connection getConnection() throws SQLException
    {
        try
        {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            if (conn == null)
            {
                throw new SQLException("Failed to establish database connection.");
            }
            return conn;
        }
        catch (SQLException e)
        {
            throw new SQLException("Database Error: " + e.getMessage());
        }
    }

    public void initializeAdmin(String userName, String password, String staffId) throws SQLException
    {
        try
        {
            String sql = "INSERT INTO Admins (user_name, staff_id, admin_password) VALUES (?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setString(2, staffId);
            pstmt.setString(3, customHash(password));
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            if (!e.getMessage().contains("Duplicate entry"))
            {
                System.out.println("Database Error: " + e.getMessage());
            }
        }
    }

    public boolean verifyAdmin(String userName, String password) throws SQLException
    {
        try
        {
            String sql = "SELECT admin_password FROM Admins WHERE user_name = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next())
            {
                throw new SQLException("Admin not found.");
            }
            String storedPasswordHash = rs.getString("admin_password");
            String inputPasswordHash = customHash(password);
            if (!storedPasswordHash.equals(inputPasswordHash))
            {
                throw new SQLException("Invalid password.");
            }
            rs.close();
            pstmt.close();
            conn.close();
            return true;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    public int addVoter(String voterId, String name, int age, String aadhaarNumber, String address, String constituencyName) throws SQLException
    {
        try
        {
            if (age < 18)
            {
                throw new InvalidAgeException("Voter must be 18 or older.");
            }
            int constituencyId = getConstituencyId(constituencyName);
            String sql = "INSERT INTO Voters (voter_id, user_name, voter_age, aadhaar_number, address, constituency_id) VALUES (?, ?, ?, ?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, voterId);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, aadhaarNumber);
            pstmt.setString(5, address);
            pstmt.setInt(6, constituencyId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Failed to add voter.");
            }
            ResultSet rs = pstmt.getGeneratedKeys();
            int voterDbId = -1;
            if (rs.next())
            {
                voterDbId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return voterDbId;
        }
        catch (InvalidAgeException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
            return -1;
        }
        catch (SQLException e)
        {
            if (e.getMessage().contains("Duplicate entry"))
            {
                System.out.println("Database Error: Aadhaar number or voter ID already exists.");
            }
            else
            {
                System.out.println("Database Error: " + e.getMessage());
            }
            return -1;
        }
    }

    public void updateVoterStatus(int voterDbId, boolean hasVoted) throws SQLException
    {
        try
        {
            String sql = "UPDATE Voters SET has_voted = ? WHERE voter_db_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, hasVoted);
            pstmt.setInt(2, voterDbId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Voter not found.");
            }
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public Voter getVoter(String voterId) throws SQLException
    {
        try
        {
            String sql = "SELECT * FROM Voters WHERE voter_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, voterId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next())
            {
                throw new SQLException("Voter not found.");
            }
            Voter voter = new Voter(
                rs.getString("user_name"),
                rs.getInt("voter_age"),
                rs.getString("aadhaar_number"),
                rs.getString("address")
            );
            voter.setVoterDbId(rs.getInt("voter_db_id"));
            rs.close();
            pstmt.close();
            conn.close();
            return voter;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return null;
        }
    }

    public void removeVoter(int voterDbId) throws SQLException
    {
        try
        {
            String sql = "DELETE FROM Voters WHERE voter_db_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, voterDbId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Voter not found.");
            }
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public List<Voter> getVoters(String constituencyName) throws SQLException
    {
        try
        {
            int constituencyId = getConstituencyId(constituencyName);
            List<Voter> voters = new ArrayList<>();
            String sql = "SELECT * FROM Voters WHERE constituency_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, constituencyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Voter voter = new Voter(
                    rs.getString("user_name"),
                    rs.getInt("voter_age"),
                    rs.getString("aadhaar_number"),
                    rs.getString("address")
                );
                voter.setVoterDbId(rs.getInt("voter_db_id"));
                voters.add(voter);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return voters;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int addCandidate(String name, String symbol, String party, String constituencyName) throws SQLException
    {
        try
        {
            int constituencyId = getConstituencyId(constituencyName);
            String sql = "INSERT INTO Candidates (candidate_name, candidate_symbol, candidate_party, constituency_id) VALUES (?, ?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, symbol);
            pstmt.setString(3, party);
            pstmt.setInt(4, constituencyId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Failed to add candidate.");
            }
            ResultSet rs = pstmt.getGeneratedKeys();
            int candidateId = -1;
            if (rs.next())
            {
                candidateId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return candidateId;
        }
        catch (SQLException e)
        {
            if (e.getMessage().contains("Duplicate entry"))
            {
                System.out.println("Exception Caught: Party already has a candidate in this constituency.");
            }
            else
            {
                System.out.println("Database Error: " + e.getMessage());
            }
            return -1;
        }
    }

    public void removeCandidate(int candidateId) throws SQLException
    {
        try
        {
            String sql = "DELETE FROM Candidates WHERE candidate_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, candidateId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new CandidateNotFoundException("Candidate not found.");
            }
            pstmt.close();
            conn.close();
        }
        catch (CandidateNotFoundException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public List<Candidate> getCandidates(String constituencyName) throws SQLException
    {
        try
        {
            int constituencyId = getConstituencyId(constituencyName);
            List<Candidate> candidates = new ArrayList<>();
            String sql = "SELECT * FROM Candidates WHERE constituency_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, constituencyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Candidate candidate = new Candidate(
                    rs.getString("candidate_name"),
                    rs.getString("candidate_symbol"),
                    rs.getString("candidate_party"),
                    rs.getInt("candidate_id")
                );
                candidate.setVoteCount(rs.getInt("vote_count"));
                candidates.add(candidate);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return candidates;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void updateCandidateVoteCount(int candidateId) throws SQLException
    {
        try
        {
            String sql = "UPDATE Candidates SET vote_count = vote_count + 1 WHERE candidate_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, candidateId);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Candidate not found.");
            }
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void addVote(int voterDbId, int candidateId, String constituencyName) throws SQLException
    {
        try
        {
            String checkSql = "SELECT has_voted FROM Voters WHERE voter_db_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, voterDbId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getBoolean("has_voted"))
            {
                throw new InvalidVoteException("Voter has already voted.");
            }
            rs.close();
            pstmt.close();
            int constituencyId = getConstituencyId(constituencyName);
            String voteSql = "INSERT INTO Votes (candidate_id, constituency_id) VALUES (?, ?)";
            PreparedStatement votePstmt = conn.prepareStatement(voteSql);
            votePstmt.setInt(1, candidateId);
            votePstmt.setInt(2, constituencyId);
            int rows = votePstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Failed to add vote.");
            }
            votePstmt.close();
            conn.close();
        }
        catch (InvalidVoteException e)
        {
            System.out.println("Exception Caught: " + e.getMessage());
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public List<VoteRecord> getVotes(String constituencyName) throws SQLException
    {
        try
        {
            int constituencyId = getConstituencyId(constituencyName);
            List<VoteRecord> votes = new ArrayList<>();
            String sql = "SELECT v.vote_id, v.candidate_id, c.candidate_name, c.candidate_party, v.timestamp " +
                        "FROM Votes v JOIN Candidates c ON v.candidate_id = c.candidate_id " +
                        "WHERE v.constituency_id = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, constituencyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                votes.add(new VoteRecord(
                    rs.getInt("vote_id"),
                    rs.getInt("candidate_id"),
                    rs.getString("candidate_name"),
                    rs.getString("candidate_party"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
            rs.close();
            pstmt.close();
            conn.close();
            return votes;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int addConstituency(String name) throws SQLException
    {
        try
        {
            String sql = "INSERT INTO Constituencies (constituency_name) VALUES (?)";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
            {
                throw new SQLException("Failed to add constituency.");
            }
            ResultSet rs = pstmt.getGeneratedKeys();
            int constituencyId = -1;
            if (rs.next())
            {
                constituencyId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return constituencyId;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return -1;
        }
    }

    private int getConstituencyId(String constituencyName) throws SQLException
    {
        try
        {
            String sql = "SELECT constituency_id FROM Constituencies WHERE constituency_name = ?";
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, constituencyName);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next())
            {
                throw new SQLException("Constituency not found.");
            }
            int constituencyId = rs.getInt("constituency_id");
            rs.close();
            pstmt.close();
            conn.close();
            return constituencyId;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            throw e;
        }
    }

    public static class VoteRecord
    {
        int voteId;
        int candidateId;
        String candidateName;
        String candidateParty;
        LocalDateTime timestamp;

        VoteRecord(int voteId, int candidateId, String candidateName, String candidateParty, LocalDateTime timestamp)
        {
            this.voteId = voteId;
            this.candidateId = candidateId;
            this.candidateName = candidateName;
            this.candidateParty = candidateParty;
            this.timestamp = timestamp;
        }
    }
}


// ElectionSystem class
class ElectionSystem
{
    private static final Scanner scan = new Scanner(System.in);
    public static ElectionCommission ec = new ElectionCommission();
    Admin admin;
    EVM evm;
    VVPAT vvpat;
    private Voter currentVoter;

    // GUI Components
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cards;
    private JPanel welcomePanel;
    private JPanel mainMenuPanel;
    private JPanel adminLoginPanel;
    private JPanel adminPanel;
    private JPanel voterVerificationPanel;
    private JPanel votingPanel;
    private JPanel ecPanel;
    private JPanel resultsPanel;
    private JTextArea resultsTextArea;

    void startElection()
    {
        System.out.println("\nElection started in constituency: " + EVM.constituency);
        while (true)
        {
            System.out.println("\nWho is interacting with the system?");
            System.out.println("1. Admin | 2. Voter | 3. Election Commission | 4. View Results | 5. End Election");
            int choice = scan.nextInt();
            scan.nextLine();

            if (choice == 1)
            {
                admin.accessSystem(evm, scan);
            }
            else if (choice == 2)
            {
                System.out.print("Enter voter ID: ");
                String voterID = scan.nextLine();
                Voter voter = ec.getVoterList().get(voterID);
                if (voter != null)
                {
                    voter.accessSystem(evm, scan);
                    ElectionOfficer officer = new ElectionOfficer("Officer1");
                    if (officer.verifyVoter(voter))
                    {
                        voter.castVote(evm, vvpat, scan);
                    }
                    else
                    {
                        System.out.println("Voter verification failed. Cannot vote.");
                    }
                }
                else
                {
                    System.out.println("Voter not found.");
                }
            }
            else if (choice == 3)
            {
                ec.accessSystem(evm, scan);
            }
            else if (choice == 4)
            {
                if (admin != null)
                {
                    admin.viewResults(evm);
                }
                else
                {
                    System.out.println("Admin not initialized. Please log in as Admin first.");
                }
            }
            else if (choice == 5)
            {
                evm.writeVotesToCSV();
                break;
            }
            else
            {
                System.out.println("Invalid choice. Please select 1, 2, 3, 4, or 5.");
            }
        }
    }

    void countVotes()
    {
        try
        {
            System.out.println("\n--- Vote Count ---");
            DatabaseManager dbManager = new DatabaseManager();
            List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            if (candidates.isEmpty())
            {
                throw new SQLException("No candidates found.");
            }
            for (Candidate c : candidates)
            {
                System.out.println(c.getCandidateName() + " (" + c.getCandidateParty() + "): " + c.getVoteCount() + " votes");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public Candidate findWinner()
    {
        try
        {
            DatabaseManager dbManager = new DatabaseManager();
            List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            if (candidates.isEmpty())
            {
                throw new SQLException("No candidates found.");
            }
            Candidate winner = candidates.get(0);
            boolean allTied = true;

            for (Candidate c : candidates)
            {
                if (c.getVoteCount() > winner.getVoteCount())
                {
                    winner = c;
                    allTied = false;
                }
                else if (c.getVoteCount() < winner.getVoteCount())
                {
                    allTied = false;
                }
            }

            if (allTied)
            {
                throw new SQLException("All candidates have the same number of votes. Constituency results in a DRAW!");
            }
            return winner;
        }
        catch (SQLException e)
        {
            System.out.println("Database Error: " + e.getMessage());
            return null;
        }
    }

    public void startGUI()
    {
        frame = new JFrame("Election Voting Machine");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        frame.add(cards);

        // Welcome Panel
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.LIGHT_GRAY);
        JLabel titleLabel = new JLabel("Election Voting Machine");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel constituencyLabel = new JLabel("Enter Constituency:");
        JTextField constituencyField = new JTextField(20);
        JButton setConstituencyButton = new JButton("Set Constituency");
        setConstituencyButton.addActionListener(e -> {
            String constituency = constituencyField.getText().trim();
            if (!constituency.isEmpty()) {
                EVM.constituency = constituency;
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.addConstituency(constituency);
                    initializeData(dbManager);
                    cardLayout.show(cards, "mainMenu");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error adding constituency: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a constituency name.");
            }
        });
        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(constituencyLabel);
        welcomePanel.add(constituencyField);
        welcomePanel.add(setConstituencyButton);
        welcomePanel.add(Box.createVerticalGlue());
        cards.add(welcomePanel, "welcome");

        // Main Menu Panel
        mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));
        mainMenuPanel.setBackground(Color.LIGHT_GRAY);
        JLabel roleLabel = new JLabel("Please Select your Role");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainMenuPanel.add(roleLabel);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JButton adminButton = new JButton("Admin");
        adminButton.setMaximumSize(new Dimension(200, 30));
        adminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminButton.addActionListener(e -> cardLayout.show(cards, "adminLogin"));
        mainMenuPanel.add(adminButton);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton voterButton = new JButton("Voter");
        voterButton.setMaximumSize(new Dimension(200, 30));
        voterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        voterButton.addActionListener(e -> cardLayout.show(cards, "voterVerification"));
        mainMenuPanel.add(voterButton);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton ecButton = new JButton("Election Commission");
        ecButton.setMaximumSize(new Dimension(200, 30));
        ecButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        ecButton.addActionListener(e -> cardLayout.show(cards, "ecPanel"));
        mainMenuPanel.add(ecButton);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton resultsButton = new JButton("View Results");
        resultsButton.setMaximumSize(new Dimension(200, 30));
        resultsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsButton.addActionListener(e -> {
            updateResultsPanel();
            cardLayout.show(cards, "resultsPanel");
        });
        mainMenuPanel.add(resultsButton);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton endElectionButton = new JButton("End Election");
        endElectionButton.setMaximumSize(new Dimension(200, 30));
        endElectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        endElectionButton.addActionListener(e -> {
            Candidate winner = findWinner();
            String message;
            if (winner != null) {
                message = "Election Winner: " + winner.getCandidateName() + " (" + winner.getCandidateParty() + ") with " + winner.getVoteCount() + " votes!";
            } else {
                message = "All candidates have the same number of votes. Constituency results in a DRAW!";
            }
            JOptionPane.showMessageDialog(frame, message, "Election Results", JOptionPane.INFORMATION_MESSAGE);
            evm.writeVotesToCSV();
            System.exit(0);
        });
        mainMenuPanel.add(endElectionButton);
        cards.add(mainMenuPanel, "mainMenu");

        // Admin Login Panel
        adminLoginPanel = new JPanel();
        adminLoginPanel.setLayout(new BoxLayout(adminLoginPanel, BoxLayout.Y_AXIS));
        adminLoginPanel.setBackground(Color.LIGHT_GRAY);
        JLabel passwordLabel = new JLabel("Enter Admin Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            try {
                DatabaseManager dbManager = new DatabaseManager();
                if (dbManager.verifyAdmin(admin.getUserName(), password)) {
                    cardLayout.show(cards, "adminPanel");
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid password.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error verifying admin: " + ex.getMessage());
            }
            passwordField.setText("");
        });
        adminLoginPanel.add(Box.createVerticalGlue());
        adminLoginPanel.add(passwordLabel);
        adminLoginPanel.add(passwordField);
        adminLoginPanel.add(loginButton);
        adminLoginPanel.add(Box.createVerticalGlue());
        cards.add(adminLoginPanel, "adminLogin");

        // Admin Panel
        adminPanel = new JPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));
        adminPanel.setBackground(Color.LIGHT_GRAY);
        JLabel addCandidateLabel = new JLabel("Add Candidate");
        JTextField nameField = new JTextField(20);
        JTextField symbolField = new JTextField(20);
        JTextField partyField = new JTextField(20);
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String symbol = symbolField.getText().trim();
            String party = partyField.getText().trim();
            if (!name.isEmpty() && !symbol.isEmpty() && !party.isEmpty()) {
                admin.addCandidate(evm, name, symbol, party);
                nameField.setText("");
                symbolField.setText("");
                partyField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill all fields.");
            }
        });
        JLabel removeCandidateLabel = new JLabel("Remove Candidate");
        JTextField removeNameField = new JTextField(20);
        JTextField removePartyField = new JTextField(20);
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            String name = removeNameField.getText().trim();
            String party = removePartyField.getText().trim();
            if (!name.isEmpty() && !party.isEmpty()) {
                admin.removeCandidate(evm, name, party);
                removeNameField.setText("");
                removePartyField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill all fields.");
            }
        });
        JButton viewResultsButton = new JButton("View Results");
        viewResultsButton.addActionListener(e -> {
            updateResultsPanel();
            cardLayout.show(cards, "resultsPanel");
        });
        JButton exitAdminButton = new JButton("Exit Admin Mode");
        exitAdminButton.addActionListener(e -> cardLayout.show(cards, "mainMenu"));
        adminPanel.add(addCandidateLabel);
        adminPanel.add(new JLabel("Name:"));
        adminPanel.add(nameField);
        adminPanel.add(new JLabel("Symbol:"));
        adminPanel.add(symbolField);
        adminPanel.add(new JLabel("Party:"));
        adminPanel.add(partyField);
        adminPanel.add(addButton);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(removeCandidateLabel);
        adminPanel.add(new JLabel("Name:"));
        adminPanel.add(removeNameField);
        adminPanel.add(new JLabel("Party:"));
        adminPanel.add(removePartyField);
        adminPanel.add(removeButton);
        adminPanel.add(viewResultsButton);
        adminPanel.add(exitAdminButton);
        cards.add(adminPanel, "adminPanel");

        // Voter Verification Panel
        voterVerificationPanel = new JPanel();
        voterVerificationPanel.setLayout(new BoxLayout(voterVerificationPanel, BoxLayout.Y_AXIS));
        voterVerificationPanel.setBackground(Color.LIGHT_GRAY);
        JLabel voterIdLabel = new JLabel("Enter Voter ID:");
        JTextField voterIdField = new JTextField(20);
        JButton verifyButton = new JButton("Verify");
        verifyButton.addActionListener(e -> {
            String voterId = voterIdField.getText().trim();
            Voter voter = ec.getVoterList().get(voterId);
            if (voter != null) {
                ElectionOfficer officer = new ElectionOfficer("Officer1");
                if (officer.verifyVoter(voter)) {
                    currentVoter = voter;
                    updateVotingPanel();
                    cardLayout.show(cards, "votingPanel");
                } else {
                    JOptionPane.showMessageDialog(frame, "Voter verification failed.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Voter not found.");
            }
            voterIdField.setText("");
        });
        voterVerificationPanel.add(Box.createVerticalGlue());
        voterVerificationPanel.add(voterIdLabel);
        voterVerificationPanel.add(voterIdField);
        voterVerificationPanel.add(verifyButton);
        voterVerificationPanel.add(Box.createVerticalGlue());
        cards.add(voterVerificationPanel, "voterVerification");

        // Voting Panel
        votingPanel = new JPanel();
        votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));
        votingPanel.setBackground(Color.LIGHT_GRAY);
        cards.add(votingPanel, "votingPanel");

        // Election Commission Panel
        ecPanel = new JPanel();
        ecPanel.setLayout(new BoxLayout(ecPanel, BoxLayout.Y_AXIS));
        ecPanel.setBackground(Color.LIGHT_GRAY);
        JLabel registerLabel = new JLabel("Register Voter");
        JTextField regNameField = new JTextField(20);
        JTextField regAgeField = new JTextField(20);
        JTextField regAadhaarField = new JTextField(20);
        JTextField regAddressField = new JTextField(20);
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            String name = regNameField.getText().trim();
            String ageText = regAgeField.getText().trim();
            String aadhaar = regAadhaarField.getText().trim();
            String address = regAddressField.getText().trim();
            try {
                int age = Integer.parseInt(ageText);
                if (!name.isEmpty() && !aadhaar.isEmpty() && !address.isEmpty()) {
                    ec.registerVoter(name, age, aadhaar, address);
                    regNameField.setText("");
                    regAgeField.setText("");
                    regAadhaarField.setText("");
                    regAddressField.setText("");
                    JOptionPane.showMessageDialog(frame, "Voter registered successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill all fields.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid age.");
            }
        });
        JLabel removeLabel = new JLabel("Remove Voter");
        JTextField remNameField = new JTextField(20);
        JTextField remAadhaarField = new JTextField(20);
        String[] reasons = {"Duplicate Entry", "Voter's Demise", "Change of Address", "Disqualification", "Non-existent", "NRI Citizenship"};
        JComboBox<String> reasonComboBox = new JComboBox<>(reasons);
        JButton removeEcButton = new JButton("Remove");
        removeEcButton.addActionListener(e -> {
            String name = remNameField.getText().trim();
            String aadhaar = remAadhaarField.getText().trim();
            int reason = reasonComboBox.getSelectedIndex() + 1;
            if (!name.isEmpty() && !aadhaar.isEmpty()) {
                ec.removeVoter(name, aadhaar, reason);
                remNameField.setText("");
                remAadhaarField.setText("");
                JOptionPane.showMessageDialog(frame, "Voter removed successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill all fields.");
            }
        });
        JButton viewVotersButton = new JButton("View All Voters");
        viewVotersButton.addActionListener(e -> {
            try {
                DatabaseManager dbManager = new DatabaseManager();
                List<Voter> voters = dbManager.getVoters(EVM.constituency);
                StringBuilder sb = new StringBuilder();
                if (voters.isEmpty()) {
                    sb.append("No voters registered.");
                } else {
                    for (Voter v : voters) {
                        sb.append("ID: ").append(v.getVoterID())
                          .append(", Name: ").append(v.getUserName())
                          .append(", Age: ").append(v.getVoterAge())
                          .append(", Aadhaar: ").append(v.getAadhaarNumber())
                          .append(", Address: ").append(v.getAddress())
                          .append("\n");
                    }
                }
                JOptionPane.showMessageDialog(frame, sb.toString(), "Registered Voters", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage());
            }
        });
        JButton exitEcButton = new JButton("Exit EC Mode");
        exitEcButton.addActionListener(e -> cardLayout.show(cards, "mainMenu"));
        ecPanel.add(registerLabel);
        ecPanel.add(new JLabel("Name:"));
        ecPanel.add(regNameField);
        ecPanel.add(new JLabel("Age:"));
        ecPanel.add(regAgeField);
        ecPanel.add(new JLabel("Aadhaar:"));
        ecPanel.add(regAadhaarField);
        ecPanel.add(new JLabel("Address:"));
        ecPanel.add(regAddressField);
        ecPanel.add(registerButton);
        ecPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        ecPanel.add(removeLabel);
        ecPanel.add(new JLabel("Name:"));
        ecPanel.add(remNameField);
        ecPanel.add(new JLabel("Aadhaar:"));
        ecPanel.add(remAadhaarField);
        ecPanel.add(new JLabel("Reason:"));
        ecPanel.add(reasonComboBox);
        ecPanel.add(removeEcButton);
        ecPanel.add(viewVotersButton);
        ecPanel.add(exitEcButton);
        cards.add(ecPanel, "ecPanel");

        // Results Panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BorderLayout());
        resultsPanel.setBackground(Color.LIGHT_GRAY);
        resultsTextArea = new JTextArea(10, 40);
        resultsTextArea.setEditable(false);
        resultsPanel.add(new JScrollPane(resultsTextArea), BorderLayout.CENTER);
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> cardLayout.show(cards, "mainMenu"));
        resultsPanel.add(backButton, BorderLayout.SOUTH);
        cards.add(resultsPanel, "resultsPanel");

        frame.setVisible(true);
        cardLayout.show(cards, "welcome");
    }

    private void initializeData(DatabaseManager dbManager) throws SQLException
    {
        dbManager.initializeAdmin("Admin1", "VUadmin@123", "STAFF100");

        // Add predefined candidates
        admin.addCandidate(evm, "Sowymya Reddy", "Hand", "Indian National Congress");
        admin.addCandidate(evm, "Arun Prasad A", "Elephant", "Bahujan Samaj Party");
        admin.addCandidate(evm, "Tejasvi Surya", "Lotus", "Bharatiya Janata Party");
        admin.addCandidate(evm, "Tintisha Hemachandra Sagar", "---", "Independent");

        // Add predefined voters
        ec.registerVoter("Ramesh Kumar", 35, "123456789012", "Jayanagar, Bengaluru South");
        ec.registerVoter("Priya Sharma", 28, "987654321098", "Basavanagudi, Bengaluru South");
        ec.registerVoter("Anil Gowda", 45, "456789123456", "Koramangala, Bengaluru South");
        ec.registerVoter("Lakshmi Devi", 50, "789123456789", "Banashankari, Bengaluru South");
        ec.registerVoter("Suresh Patil", 32, "321654987123", "JP Nagar, Bengaluru South");
        ec.registerVoter("Meena Rao", 40, "654987321456", "BTM Layout, Bengaluru South");
        ec.registerVoter("Kiran Shetty", 29, "147258369012", "Vijayanagar, Bengaluru South");
        ec.registerVoter("Vikram Singh", 38, "258369147012", "Mysore City, Mysore");
        ec.registerVoter("Asha Nair", 33, "369147258012", "Hubli Central, Hubli");
        ec.registerVoter("Rahul Menon", 47, "741852963012", "Mangalore North, Mangalore");

        // Sync evm candidate list with database
        List<Candidate> dbCandidates = dbManager.getCandidates(EVM.constituency);
        evm.getCandidateList().clear();
        evm.getCandidateList().addAll(dbCandidates);
    }

    private void updateVotingPanel()
    {
        votingPanel.removeAll();
        JLabel selectLabel = new JLabel("Select Candidate to Vote For:");
        selectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        votingPanel.add(selectLabel);
        try {
            DatabaseManager dbManager = new DatabaseManager();
            List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No candidates available to vote for.");
                cardLayout.show(cards, "mainMenu");
                return;
            }
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                JButton voteButton = new JButton(c.getCandidateName() + " (" + c.getCandidateParty() + ") - " + c.getCandidateSymbol());
                voteButton.setMaximumSize(new Dimension(400, 30));
                voteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                final int index = i + 1;
                voteButton.addActionListener(e -> {
                    try {
                        if (currentVoter.getVoteStatus()) {
                            JOptionPane.showMessageDialog(frame, "You have already voted. Duplicate voting is not allowed.");
                            cardLayout.show(cards, "mainMenu");
                            return;
                        }
                        // Simulate EVM and VVPAT actions
                        boolean voteSuccess = evm.castVote(index);
                        if (!voteSuccess) {
                            JOptionPane.showMessageDialog(frame, "Invalid vote. Please try again.");
                            return;
                        }
                        // Display red light
                        JOptionPane.showMessageDialog(frame, "🚨🚨🚨🚨 Red light ON. Beep! 🚨🚨🚨🚨", "EVM Status", JOptionPane.INFORMATION_MESSAGE);
                        Candidate selected = candidates.get(index - 1);
                        // Generate slip
                        StringBuilder slip = new StringBuilder("Generating Slip of Confirmation...\n");
                        LocalDateTime dtime = LocalDateTime.now();
                        slip.append("Candidate Name: ").append(selected.getCandidateName()).append("\n")
                            .append("Candidate Party: ").append(selected.getCandidateParty()).append("\n")
                            .append("Candidate Party Symbol: ").append(selected.getCandidateSymbol()).append("\n")
                            .append("Generated on: ").append(dtime);
                        JOptionPane.showMessageDialog(frame, slip.toString(), "VVPAT Slip", JOptionPane.INFORMATION_MESSAGE);
                        // Show slip
                        String showSlip = "Candidate: " + selected.getCandidateName() + ", Party: " + selected.getCandidateParty() + ", Timestamp: " + dtime;
                        JOptionPane.showMessageDialog(frame, showSlip, "VVPAT Display", JOptionPane.INFORMATION_MESSAGE);
                        // Print slip
                        StringBuilder printSlip = new StringBuilder("Printing the slip...\n");
                        printSlip.append("Candidate: ").append(selected.getCandidateName()).append(", Party: ").append(selected.getCandidateParty()).append(", Timestamp: ").append(dtime).append("\n")
                                 .append("Dropping slip into box...\n")
                                 .append("Vote placed in secure box.");
                        JOptionPane.showMessageDialog(frame, printSlip.toString(), "VVPAT Print", JOptionPane.INFORMATION_MESSAGE);
                        // Update database
                        currentVoter.setVoterDbId(currentVoter.getVoterDbId()); // Ensure voterDbId is set
                        dbManager.addVote(currentVoter.getVoterDbId(), selected.getCandidateId(), EVM.constituency);
                        dbManager.updateVoterStatus(currentVoter.getVoterDbId(), true);
                        currentVoter.castVote(evm, vvpat, index); // Update in-memory status
                        JOptionPane.showMessageDialog(frame, "Vote cast successfully!");
                        cardLayout.show(cards, "mainMenu");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage());
                    }
                });
                votingPanel.add(voteButton);
                votingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            JButton exitButton = new JButton("Exit Voting");
            exitButton.setMaximumSize(new Dimension(400, 30));
            exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            exitButton.addActionListener(e -> cardLayout.show(cards, "mainMenu"));
            votingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            votingPanel.add(exitButton);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
        votingPanel.revalidate();
        votingPanel.repaint();
    }

    private void updateResultsPanel()
    {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            List<Candidate> candidates = dbManager.getCandidates(EVM.constituency);
            StringBuilder sb = new StringBuilder("--- Election Results ---\n");
            if (candidates.isEmpty()) {
                sb.append("No candidates registered.");
            } else {
                for (Candidate c : candidates) {
                    sb.append(c.getCandidateName()).append(" (")
                      .append(c.getCandidateParty()).append(") (")
                      .append(c.getCandidateSymbol()).append("): ")
                      .append(c.getVoteCount()).append(" total votes\n");
                }
            }
            resultsTextArea.setText(sb.toString());
        } catch (SQLException e) {
            resultsTextArea.setText("Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        try
        {
            ElectionSystem system = new ElectionSystem();
            system.admin = new Admin("Admin1", "VUadmin@123");
            system.evm = new EVM();
            system.vvpat = new VVPAT();
            system.startGUI();
        }
        catch (Exception e)
        {
            System.out.println("Error starting GUI: " + e.getMessage());
        }
    }
}