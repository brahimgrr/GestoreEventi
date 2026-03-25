# Documentazione dei Casi d'Uso – Versione 2 (Delta rispetto a V1)

---

## 1. Riepilogo Relazione V1 → V2

I casi d'uso **UC1 – UC12** della Versione 1 (inclusi i casi d'uso inclusi *Aggiungi Campo*, *Rimuovi Campo*, *Modifica Obbligatorietà Campo*):
- ✔ **Invariati** — nessuna modifica funzionale
- ✔ **Non ridefiniti** — rimangono validi come documentati nella V1

La V2 introduce **3 nuovi casi d'uso**:
- **UC13** – Creare una Proposta di Iniziativa
- **UC14** – Pubblicare una Proposta in Bacheca
- **UC15** – Visualizzare la Bacheca

### Attori

| Attore | Stato V2 |
|---|---|
| **Configuratore** | Confermato. Unico attore anche in V2. Ora può anche creare proposte e visualizzare la bacheca. |

> [!NOTE]
> La specifica conferma: "Anche la seconda versione dell'applicazione consente l'accesso del solo configuratore." Il Fruitore sarà introdotto dalla V3.

---

## 2. Nuovi Casi d'Uso (SOLO V2)

---

### UC13 – Creare una Proposta di Iniziativa

