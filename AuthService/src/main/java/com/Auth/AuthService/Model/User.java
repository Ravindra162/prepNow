package com.Auth.AuthService.Model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;


    private boolean isEmailVerified;


    public String getUsername() {
        return username;
    }
    public String getPassword(){
        return password;
    }


    public void setPassword(String encode) {
        this.password = encode;
    }
}
