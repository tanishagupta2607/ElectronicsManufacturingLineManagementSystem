package com.emls;

public record Session(long userId, String username, String role, Long customerId) {
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isStaff() { return "STAFF".equals(role); }
    public boolean isClient() { return "CLIENT".equals(role); }
}
