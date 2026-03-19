package it.unibs.ingsoft.v3.persistence;

public interface INotificaRepository
{
    NotificaData load();
    void save(NotificaData data);
}
