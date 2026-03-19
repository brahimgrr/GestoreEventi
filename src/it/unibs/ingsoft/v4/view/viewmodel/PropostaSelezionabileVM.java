package it.unibs.ingsoft.v4.view.viewmodel;

/**
 * View-model for a Proposta entry in a selection list
 * (iscrizione / disdici / ritira).
 */
public record PropostaSelezionabileVM(
        int    index,
        String titolo,
        String categoriaNome,
        String data,
        String luogo,
        String termineScritto,
        String quota,
        int    numeroIscritti,
        int    maxPartecipanti,
        String stato
) {}
