package Users;

public class Candidate {
    private String candidateName;
    private String candidateSymbol;
    private String candidateParty;
    private int countVotes;
    private int candidateId;

    Candidate(String name, String symbol, String candidateParty) {
        this.candidateName = name;
        this.candidateSymbol = symbol;
        this.candidateParty = candidateParty;
        this.countVotes = 0;
    }

    public Candidate(String name, String symbol, String candidateParty, int candidateId) {
        this.candidateName = name;
        this.candidateSymbol = symbol;
        this.candidateParty = candidateParty;
        this.candidateId = candidateId;
        this.countVotes = 0;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getCandidateSymbol() {
        return candidateSymbol;
    }

    public String getCandidateParty() {
        return candidateParty;
    }

    public int getVoteCount() {
        return countVotes;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void addVote() {
        countVotes++;
    }

    public void setVoteCount(int voteCount) {
        this.countVotes = voteCount;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
} 