| | |
|---|---|
| **Nome** | Creare una Proposta di Iniziativa |
| **Attore** | Configuratore |
| **Relazione V1** | Indipendente (nuovo caso d'uso V2) |
| **Scenario principale** | 1. Il configuratore seleziona "Creare una proposta di iniziativa" dal Menu Principale. <br> 2. Il sistema mostra le categorie disponibili. Il configuratore ne seleziona una. <br> 3. Il sistema presenta un modulo con tutti i campi della categoria (base, comuni, specifici), indicando tipo di dato e obbligatorietà di ciascuno. <br> 4. Il configuratore compila i campi. <br> 5. Il sistema valida automaticamente la proposta: verifica che tutti i campi obbligatori siano compilati e che i vincoli temporali siano rispettati. <br> 6. La proposta acquisisce lo stato di **VALIDA**. Il sistema mostra un riepilogo della proposta. <br> Postcondizione: la proposta è in stato VALIDA. Se non pubblicata, verrà scartata al termine della sessione. <br> Fine |
| **Scenario alternativo** | 2a. Non esistono categorie. <br> Il sistema segnala l'assenza di categorie e torna al Menu Principale. <br> Fine |
| **Scenario alternativo** | 4a. Il configuratore digita "annulla" durante la compilazione. <br> Il sistema scarta la proposta. <br> Fine |
| **Scenario alternativo** | 5a. La proposta NON è valida (campi obbligatori mancanti o vincoli temporali violati). <br> Il sistema mostra l'elenco degli errori e chiede se il configuratore vuole correggere i campi errati. <br> 5a.1. Il configuratore accetta di correggere: il sistema ripresenta solo i campi con errori. <br> Torna al punto 5. |
| **Scenario alternativo** | 5b. Il configuratore rifiuta di correggere. <br> La proposta viene scartata. <br> Fine |

**Vincoli temporali verificati dal sistema (dal codice):**
- "Termine ultimo di iscrizione" deve essere successivo alla data corrente
- "Data" dell'evento deve essere successiva di almeno 2 giorni rispetto a "Termine ultimo di iscrizione"
- "Data conclusiva" non può essere precedente a "Data"

---

### UC14 – Pubblicare una Proposta in Bacheca

| | |
|---|---|
| **Nome** | Pubblicare una Proposta in Bacheca |
| **Attore** | Configuratore |
| **Relazione V1** | Indipendente (nuovo caso d'uso V2) |
| **Scenario principale** | 1. Il configuratore richiede la pubblicazione di una proposta valida in bacheca. <br> 2. Il sistema mostra il riepilogo della proposta e chiede conferma. <br> 3. Il configuratore conferma la pubblicazione. <br> 4. Il sistema verifica che il termine di iscrizione non sia già scaduto. <br> 5. Il sistema verifica che non esista già una proposta con lo stesso Titolo, Data, Ora e Luogo. <br> 6. La proposta passa dallo stato VALIDA allo stato **APERTA**, riceve la data di pubblicazione odierna e viene salvata persistentemente nella bacheca. <br> Postcondizione: la proposta è in stato APERTA e conservata in modo persistente nell'archivio delle proposte. <br> Fine |
| **Scenario alternativo** | 3a. Il configuratore non conferma la pubblicazione. <br> Il sistema segnala che la proposta non sarà salvata e verrà scartata al termine della sessione. <br> Fine |
| **Scenario alternativo** | 4a. Il termine di iscrizione è già scaduto. <br> Il sistema segnala l'errore. La proposta non viene pubblicata. <br> Fine |
| **Scenario alternativo** | 5a. Esiste già una proposta duplicata (stesso Titolo, Data, Ora, Luogo). <br> Il sistema segnala l'errore. La proposta non viene pubblicata. <br> Fine |

---

### UC15 – Visualizzare la Bacheca

| | |
|---|---|
| **Nome** | Visualizzare la Bacheca |
| **Attore** | Configuratore |
| **Relazione V1** | Indipendente (nuovo caso d'uso V2) |
| **Scenario principale** | 1. Il configuratore seleziona "Visualizzare la bacheca" dal Menu Principale. <br> 2. Il sistema mostra tutte le proposte in stato APERTA, raggruppate per categoria. Per ogni proposta vengono mostrati i valori dei campi compilati. <br> 3. Il configuratore prende visione e torna al Menu Principale. <br> Postcondizione: nessuna modifica al sistema. <br> Fine |
| **Scenario alternativo** | 2a. La bacheca è vuota (nessuna proposta aperta). <br> Il sistema mostra un messaggio indicante l'assenza di proposte. <br> Fine |

> [!NOTE]
> La specifica precisa: "nella versione attuale dell'applicazione, il contenuto della bacheca è visibile ai soli configuratori." A partire dalla V3, sarà visibile anche ai fruitori.

---

## 3. Analisi Critica V2

### 3.1. Coerenza V1 → V2

| Aspetto | Valutazione |
|---|---|
| Backward compatibility | ✅ Tutti i casi d'uso V1 (UC1–UC12) sono preservati senza alcuna modifica funzionale nel codice V2. |
| Menu Principale | ✅ Il menu V2 aggiunge 3 nuove voci (opzioni 4, 5, 6) senza alterare le prime 3 (gestione campi base, comuni, categorie). |
| Visualizzazione (UC4) | ✅ In V2, "Visualizzare campi e categorie" ha ora una voce di menu dedicata (`menuVisualizza`) che mostra in un'unica schermata campi base, comuni e categorie. Miglioramento rispetto alla V1 senza alterare la semantica del caso d'uso. |
| Persistenza | ✅ Nuovo file persistente (`v2_proposte.json`) per la bacheca, indipendente dall'archivio catalogo (`v2_catalogo.json`) e utenti (`v2_utenti.json`). |

### 3.2. Funzionalità implementate ma NON esplicitamente richieste dalla specifica V2

| Funzionalità | Descrizione |
|---|---|
| Rilevamento duplicati alla pubblicazione | Il sistema impedisce la pubblicazione di una proposta se ne esiste già una con lo stesso Titolo, Data, Ora e Luogo. La specifica non lo richiede, ma è un vincolo di integrità ragionevole. |
| Vincolo Data conclusiva ≥ Data evento | Il sistema verifica che la data conclusiva non sia precedente alla data dell'evento. La specifica menziona solo il vincolo tra Termine iscrizione e Data. |
| Validazione inline con correzione selettiva | Quando la proposta non è valida, il sistema ripresenta SOLO i campi con errori (non l'intero modulo). Miglioramento UX non richiesto dalla specifica. |

### 3.3. Incoerenze e problemi

| Aspetto | Dettaglio |
|---|---|
| Proposta non pubblicata → scartata | La specifica prevede che "una proposta valida creata ma non pubblicata non viene salvata". Il sistema la scarta correttamente al termine della sessione, ma non offre alcuna possibilità di salvarla come bozza per sessioni future. Questo è coerente con la specifica, ma rappresenta una limitazione funzionale. |
| Bacheca visibile solo ai configuratori | Limitazione corretta per V2 (la specifica lo conferma esplicitamente). Sarà risolta in V3 con l'introduzione del Fruitore. |
| Scadenza termine iscrizione alla pubblicazione | Se un configuratore crea una proposta valida ma non la pubblica immediatamente, e nel frattempo il termine scade (es: a cavallo della mezzanotte), la pubblicazione viene correttamente rifiutata con un messaggio di errore.  |

---

## 4. Diagramma UML Completo V1 + V2 (PlantUML)

```plantuml
@startuml
left to right direction

actor Configuratore as C

rectangle "GestoreEventi - Back-End V1 + V2" {

    ' === V1 Use Cases ===
    usecase "UC1: Login" as Login
    usecase "UC2: Registrazione" as Reg
    usecase "UC3: Primo Accesso:\nFissare Campi Base" as Primo
    usecase "UC4: Visualizzare\nCampi e Categorie" as Vis

    usecase "UC5: Aggiungi\nCampo Comune" as ACC
    usecase "UC6: Rimuovi\nCampo Comune" as RCC
    usecase "UC7: Modifica Obbligatorietà\nCampo Comune" as MOCC

    usecase "UC8: Crea\nCategoria" as CreaCat
    usecase "UC9: Rimuovi\nCategoria" as RimCat

    usecase "UC10: Aggiungi\nCampo Specifico" as ACS
    usecase "UC11: Rimuovi\nCampo Specifico" as RCS
    usecase "UC12: Modifica Obbligatorietà\nCampo Specifico" as MOCS

    usecase "Aggiungi Campo" as AC
    usecase "Rimuovi Campo" as RC
    usecase "Modifica Obbligatorietà\nCampo" as MO

    ' === V2 Use Cases (highlighted) ===
    usecase "UC13: Creare una\nProposta di Iniziativa" as CreaProp #PaleGreen
    usecase "UC14: Pubblicare una\nProposta in Bacheca" as PubProp #PaleGreen
    usecase "UC15: Visualizzare\nla Bacheca" as VisBach #PaleGreen

    ' === V1 Relationships ===
    Login <.. Reg : <<extend>>
    Login <.. Primo : <<extend>>

    ACC ..> AC : <<include>>
    ACS ..> AC : <<include>>

    RCC ..> RC : <<include>>
    RCS ..> RC : <<include>>

    MOCC ..> MO : <<include>>
    MOCS ..> MO : <<include>>
}

C --> Login
C --> Vis
C --> ACC
C --> RCC
C --> MOCC
C --> CreaCat
C --> RimCat
C --> ACS
C --> RCS
C --> MOCS
C --> CreaProp
C --> PubProp
C --> VisBach

@enduml
```

### Legenda del diagramma

| Elemento | Significato |
|---|---|
| Sfondo bianco | Casi d'uso V1 (UC1–UC12 + inclusi) |
| Sfondo **verde** | Casi d'uso **nuovi della V2** (UC13–UC15) |
