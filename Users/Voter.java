package Users;

import System.DatabaseManager;
import System.EVM;
import System.VVPAT;
import exceptions.InvalidVoteException;
import java.sql.SQLException;

public class Voter extends User implements ElectionRole {
    private String voterID;
    private static int vIDcounter = 100;
    private boolean hasVoted;
    private int voterAge;
    private String aadhaarNumber;
    private String address;
    private int voterDbId;

    public Voter(String name, int voterAge, String aadhaarNumber, String address) {
        super(name);
        this.voterID = "VOTER" + vIDcounter++;
        this.voterAge = voterAge;
        this.aadhaarNumber = aadhaarNumber;
        this.address = address;
        this.hasVoted = false;
    }

    public Voter(String name, String aadhaarNumber) {
        super(name);
        this.aadhaarNumber = aadhaarNumber;
    }

    public void castVote(EVM evm, VVPAT vvpat, java.util.Scanner scan) {
        try {
            if (hasVoted) {
                throw new InvalidVoteException("You have already cast your vote!");
            }
            evm.displayCandidates();
            System.out.print("Enter the number of your chosen candidate: ");
            int choice = scan.nextInt();
            if (evm.castVote(choice)) {
                vvpat.generateSlip(evm.getCandidateList().get(choice - 1));
                vvpat.showSlip(evm.getCandidateList().get(choice - 1));
                vvpat.printSlip(evm.getCandidateList().get(choice - 1));
                hasVoted = true;
                DatabaseManager dbManager = new DatabaseManager();
                dbManager.updateVoterStatus(voterDbId, true);
                dbManager.addVote(voterDbId, EVM.constituency);
                System.out.println("Vote cast successfully!");
            } else {
                throw new InvalidVoteException("Invalid vote choice!");
            }
        } catch (InvalidVoteException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void castVote(EVM evm, VVPAT vvpat, int candidateIndex) {
        try {
            if (hasVoted) {
                throw new InvalidVoteException("You have already cast your vote!");
            }
            if (evm.castVote(candidateIndex)) {
                vvpat.generateSlip(evm.getCandidateList().get(candidateIndex - 1));
                vvpat.showSlip(evm.getCandidateList().get(candidateIndex - 1));
                vvpat.printSlip(evm.getCandidateList().get(candidateIndex - 1));
                hasVoted = true;
                DatabaseManager dbManager = new DatabaseManager();
                dbManager.updateVoterStatus(voterDbId, true);
                dbManager.addVote(voterDbId, EVM.constituency);
            } else {
                throw new InvalidVoteException("Invalid vote choice!");
            }
        } catch (InvalidVoteException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    @Override
    public void accessSystem(EVM evm, java.util.Scanner scan) {
        castVote(evm, new VVPAT(), scan);
    }

    public boolean getVoteStatus() {
        return hasVoted;
    }

    public String getVoterID() {
        return voterID;
    }

    public int getVoterAge() {
        return voterAge;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public String getAddress() {
        return address;
    }

    public int getVoterDbId() {
        return voterDbId;
    }

    public void setVoterDbId(int voterDbId) {
        this.voterDbId = voterDbId;
    }
} 