/**
 * Licensed to the Mifos Initiative under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.mifos.vnext.connector.config;


import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.Base64;


import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.mifos.grpc.proto.vnext.StreamServerInitialResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Getter
@Setter
public class CryptoAndCertHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoAndCertHelper.class);
    
    private final PrivateKey clientPrivateKey;    
    private final X509Certificate serverIntermediateCertificate;
    private final X509Certificate clientSignedCertificate;
    private String serverIntermediatePublicKeyFingerprint;

    public CryptoAndCertHelper(String clientPrivateKeyFilePath, String serverIntermediateCertificatePath, String clientSignedCertificatePath) 
            throws Exception {
        logger.debug("clientPrivateKeyFilePath "+clientPrivateKeyFilePath);
        logger.debug("serverIntermediateCertificatePath "+serverIntermediateCertificatePath);
        logger.debug("clientSignedCertificatePath "+clientSignedCertificatePath);
        // Load private key (PEM -> PrivateKey)
        this.clientPrivateKey = PemUtils.loadPrivateKey(clientPrivateKeyFilePath);
        
        // Load CA intermediate certificate
        try (FileInputStream fis = new FileInputStream(serverIntermediateCertificatePath)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            this.serverIntermediateCertificate = (X509Certificate) factory.generateCertificate(fis);
        }
        
        // Load Client Signed Certificate
        try (FileInputStream fis = new FileInputStream(clientSignedCertificatePath)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            this.clientSignedCertificate = (X509Certificate) factory.generateCertificate(fis);
        }
        
        Security.addProvider(new BouncyCastleProvider());
        
    }

    /**
     * Signs a string using the loaded private key (SHA1withRSA).
     */
    public String signString(StreamServerInitialResponse response) throws Exception {
        if (clientPrivateKey == null) {
            throw new IllegalStateException("Could not find private key");
        }

        try {
            
            // Update digest with the input string (UTF-8 encoded)
            byte[] digest = response.getChallengeNonce().getBytes(StandardCharsets.UTF_8);
            
            // Initialize signature with SHA1withRSA and sign
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(clientPrivateKey);
            signature.update(digest);
            byte[] signedBytes = signature.sign();
            
            // Convert signature bytes -> ISO_8859_1 String
            String signatureIso = new String(signedBytes, StandardCharsets.ISO_8859_1);

            // Encode back into UTF-8 bytes
            byte[] utf8Bytes = signatureIso.getBytes(StandardCharsets.UTF_8);

            // Base64 encode for transport
            return Base64.getEncoder().encodeToString(utf8Bytes);
        } 
        catch (Exception e) {
            throw new RuntimeException("Error signing string", e);
        }
    }    

    /**
     * Validates a signature with Server Intermediate CA public key + fingerprint.
     */
    public boolean validateSignature(String originalString, StreamServerInitialResponse response) {
        try {
            
            PublicKey clientSignedPublicKey = clientSignedCertificate.getPublicKey();
            String calculatedFingerprint = getPublicKeyFingerprint(clientSignedPublicKey);
            this.serverIntermediatePublicKeyFingerprint = calculatedFingerprint;
                    
            if (!calculatedFingerprint.equalsIgnoreCase(response.getPubKeyFingerprint())) {
                return false;
            }
            
            // decode base64, then encode as UTF_8 string (will have 256 lengths), then transform to single byte ISO_8859_1
            byte[] signatureBase64DecodedBytes = Base64.getDecoder().decode(response.getSignedClientId());
            String signatureStr = new String(signatureBase64DecodedBytes, StandardCharsets.UTF_8);
            byte[] signatureBytes = signatureStr.getBytes(StandardCharsets.ISO_8859_1);
            
            // Update digest with the input string (UTF-8 encoded)
            byte[] digest = originalString.getBytes(StandardCharsets.UTF_8);
            
            // Verify signature with SHA1withRSA
            Signature signature = Signature.getInstance("SHA1withRSA");            
            signature.initVerify(serverIntermediateCertificate.getPublicKey());
            signature.update(digest);
            boolean verified = signature.verify(signatureBytes);
            
            return verified;
        } 
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception "+e.getMessage());
            return false;
        }
    }
           
    public String getPublicKeyFingerprint(PublicKey publicKey) throws Exception {
        
        byte[] skiExtension = serverIntermediateCertificate.getExtensionValue("2.5.29.14");
        byte[] skiBytes = null;
        
        if (skiExtension != null) {
            try (ASN1InputStream ais = new ASN1InputStream(skiExtension)) {
                DEROctetString oct = (DEROctetString) ais.readObject();
                try (ASN1InputStream ais2 = new ASN1InputStream(oct.getOctets())) {
                    DEROctetString skiOctet = (DEROctetString) ais2.readObject();
                    skiBytes = skiOctet.getOctets();
                }
            }
        }
        
        StringBuilder hexSki = new StringBuilder();
        
        if (skiBytes != null) {
            for (byte b : skiBytes) {
                hexSki.append(String.format("%02x", b));
            }
        }
        
        String subjectKeyIdentifier = hexSki.toString();
        logger.debug("Subject Key Identifier: " + subjectKeyIdentifier);        
        return subjectKeyIdentifier;
    }    
}