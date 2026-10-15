package com.emls;

public class HashPassword {
    public static void main(String[] args) {
        System.out.println(PasswordUtil.hash(args[0].toCharArray()));
    }
}
