package it.unibs.ingsoft.v1.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumerates the 8 mandatory base fields defined in the specification (GENERALITÀ section).
 * These fields are fixed, immutable, and shared by all categories.
 *
 * <p><b>Invariant:</b> Each entry has a unique, non-blank name and a non-null data type.</p>
 */
public enum CampoBaseDefinito
{
    TITOLO("Titolo", TipoDato.STRINGA),
    NUMERO_PARTECIPANTI("Numero di partecipanti", TipoDato.INTERO),
    TERMINE_ISCRIZIONE("Termine ultimo di iscrizione", TipoDato.DATA),
    LUOGO("Luogo", TipoDato.STRINGA),
    DATA("Data", TipoDato.DATA),
    ORA("Ora", TipoDato.STRINGA),
    QUOTA_INDIVIDUALE("Quota individuale", TipoDato.DECIMALE),
    DATA_CONCLUSIVA("Data conclusiva", TipoDato.DATA);

    private final String   nome;
    private final TipoDato tipoDato;

    CampoBaseDefinito(String nome, TipoDato tipoDato)
    {
        this.nome     = nome;
        this.tipoDato = tipoDato;
    }

    public String getNome()       { return nome; }
    public TipoDato getTipoDato() { return tipoDato; }

    /**
     * Converts this enum constant to a {@link Campo} instance.
     * Base fields are always mandatory ({@code obbligatorio = true}).
     */
    public Campo toCampo()
    {
        return new Campo(nome, TipoCampo.BASE, tipoDato, true);
    }

    /**
     * Returns all 8 predefined base fields as {@link Campo} instances.
     */
    public static List<Campo> tutti()
    {
        return Arrays.stream(values())
                .map(CampoBaseDefinito::toCampo)
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} if the given name matches one of the predefined base field names
     * (case-insensitive).
     */
    public static boolean isNomeFisso(String nome)
    {
        return Arrays.stream(values())
                .anyMatch(c -> c.nome.equalsIgnoreCase(nome));
    }

    /**
     * Returns the enum constant matching the given name (case-insensitive).
     *
     * @throws IllegalArgumentException if no match is found
     */
    public static CampoBaseDefinito fromNome(String nome)
    {
        return Arrays.stream(values())
                .filter(c -> c.nome.equalsIgnoreCase(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nessun campo base predefinito con nome: " + nome));
    }
}
