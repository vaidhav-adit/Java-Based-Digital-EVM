package exceptions;

public class InvalidVoteException extends Exception {
    public InvalidVoteException(String message) {
        super(message);
    }
} 