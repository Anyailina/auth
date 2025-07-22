package org.annill.security.dto;

import java.util.Set;
import lombok.Data;

@Data
public class SignUpDto {

    private String username;


    private String email;

    private Set<String> role;

    private String password;

}