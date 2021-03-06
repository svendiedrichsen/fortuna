package com.grunka.random.fortuna.tests;

import com.grunka.random.fortuna.Fortuna;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dump {
    //TODO decide which dump method to use, maybe combine

    private static final int MEGABYTE = 1024 * 1024;

    // Compression test: xz -e9zvkf random.data

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            usage();
            System.exit(args.length == 0 ? 0 : 1);
        }
        long megabytes = 0;
        try {
            megabytes = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            usage();
            System.err.println("Megabytes was not a number: " + args[0]);
            System.exit(1);
        }
        if (megabytes < 1) {
            usage();
            System.err.println("Needs to be at least one megabyte, was " + megabytes);
            System.exit(1);
        }
        OutputStream output;
        if (args.length == 2) {
            output = new FileOutputStream(args[1], false);
        } else {
            output = System.out;
        }
        long dataSize = megabytes * MEGABYTE;
        System.err.println("Initializing RNG...");
        Fortuna fortuna = Fortuna.createInstance();
        System.err.println("Generating data...");
        try (OutputStream outputStream = new BufferedOutputStream(output)) {
            byte[] buffer = new byte[MEGABYTE];
            long remainingBytes = dataSize;
            while (remainingBytes > 0) {
                fortuna.nextBytes(buffer);
                outputStream.write(buffer);
                remainingBytes -= buffer.length;
                System.err.print((100 * (dataSize - remainingBytes) / dataSize) + "%\r");
            }
        }
        System.err.println("Done");
        fortuna.shutdown();
    }

    private static void usage() {
        System.err.println("Usage: " + Dump.class.getName() + " <megabytes> [<file>]");
        System.err.println("Will generate <megabytes> of data and output them either to <file> or stdout if <file> is not specified");
    }

    public static void otherMain(String[] args) throws IOException, InterruptedException {
        boolean hasLimit = false;
        BigInteger limit = BigInteger.ZERO;
        if (args.length == 1) {
            String amount = args[0];
            Matcher matcher = Pattern.compile("^([1-9][0-9]*)([KMG])?$").matcher(amount);
            if (matcher.matches()) {
                String number = matcher.group(1);
                String suffix = matcher.group(2);
                limit = new BigInteger(number);
                if (suffix != null) {
                    if ("K".equals(suffix)) {
                        limit = limit.multiply(BigInteger.valueOf(1024));
                    } else if ("M".equals(suffix)) {
                        limit = limit.multiply(BigInteger.valueOf(1024 * 1024));
                    } else if ("G".equals(suffix)) {
                        limit = limit.multiply(BigInteger.valueOf(1024 * 1024 * 1024));
                    } else {
                        System.err.println("Unrecognized suffix");
                        System.exit(1);
                    }
                }
                hasLimit = true;
            } else {
                System.err.println("Unrecognized amount " + amount);
                System.exit(1);
            }
        } else if (args.length > 1) {
            System.err.println("Unrecognized parameters " + Arrays.toString(args));
            System.exit(1);
        }
        Fortuna fortuna = Fortuna.createInstance();
        final BigInteger fourK = BigInteger.valueOf(4 * 1024);
        while (!hasLimit || limit.compareTo(BigInteger.ZERO) > 0) {
            final byte[] buffer;
            if (hasLimit) {
                if (fourK.compareTo(limit) < 0) {
                    buffer = new byte[4 * 1024];
                    limit = limit.subtract(fourK);
                } else {
                    buffer = new byte[limit.intValue()];
                    limit = BigInteger.ZERO;
                }
            } else {
                buffer = new byte[4 * 1024];
            }
            fortuna.nextBytes(buffer);
            System.out.write(buffer);
        }
        fortuna.shutdown();
    }
}
