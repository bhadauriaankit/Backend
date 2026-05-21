package com.ankit.elearning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;
    private String confirmPassword;   // optional — may be null when called from tests/API
    private String role;              // "STUDENT" or "AUTHOR" (defaults to STUDENT if blank)

    public String getName()            { return name; }
    public void   setName(String v)    { this.name = v; }

    public String getEmail()           { return email; }
    public void   setEmail(String v)   { this.email = v; }

    public String getPassword()        { return password; }
    public void   setPassword(String v){ this.password = v; }

    public String getConfirmPassword()        { return confirmPassword; }
    public void   setConfirmPassword(String v){ this.confirmPassword = v; }

    public String getRole()            { return role; }
    public void   setRole(String v)    { this.role = v; }
}
