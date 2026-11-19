package ar.com.cocos.challenge.domain.model;

public record Instrument(
    Integer id,
    String ticker,
    String name,
    InstrumentType type
) {}
