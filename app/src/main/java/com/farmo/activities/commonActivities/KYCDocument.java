package com.farmo.activities.commonActivities;

import android.net.Uri;

public class KYCDocument {

    private String documentType;
    private String documentNumber;
    private Uri frontImageUri;
    private Uri backImageUri;
    private Uri selfieImageUri;   // ✅ Added selfie URI
    private String status;
    private long submittedTimestamp;

    public KYCDocument() {
        this.status = "PENDING";
        this.submittedTimestamp = System.currentTimeMillis();
    }

    public KYCDocument(String documentType, String documentNumber,
                       Uri frontImageUri, Uri backImageUri, Uri selfieImageUri) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.frontImageUri = frontImageUri;
        this.backImageUri = backImageUri;
        this.selfieImageUri = selfieImageUri;
        this.status = "PENDING";
        this.submittedTimestamp = System.currentTimeMillis();
    }

    // ✅ Simplified validation — no size check (sizes removed, quality kept full)
    public boolean isValid() {
        return documentType != null && !documentType.isEmpty()
                && documentNumber != null && documentNumber.length() >= 5
                && frontImageUri != null;
    }

    public String getDocumentType()   { return documentType; }
    public void setDocumentType(String v) { this.documentType = v; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String v) { this.documentNumber = v; }

    public Uri getFrontImageUri()     { return frontImageUri; }
    public void setFrontImageUri(Uri v) { this.frontImageUri = v; }

    public Uri getBackImageUri()      { return backImageUri; }
    public void setBackImageUri(Uri v) { this.backImageUri = v; }

    public Uri getSelfieImageUri()    { return selfieImageUri; }
    public void setSelfieImageUri(Uri v) { this.selfieImageUri = v; }

    public String getStatus()         { return status; }
    public void setStatus(String v)   { this.status = v; }

    public long getSubmittedTimestamp() { return submittedTimestamp; }

    @Override
    public String toString() {
        return "KYCDocument{type=" + documentType + ", number=" + documentNumber
                + ", status=" + status + "}";
    }
}