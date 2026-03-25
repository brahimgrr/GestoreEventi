# Documentazione dei Casi d'Uso – Versione 4

> **Base di confronto:** V4 estende la V3. I casi d'uso UC1–UC22 sono **invariati**. Di seguito sono documentati **solo i nuovi casi d'uso** introdotti dalla V4.

---

## Riepilogo

| | |
|---|---|
| **UC ereditati da V3** | UC1 – UC22 (invariati, non ridefiniti) |
| **Nuovi attori** | Nessuno |
| **Nuovi UC** | UC23, UC24 |

---

## UC23 – Ritirare una Proposta

| | |
|---|---|
| **Attore** | Configuratore |
| **Precondizione** | Il configuratore è autenticato (UC1). Esiste almeno una proposta in stato APERTA o CONFERMATA. |
| **Scenario principale** | 1. Il configuratore seleziona "Ritirare una proposta" dal Menu Principale. <br> 2. Il sistema mostra le proposte in stato APERTA o CONFERMATA con titolo, stato e data evento. <br> 3. Il configuratore seleziona una proposta. <br> 4. Il sistema mostra un riepilogo e avvisa che il ritiro è una misura eccezionale. <br> 5. Il configuratore conferma il ritiro. <br> 6. Il sistema transita la proposta allo stato RITIRATA, la rimuove dalla bacheca (se era APERTA) e invia una notifica a tutti gli aderenti. |
| **Postcondizione** | La proposta è in stato RITIRATA. Tutti gli aderenti sono stati notificati. Il cambio di stato è salvato persistentemente. |
| **Scenario alternativo** | 2a. Non esistono proposte ritirabili → il sistema lo segnala e torna al menu. |
| **Scenario alternativo** | 5a. Il configuratore non conferma → l'operazione è annullata. |
| **Scenario alternativo** | 6a. Il termine per il ritiro è già scaduto (mezzanotte del giorno precedente la data evento) → il sistema segnala l'errore e non esegue il ritiro. |

---

## UC24 – Disdire un'Iscrizione a una Proposta

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore è autenticato (UC16) ed è iscritto ad almeno una proposta in stato APERTA. |
| **Scenario principale** | 1. Il fruitore seleziona "Disdici iscrizione a una proposta" dal Menu Principale Fruitore. <br> 2. Il sistema mostra le proposte aperte a cui il fruitore è iscritto. <br> 3. Il fruitore seleziona una proposta. <br> 4. Il sistema mostra un riepilogo della proposta e chiede conferma. <br> 5. Il fruitore conferma. <br> 6. Il sistema rimuove il fruitore dall'elenco degli aderenti e salva persistentemente. |
| **Postcondizione** | Il fruitore è rimosso dall'elenco degli aderenti. Può iscriversi nuovamente alla stessa proposta rispettando il termine. |
| **Scenario alternativo** | 2a. Il fruitore non è iscritto a nessuna proposta aperta → il sistema lo segnala e torna al menu. |
| **Scenario alternativo** | 5a. Il fruitore non conferma → l'operazione è annullata. |
| **Scenario alternativo** | 6a. Il termine di iscrizione è scaduto → il sistema segnala l'errore e non esegue la disiscrizione. |

---

## Legenda UML

| Colore | Significato |
|---|---|
| Bianco | UC ereditati dalla V3 (invariati) |
| Verde | Nuovi casi d'uso introdotti dalla V4 |
