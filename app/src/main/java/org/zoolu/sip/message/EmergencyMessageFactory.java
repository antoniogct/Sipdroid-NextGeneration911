package org.zoolu.sip.message;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;




public class EmergencyMessageFactory extends BaseMessageFactory implements LocationListener {

    LocationManager locationManager;
    double latitude;
    double longitude;

    public static Message createInviteRequestEmergency(String call_id, SipProvider sip_provider,
                                            SipURL request_uri, NameAddress to,
                                            NameAddress from,
                                            NameAddress contact, String body, String icsi, String location) {
        long cseq = SipProvider.pickInitialCSeq();

        Log.i("location", "Create invite request emergency reached");

        String local_tag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
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
        // mandatory headers first (To, From, Via, Max-Forwards, Call-ID, CSeq):
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
        // optional headers:
        // start modification by mandrajg
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
            // System.out.println("DEBUG: Contact: "+contact.toString());
        }
        if ((method == "INVITE") && (icsi != null) ){
            req.setAcceptContactHeader(new AcceptContactHeader(icsi));
        }
        // end modifications by mandrajg
        req.setExpiresHeader(new ExpiresHeader(String
                .valueOf(SipStack.default_expires)));
        // add User-Agent header field
        if (SipStack.ua_info != null)
            req.setUserAgentHeader(new UserAgentHeader(SipStack.ua_info));
        // if (body!=null) req.setBody(body); else req.setBody("");

        String from_uri = from.getAddress().toString().substring(4);


        Header geolocation = new Header("Geolocation", "<cid:"+from_uri+">");
        Header geolocationRouting = new Header("Geolocation-Routing", "no");
        req.setHeader(geolocation);
        req.setHeader(geolocationRouting);

        String randomString = Random.nextString(50);
        String boundary = "EmergencyCall" + randomString; //this is the boundary of the MIME


        String mimeType = "multipart/mixed";
        //SDP part of the MIME body
        sectionProtocolMIME sdpMIME = new sectionProtocolMIME(call_id, "application/sdp", body);

        //Location

        //getLocation();


        //Creation of location XML

        String locationXML = "";
        locationXML += "\r\n<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
        locationXML += "\r\n\t<presence";
        locationXML += "\r\n\t\txmlns=\"urn:ietf:params:xml:ns:pidf\"";
        locationXML += "\r\n\t\txmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"";
        locationXML += "\r\n\t\txmlns:gbp=\"urn:ietf:params:xml:ns:pidf:geopriv10:basicPolicy\"";
        locationXML += "\r\n\t\txmlns:gml=\"http://www.opengis.net/gml\"";
        locationXML += "\r\n\t\txmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\"";
        locationXML += "\r\n\t\tentity=\"sip:" + local_tag + "\">";
        locationXML += "\r\n\t<dm:device id=\"target123-1\">";
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
        locationXML += "\r\n\t\t\t<gp:method>802.11</gp:method>";
        locationXML += "\r\n\t\t</gp:geopriv>";
        locationXML += "\r\n\t\t<dm:deviceID>mac:xxxx</dm:deviceID>";
        locationXML += "\r\n\t\t<dm:timestamp>XXXX-XX-XXTXX:XX:XXZ</dm:timestamp>";
        locationXML += "\r\n\t</dm:device>";
        locationXML += "\r\n</presence>";







        //Location part of the MIME body
        sectionProtocolMIME locationMIME = new sectionProtocolMIME(from_uri, "application/pidf+xml",locationXML);
        //MIME body
        List<sectionProtocolMIME> content = new ArrayList<sectionProtocolMIME>();
        content.add(sdpMIME);
        content.add(locationMIME);
        MessageProtocolMIME mimeBody = new MessageProtocolMIME(mimeType, boundary, content);
        req.setBody(mimeType + "; boundary=" + boundary + "", mimeBody.toString());


        //req.setBody(body);

        Log.i("Geolocation header", req.toString());
        // System.out.println("DEBUG: MessageFactory: request:\n"+req);
        return req;
    }
    /*
    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);

        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

     */



    public void onLocationChanged(Location location) {
        //locationText.setText("Current Location: " + location.getLatitude() + ", " + location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }


    /*public void onProviderDisabled(String provider) {
        Toast.makeText(Sipdroid.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
    */

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    public void onProviderEnabled(String provider) {

    }



}
