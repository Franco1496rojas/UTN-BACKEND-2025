package com.frc.isi.museo.servicios.interfaces;

public interface ILookUpOrPersistService<T> {

    T getOrCreateAutor(String descripcion);

}