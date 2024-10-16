package com.example.analizador_lexico;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class semantico {
    private HelloController helloController;
    private HashMap<String, VariableInfo> variables;

    // Constructor que recibe la instancia de HelloController
    public semantico(HelloController helloController) {
        this.helloController = helloController;
        this.variables = new HashMap<>(); // Inicializa el HashMap
        mostrarExpresiones(); // Llamar a mostrarExpresiones al crear la instancia
    }

    private void mostrarExpresiones() {
        ObservableList<Analisis> lista_expresiones = helloController.getListaExpresiones();

        for (Analisis analisis : lista_expresiones) {
            System.out.println("Expresion: " + analisis.getExpresion());
            System.out.println("Tipo: " + analisis.getTipo());
            System.out.println("Renglon: " + analisis.getRenglon());
            System.out.println("Columna: " + analisis.getColumna());
            System.out.println("--------------------");  // Separador entre cada análisis
        }
        verificarTipos(lista_expresiones);
        //verificarOperaciones(lista_expresiones);// Llama a verificarTipos
    }


    // ---------------------------------------------------------------------------------------------------------
    // 1. Verificación de tipos

    private void verificarTipos(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);
            String expresion = analisis.getExpresion();
            String tipo = analisis.getTipo();

            // Verifica si es una palabra reservada y si es una declaración de variable
            if (new Palabra_Reservada().isPalabraReservada(expresion)) {
                // Esperamos que la siguiente expresión sea el nombre de la variable
                if (i + 1 < lista_expresiones.size() && lista_expresiones.get(i + 1).getTipo().equals("Variable")) {
                    // siguiente elemento de asignación
                    if (i + 2 < lista_expresiones.size() && lista_expresiones.get(i + 2).getExpresion().equals("=")) {
                        // siguiente elemento valor o una variable
                        if (i + 3 < lista_expresiones.size()) {
                            String valor = lista_expresiones.get(i + 3).getExpresion();
                            String tipoVariable = expresion; // La palabra reservada (ejemplo: "int", "String", etc.)
                            String nombreVariable = lista_expresiones.get(i + 1).getExpresion(); // Nombre de la variable

                            // Verificar si el valor es una variable, y en ese caso ignorar las verificaciones de comillas
                            if (lista_expresiones.get(i + 3).getTipo().equals("Variable")) {
                                // El valor es una variable, no es necesario hacer las verificaciones de comillas
                                variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));

                                // Imprime el contenido del mapa 'variables' en consola
                                for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                                    VariableInfo info = entry.getValue();
                                    System.out.println("Variable: " + info.getNombre() + ", Tipo: " + info.getTipo());
                                }
                            } else {
                                // Verifica si es un String
                                if (tipoVariable.equals("String")) {
                                    if (!lista_expresiones.get(i + 3).getExpresion().equals("\"") ||
                                            !lista_expresiones.get(i + 5).getExpresion().equals("\"")) {
                                        mostrarAlerta("Error Semántico",
                                                "Asignación inválida: " + tipoVariable + " " + nombreVariable + " = " + valor);
                                    } else {
                                        // Si la asignación es válida, guarda la variable con su tipo y nombre
                                        variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));

                                        // Imprime el contenido del mapa 'variables' en consola
                                        for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                                            VariableInfo info = entry.getValue();
                                            System.out.println("Variable: " + info.getNombre() + ", Tipo: " + info.getTipo());
                                        }
                                    }
                                } else if (tipoVariable.equals("char")) {
                                    if (!lista_expresiones.get(i + 3).getExpresion().equals("'") ||
                                            !lista_expresiones.get(i + 5).getExpresion().equals("'")) {
                                        mostrarAlerta("Error Semántico",
                                                "Asignación inválida: " + tipoVariable + " " + nombreVariable + " = " + valor);
                                    } else {
                                        // Si la asignación es válida, guarda la variable con su tipo y nombre
                                        variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));

                                        // Imprime el contenido del mapa 'variables' en consola
                                        for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                                            VariableInfo info = entry.getValue();
                                            System.out.println("Variable: " + info.getNombre() + ", Tipo: " + info.getTipo());
                                        }
                                    }
                                } else {
                                    // Verifica si el valor es compatible con el tipo de variable
                                    if (!esValorCompatible(tipoVariable, valor)) {
                                        mostrarAlerta("Error Semántico",
                                                "Asignación inválida: " + tipoVariable + " " + nombreVariable + " = " + valor);
                                    } else {
                                        // Si la asignación es válida, guarda la variable con su tipo y nombre
                                        variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));

                                        // Imprime el contenido del mapa 'variables' en consola
                                        for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                                            VariableInfo info = entry.getValue();
                                            System.out.println("Variable: " + info.getNombre() + ", Tipo: " + info.getTipo());
                                        }
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }

    private void verificarOperaciones(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            String expresion = lista_expresiones.get(i).getExpresion();
            String tipo = lista_expresiones.get(i).getTipo();

            // Verificar si la expresión es un operador matemático
            if (esOperadorMatematico(expresion)) {
                // Obtener los operandos antes y después del operador
                if (i - 1 >= 0 && i + 1 < lista_expresiones.size()) {
                    String operandoIzq = lista_expresiones.get(i - 1).getExpresion();
                    String operandoDer = lista_expresiones.get(i + 1).getExpresion();

                    // Obtener los tipos de los operandos
                    String tipoIzq = obtenerTipo(operandoIzq);
                    String tipoDer = obtenerTipo(operandoDer);

                    // Verificar si los tipos son compatibles para la operación matemática
                    if (!sonTiposCompatibles(tipoIzq, tipoDer)) {
                        mostrarAlerta("Error Semántico",
                                "Tipos incompatibles en la operación: " + operandoIzq + " " + expresion + " " + operandoDer);
                    } else {
                        System.out.println("Operación válida: " + operandoIzq + " " + expresion + " " + operandoDer);

                        // Ahora verificamos la variable que recibirá el resultado de la operación
                        if (i - 3 >= 0 && lista_expresiones.get(i - 2).getExpresion().equals("=")) {
                            String variableDestino = lista_expresiones.get(i - 3).getExpresion();
                            String tipoVariableDestino = obtenerTipo(variableDestino);

                            // Verificar si el tipo de la variable de destino es compatible con el resultado de la operación
                            if (!sonTiposCompatibles(tipoVariableDestino, tipoIzq) || !sonTiposCompatibles(tipoVariableDestino, tipoDer)) {
                                mostrarAlerta("Error Semántico",
                                        "Asignación inválida: La variable " + variableDestino + " no es compatible con el resultado de la operación.");
                            } else {
                                System.out.println("Asignación válida: " + variableDestino + " = " + operandoIzq + " " + expresion + " " + operandoDer);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean esOperadorMatematico(String expresion) {
        return expresion.equals("+") || expresion.equals("-") || expresion.equals("*") || expresion.equals("/");
    }

    private String obtenerTipo(String variable) {
        VariableInfo info = variables.get(variable);
        return (info != null) ? info.getTipo() : null; // Retorna el tipo si la variable existe
    }

    private boolean esValorCompatible(String tipoVariable, String valor) {
        switch (tipoVariable) {
            case "int":
                return valor.matches("\\d+"); // Solo números enteros
            case "double":
                return valor.matches("\\d+\\.\\d+"); // Números con decimales
            case "float":
                return valor.matches("\\d+\\.\\d+f"); // Números con decimales terminados en 'f'
            case "char":
                return valor.matches("'.'"); // Un solo carácter entre comillas simples
            case "boolean":
                return valor.equals("true") || valor.equals("false"); // Solo true o false
            case "String":
                return valor.matches("\".*\""); // Cadenas entre comillas dobles
            // Agregar más tipos según sea necesario
            default:
                return false; // Tipo no reconocido
        }
    }

    private boolean sonTiposCompatibles(String tipo1, String tipo2) {
        // Concatenación de strings
        if (tipo1.equals("String") && tipo2.equals("String")) return true;
        if (tipo1.equals("String") && tipo2.equals("char")) return true; // char puede concatenarse con String
        if (tipo1.equals("char") && tipo2.equals("String")) return true; // char puede concatenarse con String

        // Operaciones  con char
        if (tipo1.equals("char") && tipo2.equals("char")) return true; // Operaciones entre dos char

        // Compatibilidad entre números
        if (tipo1.equals("int") && tipo2.equals("int")) return true;
        if (tipo1.equals("double") && (tipo2.equals("int") || tipo2.equals("double"))) return true;
        if (tipo1.equals("int") && tipo2.equals("double")) return true;

        // Si uno es String y el otro no, no son compatibles para operaciones matemáticas
        if (tipo1.equals("String") || tipo2.equals("String")) return false;

        return false; // Por defecto no son compatibles
    }

    // Clase interna para almacenar información de variables
    private static class VariableInfo {
        private String tipo;
        private String nombre;
        public VariableInfo(String tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre;
        }
        public String getTipo() {
            return tipo;
        }
        public String getNombre () {
            return nombre;
        }
    }

    // FIN 1.Verificación de tipos
    // ---------------------------------------------------------------------------------------------------------


    // -----------------------------------------------------------------------------------
    //2. Verificación de la existencia de variables y funciones


    // Aqui va el codigo

    // FIN 2. Verificación de la existencia de variables y funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 3. Control de alcance (Scope)
// cambios
    // Aquí va el código para verificar el alcance de las variables y funciones.
    // Se asegura que las variables y funciones sean usadas solo dentro de su alcance.
    // Si una variable local es usada fuera de la función donde fue declarada,
    // debe producirse un error.

    // FIN 3. Control de alcance (Scope)
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 4. Verificación de la coherencia de parámetros en funciones

    // Aquí va el código para verificar que las funciones sean llamadas
    // con el número correcto de parámetros y tipos adecuados.
    // Si una función espera 3 parámetros y se le pasan 2 o los tipos no coinciden,
    // se generará un error semántico.

    // FIN 4. Verificación de la coherencia de parámetros en funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 5. Control de flujo de datos

    // Aquí va el código para verificar el flujo lógico del programa.
    // Se debe asegurar que las estructuras de control como if, for, while
    // estén correctamente definidas. Por ejemplo, verificar que en un ciclo for
    // no se alteren indebidamente las variables que controlan el ciclo.

    // FIN 5. Control de flujo de datos
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 6. Verificación de la compatibilidad de tipos en expresiones

    // Aquí va el código para garantizar que todas las operaciones
    // involucradas en las expresiones sean semánticamente correctas.
    // Se revisa que las operaciones matemáticas no generen errores de incompatibilidad,
    // como sumar un entero con un carácter, lo cual es inválido.

    // FIN 6. Verificación de la compatibilidad de tipos en expresiones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 7. Asignaciones correctas

    // Aquí va el código para verificar que las asignaciones sean correctas.
    // Se asegura que una variable pueda almacenar un valor compatible con su tipo.
    // Si una variable int recibe un valor double, se debe generar una advertencia
    // o error si hay riesgo de pérdida de precisión.

    // FIN 7. Asignaciones correctas
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 8. Verificación de retornos de funciones

    // Aquí va el código para verificar que las funciones devuelvan un valor
    // adecuado de acuerdo con su tipo de retorno. Si una función declara
    // que devuelve un int pero no devuelve nada o devuelve otro tipo,
    // se generará un error semántico.

    // FIN 8. Verificación de retornos de funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 9. Análisis de la sobrecarga de operadores y funciones

    // Aquí va el código para manejar la sobrecarga de operadores
    // y la sobrecarga de funciones en caso de ser permitido por el lenguaje.
    // Se debe identificar cuál versión de la función u operador debe invocarse
    // dependiendo de los tipos de los argumentos.

    // FIN 9. Análisis de la sobrecarga de operadores y funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 10. Compatibilidad y conversión de tipos

    // Aquí va el código para manejar la conversión implícita de tipos,
    // como convertir un int a float de manera segura.
    // También se debe evitar conversiones peligrosas como convertir un float a int
    // sin advertencias sobre la pérdida de precisión.

    // FIN 10. Compatibilidad y conversión de tipos
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 11. Gestión de declaraciones y definiciones múltiples

    // Aquí va el código para detectar declaraciones duplicadas de variables o funciones.
    // Si una variable o función es declarada dos veces en el mismo ámbito,
    // el compilador debe emitir un error semántico.

    // FIN 11. Gestión de declaraciones y definiciones múltiples
    //-------------------------------------------------


    // -----------------------------------------------------------------------------------------------------------------------
    // Código para mostrar alerta de error semántico
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
