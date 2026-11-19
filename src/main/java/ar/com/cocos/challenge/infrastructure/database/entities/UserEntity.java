package ar.com.cocos.challenge.infrastructure.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "accountnumber")
    private String accountNumber;

    public UserEntity() {}

    public UserEntity(Integer id, String email, String accountNumber) {
        this.id = id;
        this.email = email;
        this.accountNumber = accountNumber;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
