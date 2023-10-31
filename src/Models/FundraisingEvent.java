package Models;

public class FundraisingEvent {
    public String eventName;
    public double targetAmount;
    public String deadline;
    public double amountRaised;

    public FundraisingEvent(){}

    public FundraisingEvent(String eventName, double targetAmount, String deadline) {
        this.eventName = eventName;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
    }

    public String getEventName() {
        return this.eventName;
    }

    public double getTargetAmount() {
        return this.targetAmount;
    }

    public String getDeadline() {
        return this.deadline;
    }

    public double getAmountRaised() {
        return this.amountRaised;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setAmountRaised(double amountRaised) {
        this.amountRaised = amountRaised;
    }
}