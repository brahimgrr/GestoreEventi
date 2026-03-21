package it.unibs.ingsoft.v3.presentation.view.viewmodel;

import java.time.LocalDate;

/** View-model for a single Notifica entry. */
public record NotificaVM(int index, String messaggio, LocalDate data) {}
