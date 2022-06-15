package org.zoolu.sip.message;

import java.util.List;

public class MessageProtocolMIME {

    String contentType;
    String boundary;
    List<sectionProtocolMIME> sections;
    public MessageProtocolMIME(String contentType, String boundary,
                       List<sectionProtocolMIME> sections) {
        this.contentType = contentType;
        this.boundary = boundary;
        this.sections= sections;
    }


    @Override
    public String toString() {
        String mime = ""; //no need to introduce CRLF, it's introduced in the method 'setBody' of 'BaseMessage.java'
        for(sectionProtocolMIME section: sections) {
            mime += "--" + boundary;
            mime += section.toString();
        }
        mime += "\r\n--" + boundary + "--\r\n";//last boundary indicating end of MIME message
        return mime;
    }

}
