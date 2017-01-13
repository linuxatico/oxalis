package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.net.MalformedURLException;
import java.net.URL;

public class PeppolEndpointData {

    URL url;

    TransportProfile transportProfile;

    CommonName commonName = null;

    public PeppolEndpointData(URL url, TransportProfile transportProfile) {
        this.url = url;
        this.transportProfile = transportProfile;
    }

    public PeppolEndpointData(URL url, BusDoxProtocol transportProfile, CommonName commonName) {
        this(url, transportProfile.toVefa());
        this.commonName = commonName;
    }

    public PeppolEndpointData(Endpoint endpoint) {
        try {
            url = endpoint.getAddress().toURL();
            transportProfile = BusDoxProtocol.AS2.toVefa();
            commonName = CommonName.valueOf(endpoint.getCertificate().getSubjectX500Principal());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    public URL getUrl() {
        return url;
    }

    public TransportProfile getTransportProfile() {
        return transportProfile;
    }

    /**
     * The CN attribute of the Endpoint's X.509 Distinguished Name
     *
     * @return the value of the CN attribute or <code>null</code> if not set.
     */
    public CommonName getCommonName() {
        return commonName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeppolEndpointData{");
        sb.append("url=").append(url.toExternalForm());
        sb.append(", transportProfile=").append(transportProfile);
        sb.append(", commonName=").append(commonName);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeppolEndpointData that = (PeppolEndpointData) o;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (transportProfile != null ? !transportProfile.equals(that.transportProfile) : that.transportProfile != null)
            return false;
        if (commonName != null ? !commonName.equals(that.commonName) : that.commonName != null) return false;
        return true;
    }

}
