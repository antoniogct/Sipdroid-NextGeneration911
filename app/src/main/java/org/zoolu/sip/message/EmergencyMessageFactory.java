package org.zoolu.sip.message;

import android.annotation.SuppressLint;
import android.util.Log;


import org.sipdroid.sipua.ui.Sipdroid;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.AcceptContactHeader;
import org.zoolu.sip.header.CSeqHeader;
import org.zoolu.sip.header.CallIdHeader;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.ContentTypeHeader;
import org.zoolu.sip.header.DateHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.FromHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.SipHeaders;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.header.UserAgentHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Random;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import android.location.Location;








public class EmergencyMessageFactory extends BaseMessageFactory {

    double latitude;
    double longitude;

    @SuppressLint("LongLogTag")
    public static Message createInviteRequestEmergency(String call_id, SipProvider sip_provider,
                                                       SipURL request_uri, NameAddress to,
                                                       NameAddress from,
                                                       NameAddress contact, String body, String icsi, String location) {
        long cseq = SipProvider.pickInitialCSeq();

        Log.i("location", "Create invite request emergency reached");

        String local_tag = SipProvider.pickTag();

        if (contact == null)
            contact = from;


        String method = SipMethods.INVITE;
        String remote_tag = null;


        String branch = null;
        String via_addr = sip_provider.getViaAddress();
        int host_port = sip_provider.getPort();

        boolean rport = sip_provider.isRportSet();
        String proto;
        if (request_uri.hasTransport())
            proto = request_uri.getTransport();
        else
            proto = sip_provider.getDefaultTransport();
        String qvalue = null;


        Message req = new Message();
        req.setRequestLine(new RequestLine(method, request_uri));
        ViaHeader via = new ViaHeader(proto, via_addr, host_port);
        if (rport)
            via.setRport();
        if (branch == null)
            branch = SipProvider.pickBranch();
        via.setBranch(branch);

        req.addViaHeader(via);

        req.setMaxForwardsHeader(new MaxForwardsHeader(70));
        if (remote_tag == null)
            req.setToHeader(new ToHeader(to));
        else
            req.setToHeader(new ToHeader(to, remote_tag));
        req.setFromHeader(new FromHeader(from, local_tag));

        req.setCallIdHeader(new CallIdHeader(call_id));
        req.setCSeqHeader(new CSeqHeader(cseq, method));


        if (contact != null) {
            if (((method == "REGISTER")||(method == "INVITE")) && (icsi != null) ){
                MultipleHeader contacts = new MultipleHeader(SipHeaders.Contact);
                contacts.addBottom(new ContactHeader(contact, qvalue, icsi));
                req.setContacts(contacts);
            }
            else{
                MultipleHeader contacts = new MultipleHeader(SipHeaders.Contact);
                contacts.addBottom(new ContactHeader(contact));
                req.setContacts(contacts);
            }

        }
        if ((method == "INVITE") && (icsi != null) ){
            req.setAcceptContactHeader(new AcceptContactHeader(icsi));
        }
        req.setExpiresHeader(new ExpiresHeader(String
                .valueOf(SipStack.default_expires)));
        if (SipStack.ua_info != null)
            req.setUserAgentHeader(new UserAgentHeader(SipStack.ua_info));
        String from_uri = from.getAddress().toString().substring(4);

        //Geolocation headers

        Header geolocation = new Header("Geolocation", "<cid:"+from_uri+">");
        Header geolocationRouting = new Header("Geolocation-Routing", "no");
        req.setHeader(geolocation);
        req.setHeader(geolocationRouting);
        String randomString = Random.nextString(50);
        String boundary = "EmergencyCall" + randomString;
        String mimeType = "multipart/mixed";

        //MIME SDP part
        sectionProtocolMIME sdpMIME = new sectionProtocolMIME(call_id, "application/sdp", body);

        //Call the method that return mac address
        String mobile_mac_address = getMacAddress();  //call the method that return mac address

        //Creation of location XML

        String locationXML = "";
        locationXML += "\r\n<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
        locationXML += "\r\n<presence";
        locationXML += "\r\n\t\txmlns=\"urn:ietf:params:xml:ns:pidf\"";
        locationXML += "\r\n\t\txmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"";
        locationXML += "\r\n\t\txmlns:gbp=\"urn:ietf:params:xml:ns:pidf:geopriv10:basicPolicy\"";
        locationXML += "\r\n\t\txmlns:gml=\"http://www.opengis.net/gml\"";
        locationXML += "\r\n\t\txmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\"";
        locationXML += "\r\n\t\tentity=\"sip:" + from + "\">";
        locationXML += "\r\n\t<dm:device id=\"" + from +"-1\">";
        locationXML += "\r\n\t\t<gp:geopriv>";
        locationXML += "\r\n\t\t\t<gp:location-info>";
        locationXML += "\r\n\t\t\t\t<gml:location>";
        locationXML += "\r\n\t\t\t\t\t<gml:Point srsName=\"urn:ogc:def:crs:EPSG::4326\">";
        locationXML += "\r\n\t\t\t\t\t\t<gml:pos>" + location + "</gml:pos>";
        locationXML += "\r\n\t\t\t\t\t</gml:Point>";
        locationXML += "\r\n\t\t\t\t</gml:location>";
        locationXML += "\r\n\t\t\t</gp:location-info>";
        locationXML += "\r\n\t\t\t<gp:usage-rules>";
        locationXML += "\r\n\t\t\t</gp:usage-rules>";
        locationXML += "\r\n\t\t\t<gp:method>GPS</gp:method>";
        locationXML += "\r\n\t\t</gp:geopriv>";
        locationXML += "\r\n\t\t<dm:deviceID>mac:" + mobile_mac_address + "</dm:deviceID>";
        locationXML += "\r\n\t\t<dm:timestamp>" + new SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZ").format(new Date()) + "</dm:timestamp>";
        locationXML += "\r\n\t</dm:device>";
        locationXML += "\r\n</presence>";


        //MIME XML part with the location
        sectionProtocolMIME locationMIME = new sectionProtocolMIME(from_uri, "application/pidf+xml",locationXML);

        //Union of the two parts of the MIME
        List<sectionProtocolMIME> content = new ArrayList<sectionProtocolMIME>();
        content.add(sdpMIME);
        content.add(locationMIME);

        MessageProtocolMIME mimeBody = new MessageProtocolMIME(mimeType, boundary, content);
        req.setBody(mimeType + "; boundary=" + boundary + "", mimeBody.toString());


        Log.i("Emergency INVITE message", req.toString());

        return req;
    }




    public void onLocationChanged(Location location) {
        //locationText.setText("Current Location: " + location.getLatitude() + ", " + location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    // Method used to obtain the MAC address of the Android device

    public static String getMacAddress(){
        try{
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            String stringMac = "";
            for(NetworkInterface networkInterface : networkInterfaceList)
            {
                if(networkInterface.getName().equalsIgnoreCase("wlon0"));
                {
                    for(int i = 0 ;i <networkInterface.getHardwareAddress().length; i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i]& 0xFF);
                        if(stringMacByte.length() == 1)
                        {
                            stringMacByte = "0" +stringMacByte;
                        }
                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }
            }
            return stringMac;
        }catch (SocketException e)
        {
            e.printStackTrace();
        }
        return  "0";
    }


}
