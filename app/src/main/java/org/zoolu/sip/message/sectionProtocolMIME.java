package org.zoolu.sip.message;



public class sectionProtocolMIME {

    String version;
    String contentID;
    String contentType;
    String contentTransferEncoding;
    String content;
    /**
     * Constructor with all the fields
     *

     * @param contentID
     * @param contentType
     * @param content
     */
    public sectionProtocolMIME(String contentID, String contentType, String content) {
        this.contentID = contentID;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String toString() {
        String part = "";
        part += "\r\nContent-ID: <" + contentID + ">";
        part += "\r\nContent-Type: " + contentType;
        part += "\r\n" + content;
        part += "\r\n";
        return part;
    }

}