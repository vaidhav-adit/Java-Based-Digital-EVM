package Users;

import System.DatabaseManager;
import System.EVM;
import exceptions.VerifyVoterException;
import java.sql.SQLException;

public class ElectionOfficer extends ElectionStaff implements ElectionRole {
    private String officerName;

    public ElectionOfficer(String officerName) {
        super(officerName);
        this.officerName = officerName;
    }

    public String getOfficerName() {
        return officerName;
    }

    public boolean verifyVoter(Voter voter) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            Voter dbVoter = dbManager.getVoter(voter.getAadhaarNumber());
            if (dbVoter == null) {
                throw new VerifyVoterException("Voter not found in database!");
            }
            if (dbVoter.getVoteStatus()) {
                throw new VerifyVoterException("Voter has already cast their vote!");
            }
            voter.setVoterDbId(dbVoter.getVoterDbId());
            return true;
        } catch (VerifyVoterException e) {
            System.out.println("Exception Caught: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void accessSystem(EVM evm, java.util.Scanner scan) {
        System.out.println("Election Officer can verify voters and manage the voting process.");
        System.out.print("Enter voter's Aadhaar number: ");
        String aadhaarNumber = scan.nextLine();
        Voter voter = new Voter("", aadhaarNumber);
        if (verifyVoter(voter)) {
            System.out.println("Voter verified successfully!");
            voter.accessSystem(evm, scan);
        }
    }
} 