package com.bossbuddy.loot;

public class BossDropFraction implements Comparable<BossDropFraction> {
    private int numerator;
    private int denominator;

    public BossDropFraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }


    public static BossDropFraction parseFraction(String s) {
        String[] parts = s.replace(",","").split("/");
        if (parts.length == 2) {
            double num = Double.parseDouble(parts[0]);
            double den = Double.parseDouble(parts[1]);
            return new BossDropFraction((int)Math.round(num), (int)Math.round(den));
        } else if (parts.length == 1) {
            int num = Integer.parseInt(parts[0]);
            return new BossDropFraction(num, 1);
        } else {
            throw new IllegalArgumentException("Invalid fraction format: " + s);
        }
    }

    @Override
    public int compareTo(BossDropFraction other) {
        long val1 = (long) this.numerator * other.denominator;
        long val2 = (long) other.numerator * this.denominator;
        return Long.compare(val1, val2);
    }
}