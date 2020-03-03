package cs455.scaling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class HashUtil {
    private static final Logger log = LogManager.getLogger(HashUtil.class);

    public static void main(String[] args) {
        Random random = new Random();
        byte[] bytes = new byte[Constants.MESSAGE_SIZE];
        random.nextBytes(bytes);

        String sent = new String(bytes);
        String received = SHA1FromBytes(sent.getBytes());

        String sentString = SHA1FromBytes(sent.getBytes());
        System.out.println(received.equals(sentString) ? "Equal" : "Not equal");
    }

    public static String SHA1FromBytes(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            // log.error("NoSuchAlgorithmException");
            e.printStackTrace();
        }
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

        // TODO: Pad with 0's in case length < 40 characters
        return hashInt.toString(16);
    }
}
