package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.persistence.dto.NotificaData;

public interface INotificaRepository
{
    NotificaData load();
    void save(NotificaData data);
}
