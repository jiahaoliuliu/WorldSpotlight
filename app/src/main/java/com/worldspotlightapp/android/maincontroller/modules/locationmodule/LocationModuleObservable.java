package com.worldspotlightapp.android.maincontroller.modules.locationmodule;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.worldspotlightapp.android.utils.Secret;

/**
 * Created by jiahaoliuliu on 15/9/19.
 */
public class LocationModuleObservable extends AbstractLocationModuleObservable {

    /*
     * Establish an OAuth connection to a MasterCard API over HTTPS.
     * @param httpsURL The full URL to call, including any querystring parameters.
     * @param body The body to include.  If this has a body, an HTTP POST will be established,
     * 			   this content will be used to generate the oauth_body_hash and the contents passed
     * 			   as the body of the request.  If the body parameter is null, an HTTP GET
     *             will be established.
     */
    private HttpsURLConnection createOpenAPIConnection(String httpsURL, String body) throws NoSuchAlgorithmException, InvalidKeySpecException,
            IOException, OAuthException, KeyStoreException, CertificateException, UnrecoverableKeyException,
            KeyManagementException {
        HttpsURLConnection con = null;
        PrivateKey privKey = getPrivateKey();
        if (privKey != null) {
            OAuthRsaSha1Signer rsaSigner = new OAuthRsaSha1Signer();
            OAuthParameters params = new OAuthParameters();
            params.setOAuthConsumerKey(Secret.MASTER_CARD_API_CONSUMER_KEY);
            params.setOAuthNonce(OAuthUtil.getNonce());
            params.setOAuthTimestamp(OAuthUtil.getTimestamp());
            params.setOAuthSignatureMethod("RSA-SHA1");
            params.setOAuthType(OAuthParameters.OAuthType.TWO_LEGGED_OAUTH);
            params.addCustomBaseParameter("oauth_version", "1.0");
            rsaSigner.setPrivateKey(privKey);

            String method = "GET";
            if (body != null) {
                method = "POST";

                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.reset();
                byte[] hash = digest.digest(body.getBytes("UTF-8"));
                String encodedHash = Base64.encodeToString(hash, Base64.DEFAULT);

                params.addCustomBaseParameter("oauth_body_hash", encodedHash);
            }

            String baseString = OAuthUtil.getSignatureBaseString(httpsURL, method, params.getBaseParameters());
            System.out.println(baseString);

            String signature = rsaSigner.getSignature(baseString, params);

            params.addCustomBaseParameter("oauth_signature", signature);

            URL url = new URL(httpsURL);
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            con.setDoOutput(true);
            con.setDoInput(true);
            con.addRequestProperty("Authorization",	buildAuthHeaderString(params));
            System.out.println(buildAuthHeaderString(params));

            if (body != null) {
                con.addRequestProperty("content-type", "application/xml; charset=UTF-8");
                con.addRequestProperty("content-length", Integer.toString(body.length()));
            }
            con.connect();

            if (body != null) {
                CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                OutputStreamWriter request = new OutputStreamWriter(con.getOutputStream(), encoder);
                request.append(body);
                request.flush();
                request.close();
            }
        }
        return con;
    }

    /*
 * Pulls the private key out of a PEM file and loads it into an RSAPrivateKey and returns it.
 */
    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privKeyFile = "openapi-samplecode-privatekey.pem";
        final String beginPK = "-----BEGIN PRIVATE KEY-----";
        final String endPK = "-----END PRIVATE KEY-----";

        // read private key PEM file
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream(privKeyFile);
        java.io.DataInputStream dis = new java.io.DataInputStream(stream);
        byte[] privKeyBytes = new byte[(int) stream.available()];
        dis.readFully(privKeyBytes);
        dis.close();
        String privKeyStr = new String(privKeyBytes, "UTF-8");

        int startIndex = privKeyStr.indexOf(beginPK);
        int endIndex = privKeyStr.indexOf(endPK);

        privKeyStr = privKeyStr.substring(startIndex + beginPK.length(), endIndex);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // decode private key. Check if it works
//        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec((new Base64()).decodeBuffer(privKeyStr));
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec((Base64.decode(privKeyStr, Base64.DEFAULT)));
        RSAPrivateKey privKey = (RSAPrivateKey)keyFactory.generatePrivate(privSpec);
        return privKey;
    }

//    protected PrivateKey getPrivateKey()
//            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException,
//            CertificateException, UnrecoverableEntryException {
//
//        String kspw = "replaceme";
//        String privKeyFile = "MCOpenAPI.p12";
//        String keyAlias = "mckp";
//
//        KeyStore ks = KeyStore.getInstance("PKCS12");
//
//        // get user password and file input stream
//        ClassLoader cl = this.getClass().getClassLoader();
//        InputStream stream = cl.getResourceAsStream(privKeyFile);
//        ks.load(stream, kspw.toCharArray());
//        Key key = ks.getKey(keyAlias, kspw.toCharArray());
//
//        return (PrivateKey) key;
//    }

    private String buildAuthHeaderString(OAuthParameters params) {
        StringBuffer buffer = new StringBuffer();
        int cnt = 0;
        buffer.append("OAuth ");
        Map<String, String> paramMap = params.getBaseParameters();
        Object[] paramNames = paramMap.keySet().toArray();
        for (Object paramName : paramNames) {
            String value = paramMap.get((String) paramName);
            buffer.append(paramName + "=\"" + OAuthUtil.encode(value) + "\"");
            cnt++;
            if (paramNames.length > cnt) {
                buffer.append(",");
            }

        }
        return buffer.toString();
    }
}
