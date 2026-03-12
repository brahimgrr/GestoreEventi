package it.unibs.ingsoft.v3.model;

/**
 * Campi base FISSI definiti dalla traccia.
 * Sono sempre presenti, obbligatori e non modificabili.
 * Il configuratore al primo avvio può solo aggiungere campi base EXTRA.
 */

public enum CampoBaseDefinito
{
    TITOLO                  ("Titolo",                       TipoDato.STRINGA),
    NUMERO_PARTECIPANTI     ("Numero di partecipanti",       TipoDato.INTERO),
    TERMINE_ISCRIZIONE      ("Termine ultimo di iscrizione", TipoDato.DATA),
    LUOGO                   ("Luogo",                        TipoDato.STRINGA),
    DATA                    ("Data",                         TipoDato.DATA),
    ORA                     ("Ora",                          TipoDato.STRINGA),
    QUOTA_INDIVIDUALE       ("Quota individuale",            TipoDato.DECIMALE),
    DATA_CONCLUSIVA         ("Data conclusiva",              TipoDato.DATA);


    private final String nomeCampo;
    private final TipoDato tipoDato;

    CampoBaseDefinito(String nomeCampo, TipoDato tipoDato)
    {
        this.nomeCampo = nomeCampo;
        this.tipoDato  = tipoDato;
    }

    public String getNomeCampo()
    {
        return nomeCampo;
    }

    public TipoDato getTipoDato()
    {
        return tipoDato;
    }

    public static CampoBaseDefinito fromNome(String nome)
    {
        if (nome == null)
            return null;

        for (CampoBaseDefinito c : values())
        {
            if (c.nomeCampo.equalsIgnoreCase(nome.trim()))
                return c;
        }
        return null;
    }

    public static boolean isNomeFisso(String nome)
    {
        return fromNome(nome) != null;
    }
}