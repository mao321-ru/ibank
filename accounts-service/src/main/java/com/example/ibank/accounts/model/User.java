package com.example.ibank.accounts.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Objects;

@Table( name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class User {

    @Id
    @Column( "user_id")
    private Long id;

    private String login;

    private String passwordHash;

    private String userName;

    private LocalDate birthDate;

    @Override
    public boolean equals( Object o) {
        if( this == o) return true;
        if( o == null || getClass() != o.getClass()) return false;
        User other = (User) o;
        return id != null && Objects.equals( id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash( id);
    }
}
