package com.example.analizador_lexico;

import java.util.List;

public class Simbolos {
        private String nombre;
        private String tipo;
        private String ambito; // Podría ser "global", "local", etc.
        private String categoria; // Podría ser "variable" o "función"
        private String firma; // Para funciones, podría incluir el tipo de retorno y los tipos de parámetros

        // Constructor
        public Simbolos() {
                this.nombre = nombre;
                this.tipo = tipo;
                this.ambito = ambito;
                this.categoria = categoria;
                this.firma = firma;
        }

        // Métodos para obtener información
        public String getNombre() {
                return nombre;
        }

        public String getTipo() {
                return tipo;
        }

        public String getAmbito() {
                return ambito;
        }

        public String getCategoria() {
                return categoria;
        }

        public String getFirma() {
                return firma;
        }

        // Método para verificar si el símbolo ya existe
        public static boolean exists(String nombre, List<Simbolos> tablaSimbolos) {
                for (Simbolos simbolo : tablaSimbolos) {
                        if (simbolo.getNombre().equals(nombre)) {
                                return true; // Ya existe
                        }
                }
                return false; // No existe
        }

        // Método para verificar si una palabra es un símbolo
        public boolean isSimbolo(String palabra) {
                switch (palabra) {
                        case "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "==", "!=", ">", "<", ">=", "<=", "++", "--", "(", ")", "{", "}", "[", "]", ".", ",", ";", "'", "\"", ":", "|", "&", "||", "&&":
                                return true;
                        default:
                                return false;
                }
        }
}
