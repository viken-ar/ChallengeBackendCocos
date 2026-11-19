package ar.com.cocos.challenge.infrastructure.database.entities;

import ar.com.cocos.challenge.domain.model.InstrumentType;
import jakarta.persistence.*;

@Entity
@Table(name = "instruments")
public class InstrumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InstrumentType type;

    public InstrumentEntity() {}

    public InstrumentEntity(Integer id, String ticker, String name, InstrumentType type) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.type = type;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public InstrumentType getType() { return type; }
    public void setType(InstrumentType type) { this.type = type; }
}
