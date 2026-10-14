package de.zumbyte.pzarchitect.update;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/** Offline release tool. Reads an existing private key; never creates or uploads keys. */
public final class ManifestSigner {
    private ManifestSigner() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: ManifestSigner manifest.json PRIVATE-KEY.pem new-envelope.json");
        Path payloadPath = Path.of(args[0]), keyPath = Path.of(args[1]);
        if (Files.size(payloadPath) > 512_000 || Files.size(keyPath) > 16_384) throw new IllegalArgumentException("Input too large");
        byte[] payload = Files.readAllBytes(payloadPath);
        Json.object(Json.parse(UpdateClient.utf8(payload)));
        String pem = Files.readString(keyPath, StandardCharsets.US_ASCII).strip();
        if (!pem.startsWith("-----BEGIN PRIVATE KEY-----") || !pem.endsWith("-----END PRIVATE KEY-----")) throw new IllegalArgumentException("Expected unencrypted PKCS#8 PEM key");
        byte[] der = Base64.getDecoder().decode(pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", ""));
        try {
            PrivateKey key = KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(der));
            Signature signer = Signature.getInstance("Ed25519"); signer.initSign(key); signer.update(payload);
            String envelope = Json.write(Map.of("payload", Base64.getEncoder().encodeToString(payload), "signature", Base64.getEncoder().encodeToString(signer.sign())));
            Files.writeString(Path.of(args[2]), envelope + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } finally { Arrays.fill(der, (byte)0); }
        System.out.println("Signed envelope written. No release was published and no server was changed.");
    }
}
