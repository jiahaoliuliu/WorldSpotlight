package com.worldspotlightapp.android.maincontroller.modules.locationmodule;

import android.util.Base64;

import com.google.api.client.auth.oauth.OAuthParameters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.parse.signpost.exception.OAuthException;

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
            params.setOAuthConsumerKey(clientId);
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
                String encodedHash = Base64.encode(hash);

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

}
