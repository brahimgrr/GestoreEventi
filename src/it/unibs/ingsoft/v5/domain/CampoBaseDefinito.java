package it.unibs.ingsoft.v5.domain;

/**
 * Enumeration of the eight fixed base fields mandated by the specification.
 * Each constant defines the canonical field name and its expected data type.
 */
public enum CampoBaseDefinito {
    TITOLO("Titolo", TipoDato.STRINGA),
    NUMERO_PARTECIPANTI("Numero di partecipanti", TipoDato.INTERO),
    TERMINE_ISCRIZIONE("Termine ultimo di iscrizione", TipoDato.DATA),
    LUOGO("Luogo", TipoDato.STRINGA),
    DATA("Data", TipoDato.DATA),
    ORA("Ora", TipoDato.ORA),
    QUOTA_INDIVIDUALE("Quota individuale", TipoDato.DECIMALE),
    DATA_CONCLUSIVA("Data conclusiva", TipoDato.DATA);

    private final String nomeCampo;
    private final TipoDato tipoDato;

    CampoBaseDefinito(String nomeCampo, TipoDato tipoDato) {
        this.nomeCampo = nomeCampo;
        this.tipoDato = tipoDato;
    }

    public String getNomeCampo() {
        return nomeCampo;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    /**
     * Case-insensitive lookup by field name; returns null if not found.
     */
    public static CampoBaseDefinito fromNome(String nome) {
        if (nome == null) return null;
        for (CampoBaseDefinito c : values())
            if (c.nomeCampo.equalsIgnoreCase(nome.trim()))
                return c;
        return null;
    }

    /**
     * Converts this enum constant to a {@link Campo} instance.
     * Base fields are always mandatory ({@code obbligatorio = true}).
     */
    public Campo toCampo() {
        return new Campo(nomeCampo, TipoCampo.BASE, tipoDato, true);
    }

    /**
     * Returns true if {@code nome} matches any fixed base field (case-insensitive).
     */
    public static boolean isNomeFisso(String nome) {
        return fromNome(nome) != null;
    }
}
