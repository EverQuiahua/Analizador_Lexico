package com.example.analizador_lexico;

public class pruebas {
    public static void main(String[] args) {
        float miFloat = 10.5f;
        double miDouble = 20.99;
        long miLong = 3000000L;

        int desdeFloat = (int) miFloat; // Convierte float a int
        int desdeDouble = (int) miDouble; // Convierte double a int
        int desdeLong = (int) miLong; // Convierte long a int

        System.out.println("Desde float: " + desdeFloat); // Resultado: 10
        System.out.println("Desde double: " + desdeDouble); // Resultado: 20
        System.out.println("Desde long: " + desdeLong); // Resultado: 3000000
    }
}
