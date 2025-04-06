package edu.uth.nurseborn.exception;

public class FeedbackNotFoundException extends RuntimeException {
  public FeedbackNotFoundException(String message) {
    super(message);
  }
}
