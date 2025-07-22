package org.annill.security.dto;

import lombok.Data;
import lombok.Value;

@Data
public class LoginDto{
    private String username;
    private String password;
}