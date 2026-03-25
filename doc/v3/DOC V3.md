# Documentazione dei Casi d'Uso – Versione 3

> **Base di confronto:** V3 estende la V2. I casi d'uso UC1–UC15 sono **invariati**. Di seguito sono documentati **solo i nuovi casi d'uso** introdotti dalla V3.

---

## Riepilogo

| | |
|---|---|
| **UC ereditati da V2** | UC1 – UC15 (invariati, non ridefiniti) |
| **Nuovi attori** | **Fruitore** (nuovo in V3) |
| **Nuovi UC** | UC16, UC17, UC18, UC19, UC20, UC21, UC22 |

---

## UC16 – Login Fruitore

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore è già registrato nel sistema. |
| **Scenario principale** | 1. Il fruitore seleziona "Accedi come Fruitore" dal Menu di Accesso. <br> 2. Il fruitore inserisce username e password. <br> 3. Il sistema verifica le credenziali. <br> 4. Il sistema concede l'accesso al Menu Principale Fruitore. |
| **Postcondizione** | Il fruitore è autenticato. |
| **Scenario alternativo** | 3a. Le credenziali sono errate → il sistema segnala l'errore e torna a 2. |

---

## UC17 – Registrazione Fruitore

| | |
|---|---|
| **Attore** | Fruitore (non ancora registrato) |
| **Precondizione** | Nessuna. |
| **Scenario principale** | 1. Il fruitore seleziona "Registrati come Fruitore" dal Menu di Accesso. <br> 2. Il sistema mostra i vincoli (username ≥ 3 caratteri, univoco nel sistema; password ≥ 4 caratteri). <br> 3. Il fruitore inserisce username e password. <br> 4. Il sistema verifica unicità e lunghezza minima. <br> 5. Il sistema chiede conferma. Il fruitore conferma. <br> 6. Il sistema registra il nuovo account e lo salva persistentemente. |
| **Postcondizione** | Il fruitore è registrato nel sistema. |
| **Scenario alternativo** | 4a. Lo username non è valido (troppo corto, già in uso, riservato) → il sistema segnala l'errore e torna a 3. |
| **Scenario alternativo** | 4b. La password è troppo corta → il sistema segnala l'errore e torna a 3. |
| **Scenario alternativo** | 5a. Il fruitore non conferma → il sistema torna al Menu di Accesso. |

---

## UC18 – Visualizzare la Bacheca (Fruitore)

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore è autenticato (UC16). |
| **Scenario principale** | 1. Il fruitore seleziona "Visualizza bacheca" dal Menu Principale Fruitore. <br> 2. Il sistema mostra tutte le proposte in stato APERTA, raggruppate per categoria, con titolo, scadenza e posti disponibili. <br> 3. Il fruitore consulta la lista e può selezionare una proposta per i dettagli. |
| **Postcondizione** | Nessuna modifica al sistema. |
| **Scenario alternativo** | 2a. La bacheca è vuota → il sistema lo segnala e torna al menu. |

---

## UC19 – Iscriversi a una Proposta Aperta

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore è autenticato e ha selezionato una proposta dalla bacheca (UC18). |
| **Scenario principale** | 1. Il fruitore seleziona una proposta aperta e ne visualizza i dettagli. <br> 2. Il sistema verifica che il fruitore non sia già iscritto e che ci siano posti disponibili. <br> 3. Il sistema chiede conferma. Il fruitore conferma. <br> 4. Il sistema registra l'adesione e salva persistentemente. |
| **Postcondizione** | Il fruitore è aggiunto all'elenco degli aderenti della proposta. |
| **Scenario alternativo** | 2a. Il fruitore è già iscritto → il sistema lo segnala e termina. |
| **Scenario alternativo** | 2b. I posti sono esauriti → il sistema lo segnala e termina. |
| **Scenario alternativo** | 2c. Il termine di iscrizione è scaduto → il sistema lo segnala e termina. |
| **Scenario alternativo** | 3a. Il fruitore non conferma → l'operazione è annullata. |

> **Nota:** Se l'iscrizione raggiunge il numero di partecipanti previsto, il sistema transita automaticamente la proposta da APERTA a CONFERMATA e invia notifiche a tutti gli aderenti.

---

## UC20 – Visualizzare lo Spazio Personale

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore è autenticato (UC16). |
| **Scenario principale** | 1. Il fruitore seleziona "Spazio Personale" dal Menu Principale Fruitore. <br> 2. Il sistema mostra l'elenco delle notifiche ricevute con data/ora e messaggio. |
| **Postcondizione** | Nessuna modifica al sistema. |
| **Scenario alternativo** | 2a. Non ci sono notifiche → il sistema lo segnala e torna al menu. |

---

## UC21 – Eliminare una Notifica

| | |
|---|---|
| **Attore** | Fruitore |
| **Precondizione** | Il fruitore ha aperto lo Spazio Personale (UC20) e sono presenti notifiche. |
| **Scenario principale** | 1. Il fruitore seleziona una notifica da eliminare. <br> 2. Il sistema chiede conferma. Il fruitore conferma. <br> 3. Il sistema elimina la notifica e salva persistentemente. |
| **Postcondizione** | La notifica è rimossa dallo spazio personale del fruitore. |
| **Scenario alternativo** | 2a. Il fruitore non conferma → l'operazione è annullata. |

---

## UC22 – Visualizzare l'Archivio delle Proposte

| | |
|---|---|
| **Attore** | Configuratore |
| **Precondizione** | Il configuratore è autenticato (UC1). |
| **Scenario principale** | 1. Il configuratore seleziona "Visualizzare archivio proposte" dal Menu Principale. <br> 2. Il sistema mostra tutte le proposte dell'archivio, raggruppate per categoria, con stato e cronologia dei passaggi di stato. |
| **Postcondizione** | Nessuna modifica al sistema. |

---

## Legenda UML

| Colore | Significato |
|---|---|
| Bianco | UC ereditati dalla V2 (invariati) |
| Verde | Nuovi casi d'uso introdotti dalla V3 |
