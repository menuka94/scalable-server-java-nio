package cs455.scaling;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class Hash {


    public static byte[] hash(byte[] in) {
        byte[] hash = null;
        try {
            MessageDigest sha1Hasher = MessageDigest.getInstance("SHA1");
            hash = sha1Hasher.digest(in);
        } catch (Exception e) {
            System.out.println("Lmao thats never gonna happen");
        }

        return hash;
    }

    public static byte[] hash(String s) {
        try {
            return hash(s.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("not gonna happen");
        }
        return null;
    }


}
