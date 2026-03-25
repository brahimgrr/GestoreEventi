# Documentazione dei Casi d'Uso – Versione 5

> **Base di confronto:** V5 estende la V4. I casi d'uso UC1–UC24 sono **invariati**. Di seguito è documentato **solo il nuovo caso d'uso** introdotto dalla V5.

---

## Riepilogo

| | |
|---|---|
| **UC ereditati da V4** | UC1 – UC24 (invariati, non ridefiniti) |
| **Nuovi attori** | Nessuno |
| **Nuovi UC** | UC25 |

---

## UC25 – Importare Dati in Modalità Batch

| | |
|---|---|
| **Attore** | Configuratore |
| **Precondizione** | Il configuratore è autenticato (UC1). I campi base sono già fissati. Esiste un file JSON valido contenente i dati da importare. |
| **Scenario principale** | 1. Il configuratore seleziona "Importare dati in modalità batch" dal Menu Principale. <br> 2. Il sistema mostra le istruzioni e chiede il percorso del file JSON. <br> 3. Il configuratore inserisce il percorso del file. <br> 4. Il sistema legge il file e importa in ordine: campi comuni, categorie (con i relativi campi specifici), proposte. <br> 5. Il sistema mostra il riepilogo dell'importazione: numero di elementi importati per tipo ed eventuali errori. <br> 6. Le proposte valide importate sono disponibili per la pubblicazione tramite UC14. |
| **Postcondizione** | Gli elementi validi contenuti nel file sono stati aggiunti al sistema. Gli elementi non validi sono scartati con un messaggio di errore. Nessuna modifica ai dati pre-esistenti. |
| **Scenario alternativo** | 3a. Il file non esiste o non è leggibile → il sistema segnala l'errore e torna al menu. |
| **Scenario alternativo** | 4a. Un elemento del file non è valido (nome duplicato, tipo di dato errato, vincoli violati) → quell'elemento è scartato con un messaggio di errore; l'importazione degli altri continua (strategia best-effort). |
| **Scenario alternativo** | 4b. Il file non contiene dati da importare → il sistema lo segnala. |

> **Nota:** La modalità interattiva (UC5–UC14) rimane disponibile. L'importazione batch è un'alternativa aggiuntiva alla gestione interattiva.

---

## Legenda UML

| Colore | Significato |
|---|---|
| Bianco | UC ereditati dalla V4 (invariati) |
| Verde | Nuovo caso d'uso introdotto dalla V5 |
