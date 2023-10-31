package Models;

import java.util.ArrayList;

public class FundraisingEventMessage {

    public RequestTypeEnum requestType;
    public ArrayList<FundraisingEvent> events;
    public double donation;
    public int arrayIndex;

    public FundraisingEventMessage(){
        
    }

    public FundraisingEventMessage(RequestTypeEnum requestType, ArrayList<FundraisingEvent> events, double donation) {
        this.requestType = requestType;
        this.events = events;
        this.donation = donation;
    }
    public FundraisingEventMessage(RequestTypeEnum requestType, ArrayList<FundraisingEvent> events) {
        this.requestType = requestType;
        this.events = events;
    }

    public FundraisingEventMessage(RequestTypeEnum requestType) {
        this.requestType = requestType;
    }

    public RequestTypeEnum getRequestType() {
        return requestType;
    }

    public ArrayList<FundraisingEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<FundraisingEvent> events) {
        this.events = events;
    }

    public double getDonation() {
        return donation;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setarrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

}