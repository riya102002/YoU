package com.utkarsh.you;

import com.google.firebase.database.FirebaseDatabase;

public class MsgModelClass {
    private String message;
    private String senderId;
    private String messageId; // Unique ID for the message
    private long timestamp;

    // Default constructor for Firebase
    public MsgModelClass() {
    }

    public MsgModelClass(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        // Assuming messageId is generated at the time of creation
        this.messageId = FirebaseDatabase.getInstance().getReference().push().getKey();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
