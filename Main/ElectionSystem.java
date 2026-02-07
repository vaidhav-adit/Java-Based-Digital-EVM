package Main;

import Users.Admin;
import Users.Candidate;
import Users.ElectionOfficer;
import Users.Voter;
import System.EVM;
import System.VVPAT;
import System.ElectionCommission;
import System.DatabaseManager;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


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
                System.out.print("Enter Aadhaar number: ");
                String aadhaar = scan.nextLine();
                Voter voter = ec.getVoterList().get(aadhaar);
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
        JLabel aadhaarLabel = new JLabel("Enter Aadhaar number:");
        JTextField aadhaarField = new JTextField(20);
        JButton verifyButton = new JButton("Verify");
        verifyButton.addActionListener(e -> {
            String aadhaar = aadhaarField.getText().trim();
            Voter voter = ec.getVoterList().get(aadhaar);
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
            aadhaarField.setText("");
        });
        voterVerificationPanel.add(Box.createVerticalGlue());
        voterVerificationPanel.add(aadhaarLabel);
        voterVerificationPanel.add(aadhaarField);
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
                        Candidate selected = candidates.get(index - 1);

                        // Simulate EVM and VVPAT actions (display only, do NOT increment in memory)
                        evm.shineRedLight(); // Red light logic from EVM
                        JOptionPane.showMessageDialog(frame, "🚨🚨🚨🚨 Red light ON. Beep! 🚨🚨🚨🚨", "EVM Status", JOptionPane.INFORMATION_MESSAGE);

                        // VVPAT logic
                        vvpat.generateSlip(selected);
                        vvpat.showSlip(selected);
                        vvpat.printSlip(selected);

                        // Update database (this increments the vote count)
                        dbManager.addVote(selected.getCandidateId(), EVM.constituency);
                        dbManager.updateVoterStatus(currentVoter.getVoterDbId(), true);

                        // Mark voter as voted in memory
                        // currentVoter.castVote(evm, vvpat, index); // REMOVE or comment out this line!

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