import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

/** Publish only the public certificate and raw SHA256withRSA detached signatures. */
final class ReleaseSignature {
    public static void main(String[] args) throws Exception {
        if(args.length<3) throw new IllegalArgumentException("sign keystore cert.pem artifact... | verify cert.pem artifact");
        if(args[0].equals("sign") && args.length>=4) {
            char[] password=Objects.requireNonNull(System.getenv("DUFS_KEYSTORE_PASS")).toCharArray();
            try {
                KeyStore store=KeyStore.getInstance("PKCS12");
                try(var input=Files.newInputStream(Path.of(args[1]))) {store.load(input,password);}
                var cert=(X509Certificate)store.getCertificate("dufs");
                var key=(PrivateKey)store.getKey("dufs",password);
                String encoded=Base64.getMimeEncoder(64,new byte[]{10}).encodeToString(cert.getEncoded());
                Files.writeString(Path.of(args[2]),"-----BEGIN CERTIFICATE-----\n"+encoded+"\n-----END CERTIFICATE-----\n",StandardCharsets.US_ASCII);
                for(int i=3;i<args.length;i++) {
                    Path artifact=Path.of(args[i]); byte[] data=Files.readAllBytes(artifact);
                    Signature signing=Signature.getInstance("SHA256withRSA"); signing.initSign(key); signing.update(data);
                    byte[] sig=signing.sign();
                    if(!verify(cert,data,sig)) throw new GeneralSecurityException("Signature self-check failed");
                    data[0]^=1;
                    if(verify(cert,data,sig)) throw new GeneralSecurityException("Tampering not detected");
                    Files.write(Path.of(args[i]+".sig"),sig);
                }
            } finally {Arrays.fill(password,'\0');}
        } else if(args[0].equals("verify") && args.length==3) {
            X509Certificate cert;
            try(var in=Files.newInputStream(Path.of(args[1]))) {cert=(X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(in);}
            if(!verify(cert,Files.readAllBytes(Path.of(args[2])),Files.readAllBytes(Path.of(args[2]+".sig")))) throw new GeneralSecurityException("Invalid detached signature");
            System.out.println("Verified SHA256withRSA: "+args[2]);
        } else throw new IllegalArgumentException("Unrecognized command");
    }
    private static boolean verify(X509Certificate cert,byte[] data,byte[] signature) throws Exception {
        Signature verifier=Signature.getInstance("SHA256withRSA"); verifier.initVerify(cert.getPublicKey()); verifier.update(data); return verifier.verify(signature);
    }
}
