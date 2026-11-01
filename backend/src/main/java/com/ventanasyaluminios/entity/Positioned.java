package com.ventanasyaluminios.entity;

/** Entidad que el administrador puede reordenar: tiene id y una posición desde 1. */
public interface Positioned {

    Long getId();

    void setPosition(int position);
}
